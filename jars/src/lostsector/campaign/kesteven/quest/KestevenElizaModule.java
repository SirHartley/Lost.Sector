package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;
import lostsector.world.SectorGen;

import java.util.List;

// Eliza's port: the meeting in the `# KESTEVEN QUESTLINE: ELIZA` rows, which take over her market's dialog until it
// has finished once, and her move when that market is decivilized. Active in every stage, as the old CorePlugin route
// and QuestStageManager check were: they tested only flags, the market and the legacy stage.
final class KestevenElizaModule extends QuestModule<KestevenStage, KestevenState> {

    static final String UPDATE_ELIZA_MOVED = "elizaMoved";

    KestevenElizaModule() {
        super();
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.check("elizaPort", ctx -> !ctx.has(KestevenFlag.ELIZA_DIALOG_FINISHED)
                && ctx.target() != null && KestevenQuest.atElizaMarket(ctx.target()));

        d.action("elizaMeet", KestevenElizaModule::meet);
        d.action("elizaToPort", KestevenElizaModule::toPort);
        d.action("elizaEnableRaid", ctx -> enableRaid());
        d.action("elizaHandOver", KestevenElizaModule::handOver);

        d.token("elizaFormer", ctx -> ctx.state().elizaFormerName == null ? "" : ctx.state().elizaFormerName);
    }

    // DecivTracker.decivilize sets the market condition-only before it reports the decivilization. The old check ran
    // every frame from stage 16 on, failure included, and posted a campaign message; the Delve entry carries it now.
    @Override
    protected void onDecivilized(QuestContext<KestevenStage, KestevenState> ctx, MarketAPI decivilized, boolean fullyDestroyed) {
        KestevenState s = ctx.state();
        SectorEntityToken market = s.elizaMarket;
        if (market == null || market.getMarket() == null || !market.getMarket().isPlanetConditionMarketOnly()) return;
        if (ctx.stage().toLegacy() < 16 || !ctx.has(KestevenFlag.ELIZA_FOUND) || ctx.has(KestevenFlag.ELIZA_KILLED)) return;
        s.elizaFormerName = market.getMarket().getPrimaryEntity().getName();
        s.elizaMarket = QuestHelper.pickElizaMarket(ctx.random(KestevenState.RANDOM_QUEST), false);
        ctx.log("Eliza moved from " + s.elizaFormerName + " to " + s.elizaMarket.getName());
        PersonAPI eliza = KestevenPeople.getEliza();
        if (eliza != null) {
            s.elizaMarket.getMarket().getCommDirectory().addPerson(eliza, 1);
            s.elizaMarket.getMarket().addPerson(eliza);
            ContactIntel contact = ContactIntel.getContactIntel(eliza);
            if (contact != null && contact.getState() == ContactIntel.ContactState.LOST_CONTACT_DECIV) {
                contact.setState(ContactIntel.ContactState.PRIORITY);
            }
        }
        // The old campaign message played the minor message sound (MessageIntel.getCommMessageSound).
        String delve = KestevenJob5Module.INTEL;
        if (ctx.intel().isShown(delve)) ctx.intel().update(delve, UPDATE_ELIZA_MOVED, BaseIntelPlugin.getSoundMinorMessage());
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        SectorEntityToken market = ctx.state().elizaMarket;
        lines.add("Eliza's market: " + (market == null ? "none" : market.getName()) + ", Eliza exists: " + (KestevenPeople.getEliza() != null));
    }

    // Eliza is a fixed character made on first contact; the story skip may have made her already.
    private static void meet(QuestContext<KestevenStage, KestevenState> ctx) {
        if (KestevenPeople.getEliza() == null) SectorGen.genEliza();
    }

    // The dialog target's market, as the old dialog used; a station of a connected market shares it.
    private static void toPort(QuestContext<KestevenStage, KestevenState> ctx) {
        PersonAPI eliza = KestevenPeople.getEliza();
        MarketAPI market = ctx.target() == null ? null : ctx.target().getMarket();
        if (eliza == null || market == null) {
            ctx.log("Eliza not added to a port: " + (eliza == null ? "no Eliza" : "no market"));
            return;
        }
        market.getCommDirectory().addPerson(eliza, 1);
        market.addPerson(eliza);
    }

    // The listener is saved with the listener manager and adds the raid objective while the raid is enabled.
    private static void enableRaid() {
        ListenerManagerAPI listeners = Global.getSector().getListenerManager();
        if (!listeners.hasListenerOfClass(ElizaRaidObjectiveCreator.class)) {
            listeners.addListener(new ElizaRaidObjectiveCreator(), false);
        }
    }

    // The search set the market's marker with a plain memory flag, so it is cleared the same way.
    private static void handOver(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.state().disksRecovered += 2;
        SectorEntityToken target = ctx.target();
        if (target != null) target.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
    }
}
