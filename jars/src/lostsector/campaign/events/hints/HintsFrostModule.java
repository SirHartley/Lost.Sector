package lostsector.campaign.events.hints;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import lostsector.helper.SectorLookup;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;
import lostsector.world.systems.frost.Frost;

import java.util.List;

// The Frost entry, shown on the first visit to Frost and closed once the Enigma no longer holds the Frozen Heart.
final class HintsFrostModule extends QuestModule<HintsStage, HintsState> {

    HintsFrostModule() {
        super();
    }

    @Override
    protected void declare(Declarations<HintsStage, HintsState> d) {
        d.intel(HintsQuest.INTEL_FROST, "frost", Tags.INTEL_FLEET_LOG, Tags.INTEL_EXPLORATION)
                .tier(IntelInfoPlugin.IntelSortTier.TIER_2).majorPosting().deletable();
        d.token("frostSystem", ctx -> {
            StarSystemAPI frost = frost();
            return frost == null ? "" : frost.getName();
        });
        d.check("frostGone", ctx -> ctx.state().frostGone);
    }

    @Override
    protected void onLocationChanged(QuestContext<HintsStage, HintsState> ctx, LocationAPI prev, LocationAPI curr) {
        HintsState s = ctx.state();
        if (s.frostShown) return;
        StarSystemAPI frost = frost();
        if (frost == null || curr != frost) return;
        s.frostShown = true;
        s.sources.remove(HintsSignalsModule.FROST);
        ctx.intel().show(HintsQuest.INTEL_FROST);
        ctx.intel().setMapLocation(HintsQuest.INTEL_FROST, frost.getHyperspaceAnchor());
    }

    // The Heart market leaves the Enigma through HeartOccupation's destruction, decivilization or an invasion; no
    // callback covers all three, so the owner is read once a day.
    @Override
    protected void onDay(QuestContext<HintsStage, HintsState> ctx) {
        HintsState s = ctx.state();
        if (!s.frostShown || s.frostGone || SectorLookup.enigmaExists()) return;
        s.frostGone = true;
        if (ctx.intel().isShown(HintsQuest.INTEL_FROST)) ctx.intel().close(HintsQuest.INTEL_FROST);
    }

    @Override
    protected void devInfo(QuestContext<HintsStage, HintsState> ctx, List<String> lines) {
        lines.add("frost shown " + ctx.state().frostShown + ", heart gone " + ctx.state().frostGone);
    }

    static StarSystemAPI frost() {
        return Global.getSector().getStarSystem(Frost.getName());
    }
}
