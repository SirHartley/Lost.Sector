package lostsector.campaign.events.hints;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import lostsector.campaign.bounties.BountiesQuest;
import lostsector.helper.MathHelper;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

// Signal hints: entering a system the player has never entered, outside the core, has a chance to point the player
// at a bounty system or Frost that no hint or discovery has used yet.
final class HintsSignalsModule extends QuestModule<HintsStage, HintsState> {

    static final float HINT_CHANCE = 0.04f;

    static final String ABYSS = BountiesQuest.ABYSS;
    static final String ETERNITY = BountiesQuest.ETERNITY;
    static final String MOTHERSHIP = BountiesQuest.MOTHERSHIP;
    static final String FROST = "frost";

    HintsSignalsModule() {
        super();
    }

    @Override
    protected void declare(Declarations<HintsStage, HintsState> d) {
        d.intel(HintsQuest.INTEL_SIGNAL, "hint", Tags.INTEL_FLEET_LOG, Tags.INTEL_EXPLORATION).majorPosting().descriptionBullets().deletable();
    }

    // Quest bounty comes before this quest in QuestCatalog, so its onStart has placed the bounties; a bounty without a
    // location gives no source.
    @Override
    protected void onStart(QuestContext<HintsStage, HintsState> ctx) {
        Map<String, StarSystemAPI> sources = ctx.state().sources;
        addSource(sources, ABYSS, BountiesQuest.location(BountiesQuest.ABYSS));
        addSource(sources, ETERNITY, BountiesQuest.location(BountiesQuest.ETERNITY));
        addSource(sources, MOTHERSHIP, BountiesQuest.location(BountiesQuest.MOTHERSHIP));
        StarSystemAPI frost = HintsFrostModule.frost();
        if (frost != null) sources.put(FROST, frost);
        ctx.log("signal sources " + sources.keySet());
    }

    @Override
    protected void onDay(QuestContext<HintsStage, HintsState> ctx) {
        dropSighted(ctx.state());
    }

    // reportCurrentLocationChanged fires when the jump switches locations, before the jump finishes and CoreScript
    // marks the system as entered (reportFleetJumped), so isEnteredByPlayer is still false on a first entry.
    @Override
    protected void onLocationChanged(QuestContext<HintsStage, HintsState> ctx, LocationAPI prev, LocationAPI curr) {
        HintsState s = ctx.state();
        dropSighted(s);
        if (s.sources.isEmpty() || !(curr instanceof StarSystemAPI)) return;
        StarSystemAPI system = (StarSystemAPI) curr;
        if (system.isEnteredByPlayer() || system.hasTag(Tags.THEME_CORE) || system.hasTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)) return;
        Random random = ctx.random("signal");
        if (random.nextFloat() >= HINT_CHANCE) return;
        List<String> ids = new ArrayList<>(s.sources.keySet());
        String id = ids.get(MathHelper.getSeededRandomNumberInRange(0, ids.size() - 1, random));
        StarSystemAPI source = s.sources.remove(id);
        s.records.put(id, new HintRecord(HintsQuest.INTEL_SIGNAL, source, null));
        ctx.intel().record(id).show(HintsQuest.INTEL_SIGNAL);
        ctx.intel().record(id).setMapLocation(HintsQuest.INTEL_SIGNAL, source.getHyperspaceAnchor());
    }

    static void bountySighted(QuestContext<HintsStage, HintsState> ctx) {
        HintsState s = ctx.state();
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        LocationAPI here = player == null ? null : player.getContainingLocation();
        for (Map.Entry<String, HintRecord> entry : s.records.entrySet()) {
            HintRecord record = entry.getValue();
            if (HintsQuest.INTEL_SIGNAL.equals(record.intel) && record.system == here) {
                ctx.intel().record(entry.getKey()).end(HintsQuest.INTEL_SIGNAL);
            }
        }
    }

    @Override
    protected void devInfo(QuestContext<HintsStage, HintsState> ctx, List<String> lines) {
        lines.add("signal sources left " + ctx.state().sources.keySet());
    }

    // A sighted bounty gives no more hints. The source ids are the bounty ids of quest bounty; Frost is none.
    private static void dropSighted(HintsState s) {
        s.sources.keySet().removeIf(BountiesQuest::sighted);
    }

    private static void addSource(Map<String, StarSystemAPI> sources, String id, SectorEntityToken location) {
        if (location != null && location.getStarSystem() != null) sources.put(id, location.getStarSystem());
    }
}
