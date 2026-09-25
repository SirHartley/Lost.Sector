package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.contacts.ContactIntel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidType;
import com.fs.starfarer.api.util.Misc;
import lostsector.helper.MathHelper;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;
import lostsector.world.SectorGen;

import java.util.List;

// Eliza's port: the meeting in the `# KESTEVEN QUESTLINE: ELIZA` rows, which take over her market's dialog until it
// has finished once, the raid for her disks after a refusal, and her move when that market is decivilized. Active in
// every stage, as the old CorePlugin route, raid listener and QuestStageManager check were: they tested only flags,
// the market and the legacy stage.
final class KestevenElizaModule extends QuestModule<KestevenStage, KestevenState> {

    static final String UPDATE_ELIZA_MOVED = "elizaMoved";
    static final String RAID = "elizaDisks";
    private static final String RAID_ICON_COMMODITY = "nskr_electronics";
    private static final float RAID_CREDITS_MIN = 30000f;
    private static final float RAID_CREDITS_MAX = 40000f;

    KestevenElizaModule() {
        super();
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.check("elizaPort", ctx -> !ctx.has(KestevenFlag.ELIZA_DIALOG_FINISHED)
                && ctx.target() != null && KestevenQuest.atElizaMarket(ctx.target()));

        d.action("elizaMeet", KestevenElizaModule::meet);
        d.action("elizaToPort", KestevenElizaModule::toPort);
        d.action("elizaHandOver", KestevenElizaModule::handOver);

        d.raid(RAID, "elizaRaid");
        d.action("elizaRaid", KestevenElizaModule::raided);

        d.token("elizaFormer", ctx -> ctx.state().elizaFormerName == null ? "" : ctx.state().elizaFormerName);
        d.token("elizaRaidCredits", ctx -> Misc.getDGSCredits(ctx.state().elizaRaidCredits));
    }

    // A jump past JOB5_DISKS has met Eliza at her port and received her two disks by agreement; the search's onSkip
    // picked her market first.
    @Override
    protected void onSkip(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() != KestevenStage.JOB5_DISKS || ctx.has(KestevenFlag.ELIZA_DIALOG_FINISHED)) return;
        SectorEntityToken market = ctx.state().elizaMarket;
        if (KestevenPeople.getEliza() == null && market != null) {
            PersonAPI eliza = SectorGen.genEliza();
            market.getMarket().getCommDirectory().addPerson(eliza, 1);
            market.getMarket().addPerson(eliza);
            ctx.log("Eliza loc " + market.getMarket().getName());
        }
        ctx.set(KestevenFlag.ELIZA_DIALOG_FINISHED);
        ctx.set(KestevenFlag.ELIZA_HELPED);
        ctx.state().disksRecovered += 2;
        KestevenSatelliteModule.checkAllDisks(ctx);
    }

    // The old listener added the objective at priority 0 to every raid type, disruption included.
    @Override
    protected void onRaidObjectives(QuestContext<KestevenStage, KestevenState> ctx, MarketAPI market, SectorEntityToken entity,
                                    List<GroundRaidObjectivePlugin> objectives, RaidType type, int marineTokens, int priority) {
        if (priority != 0 || market == null) return;
        if (!ctx.has(KestevenFlag.ELIZA_RAID_ENABLED) || ctx.has(KestevenFlag.ELIZA_RAIDED)) return;
        SectorEntityToken port = ctx.state().elizaMarket;
        if (port == null || market != port.getMarket()) return;
        GroundRaidObjectivePlugin raid = ctx.raidObjective(RAID, market, RaidDangerLevel.EXTREME, RAID_ICON_COMMODITY);
        if (raid != null) objectives.add(raid);
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

    // The search set the market's marker with a plain memory flag, so it is cleared the same way.
    private static void handOver(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.state().disksRecovered += 2;
        KestevenSatelliteModule.checkAllDisks(ctx);
        SectorEntityToken target = ctx.target();
        if (target != null) target.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
    }

    // Runs in the raid dialog from the objective's performRaid, before the result rows print. The market is the dialog
    // target's, the raided one. Credits are a float, added directly and shown by the result row, as before; the random
    // draws come in the old order: the credits, then Eliza's fleet.
    private static void raided(QuestContext<KestevenStage, KestevenState> ctx) {
        MarketAPI market = ctx.target() == null ? null : ctx.target().getMarket();
        if (market == null) {
            ctx.log("Eliza raid without a market target");
            return;
        }
        KestevenState s = ctx.state();
        s.disksRecovered += 2;
        KestevenSatelliteModule.checkAllDisks(ctx);
        s.elizaRaidCredits = MathHelper.getSeededRandomNumberInRange(RAID_CREDITS_MIN, RAID_CREDITS_MAX, ctx.random(KestevenState.RANDOM_ELIZA));
        Global.getSector().getPlayerFleet().getCargo().getCredits().add(s.elizaRaidCredits);
        market.getPrimaryEntity().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        PersonAPI eliza = KestevenPeople.getEliza();
        market.getCommDirectory().removePerson(eliza);
        market.removePerson(eliza);
        ctx.set(KestevenFlag.ELIZA_RAIDED);
        KestevenElizaFleetsModule.spawnRaided(ctx, market.getPrimaryEntity(), eliza, ctx.random(KestevenState.RANDOM_ELIZA));
    }

    // Eliza back at her port after one of her fleets took her home (KestevenElizaFleetsModule.onFleetGone); the Eliza
    // ending waits for ELIZA_RETURNED.
    static void respawnEliza(QuestContext<KestevenStage, KestevenState> ctx, SectorEntityToken port) {
        PersonAPI eliza = KestevenPeople.getEliza();
        port.getMarket().getCommDirectory().addPerson(eliza, 1);
        port.getMarket().addPerson(eliza);
        ctx.set(KestevenFlag.ELIZA_RETURNED);
    }
}
