package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import lostsector.helper.FleetHelper;
import lostsector.helper.Ids;
import lostsector.helper.SectorLookup;
import lostsector.helper.fleet.FleetInfo;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestFleet;
import lostsector.quest.QuestModule;

import java.util.List;
import java.util.Random;

// Eliza's three fleets: the one she leads after the raid on her market (spawned by KestevenElizaModule's raid action),
// the one that intercepts the player for the chip at CHIP_RECOVERED, and the one she sends after a player who took her
// market after her ending.
// Losing Eliza aboard sets ELIZA_KILLED. The comm conversations are the # KESTEVEN QUESTLINE: ELIZA FLEETS rows.
// Active in every stage, as the old fleet logic ran whatever the stage.
final class KestevenElizaFleetsModule extends QuestModule<KestevenStage, KestevenState> {

    static final String ROLE_RAIDED = "elizaRaided";
    static final String ROLE_INTERCEPT = "elizaIntercept";
    static final String ROLE_RETURNING = "elizaReturning";
    static final String ROLE_REVENGE = "elizaRevenge";

    // Set on a raided or intercept fleet once it is done: Eliza is no longer aboard, or it gives up the chase. The fleet
    // keeps its role, so its comm rows still match, and despawns once out of the player's sight.
    static final String WITHDRAW_KEY = "$nskr_kq_elizaFleetDone";

    static final float INTERCEPT_GIVE_UP_DAYS = 60f;

    KestevenElizaFleetsModule() {
        super();
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.role(ROLE_RAIDED, FleetRole.of(FleetOrders.intercept(FleetHelper.InterceptBehaviour.AROUND).withdrawWhen(KestevenElizaFleetsModule::done)));
        d.role(ROLE_INTERCEPT, FleetRole.of(FleetOrders.intercept(FleetHelper.InterceptBehaviour.DIRECT).withdrawWhen(KestevenElizaFleetsModule::done)));
        d.role(ROLE_RETURNING, FleetRole.of(FleetOrders.leave().withdrawWhen(KestevenElizaFleetsModule::done)));
        d.role(ROLE_REVENGE, FleetRole.of(FleetOrders.intercept(FleetHelper.InterceptBehaviour.AROUND)));

        d.check("elizaChipGone", ctx -> ctx.has(KestevenFlag.KESTEVEN_ENDING_DONE) || ctx.has(KestevenFlag.ALT_ENDING_DONE));

        d.action("elizaAggro", KestevenElizaFleetsModule::aggro);
        d.action("elizaTalked", ctx -> ctx.set(KestevenFlag.ELIZA_INTERCEPT_TALKED));
        d.action("elizaChipHandOver", KestevenElizaFleetsModule::handOver);
        d.action("elizaHostile", KestevenElizaFleetsModule::hostile);

        d.token("elizaHomeMarket", ctx -> {
            SectorEntityToken home = ctx.state().elizaMarket;
            return home == null || home.getMarket() == null ? "" : home.getMarket().getName();
        });
    }

    private static boolean done(FleetInfo info) {
        return info.fleet.getMemoryWithoutUpdate().getBoolean(WITHDRAW_KEY);
    }

    // The old checks ran on the questline's day tick (10 frame seconds); the betrayal check ran every unpaused frame.
    @Override
    protected void onDay(QuestContext<KestevenStage, KestevenState> ctx) {
        KestevenState s = ctx.state();
        SectorEntityToken home = s.elizaMarket;
        if (home != null && !home.getMarket().isPlanetConditionMarketOnly()
                && home.getMarket().getFaction().getId().equals(Factions.PLAYER)
                && ctx.has(KestevenFlag.ELIZA_HELPED) && !ctx.has(KestevenFlag.ELIZA_BETRAYED) && ctx.has(KestevenFlag.ELIZA_ENDING_DONE)) {
            ctx.set(KestevenFlag.ELIZA_BETRAYED);
        }
        if (!s.elizaInterceptSpawned && ctx.stage() == KestevenStage.CHIP_RECOVERED && ctx.has(KestevenFlag.ELIZA_HELPED)) {
            spawnFromMarket(ctx, ROLE_INTERCEPT, false);
            s.elizaInterceptSpawned = true;
            ctx.log("Eliza intercepts the player");
        }
        if (ctx.has(KestevenFlag.ELIZA_BETRAYED) && !s.elizaRevengeSpawned) {
            KestevenPeople.getEliza().getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);
            spawnFromMarket(ctx, ROLE_REVENGE, true);
            s.elizaRevengeSpawned = true;
        }
        // The old chase gave up once the player had talked and kept the chip for more than 60 days.
        if (ctx.has(KestevenFlag.ELIZA_INTERCEPT_TALKED) && !ctx.has(KestevenFlag.CHIP_HANDED_TO_ELIZA)) {
            for (QuestFleet fleet : ctx.fleets().get(ROLE_INTERCEPT)) {
                if (fleet.info().age > INTERCEPT_GIVE_UP_DAYS) fleet.fleet().getMemoryWithoutUpdate().set(WITHDRAW_KEY, true);
            }
        }
        checkFailure(ctx);
    }

    @Override
    protected void onBattle(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet, BattleAPI battle, CampaignFleetAPI primaryWinner) {
        if (carriesEliza(fleet) && !elizaAboard(fleet.fleet())) elizaLost(ctx, fleet);
    }

    // A raided or intercept fleet that despawns with Eliza aboard, not destroyed, took her home: after giving up the
    // chase, or on arrival after the hand-over.
    @Override
    protected void onFleetGone(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet, FleetDespawnReason reason, Object param) {
        if (!carriesEliza(fleet)) return;
        if (fleet.wasDestroyed(reason) || !elizaAboard(fleet.fleet())) {
            elizaLost(ctx, fleet);
            return;
        }
        if ((fleet.isRole(ROLE_INTERCEPT) || fleet.isRole(ROLE_RETURNING)) && ctx.state().elizaMarket != null) {
            KestevenElizaModule.respawnEliza(ctx, ctx.state().elizaMarket);
            ctx.log("Eliza is back at her market");
        }
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        for (String role : List.of(ROLE_RAIDED, ROLE_INTERCEPT, ROLE_RETURNING, ROLE_REVENGE)) {
            for (QuestFleet fleet : ctx.fleets().get(role)) {
                lines.add(role + ": age " + (int) fleet.info().age + ", Eliza aboard " + elizaAboard(fleet.fleet())
                        + ", done " + fleet.fleet().getMemoryWithoutUpdate().getBoolean(WITHDRAW_KEY));
            }
        }
        lines.add("intercept spawned " + ctx.state().elizaInterceptSpawned + ", revenge spawned " + ctx.state().elizaRevengeSpawned);
    }

    // The fleet Eliza leads after the raid on her port, from KestevenElizaModule's raid action with its random.
    static CampaignFleetAPI spawnRaided(QuestContext<KestevenStage, KestevenState> ctx, SectorEntityToken loc, PersonAPI eliza, Random random) {
        return spawn(ctx, ROLE_RAIDED, loc, eliza, random, true);
    }

    private static void spawnFromMarket(QuestContext<KestevenStage, KestevenState> ctx, String role, boolean hostile) {
        PersonAPI eliza = KestevenPeople.getEliza();
        SectorEntityToken loc = ctx.state().elizaMarket;
        spawn(ctx, role, loc, eliza, ctx.random(KestevenState.RANDOM_ELIZA), hostile);
        loc.getMarket().getCommDirectory().removePerson(eliza);
        loc.getMarket().removePerson(eliza);
    }

    private static CampaignFleetAPI spawn(QuestContext<KestevenStage, KestevenState> ctx, String role, SectorEntityToken loc,
                                          PersonAPI eliza, Random random, boolean hostile) {
        CampaignFleetAPI fleet = ctx.fleets().spawn(role, KestevenFleets.elizaFleet(loc, eliza, random, hostile));
        if (fleet == null) return null;
        fleet.setFaction(Factions.MERCENARY, false);
        FleetHelper.update(fleet, random);
        ctx.log("Eliza's fleet " + role + " at " + loc.getName() + " in " + loc.getContainingLocation().getName() + ", " + fleet.getFleetPoints() + " points");
        return fleet;
    }

    private static boolean carriesEliza(QuestFleet fleet) {
        return fleet.isRole(ROLE_RAIDED) || fleet.isRole(ROLE_INTERCEPT) || fleet.isRole(ROLE_RETURNING);
    }

    // As the old check: Eliza is aboard while one of the captains is her; a fleet without captains counts as long as it
    // has fleet points.
    private static boolean elizaAboard(CampaignFleetAPI fleet) {
        boolean anyCaptain = false;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListWithFightersCopy()) {
            if (member.getCaptain() == null) continue;
            if (member.getCaptain().getId().equals(Ids.ELIZA_PERSON_ID)) return true;
            anyCaptain = true;
        }
        return !anyCaptain && fleet.getFleetPoints() > 0;
    }

    private static void elizaLost(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet) {
        fleet.fleet().getMemoryWithoutUpdate().set(WITHDRAW_KEY, true);
        if (ctx.has(KestevenFlag.ELIZA_KILLED)) return;
        ctx.set(KestevenFlag.ELIZA_KILLED);
        Global.getSector().getImportantPeople().removePerson(Ids.ELIZA_PERSON_ID);
        ctx.log("Eliza killed");
        checkFailure(ctx);
    }

    // The questline fails when Eliza dies after the player handed her the chip.
    private static void checkFailure(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() == KestevenStage.CHIP_RECOVERED && ctx.has(KestevenFlag.CHIP_HANDED_TO_ELIZA)
                && ctx.has(KestevenFlag.ELIZA_KILLED) && !ctx.has(KestevenFlag.ENDED)) {
            ctx.set(KestevenFlag.JOB5_FAILED);
            ctx.advance(KestevenStage.FAILED);
            ctx.set(KestevenFlag.ENDED);
        }
    }

    private static void aggro(QuestContext<KestevenStage, KestevenState> ctx) {
        MemoryAPI memory = ctx.target().getMemoryWithoutUpdate();
        memory.set(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
    }

    private static void hostile(QuestContext<KestevenStage, KestevenState> ctx) {
        MemoryAPI memory = ctx.target().getMemoryWithoutUpdate();
        memory.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE, true);
        memory.set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        memory.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true);
        memory.unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        KestevenPeople.getEliza().getRelToPlayer().adjustRelationship(-0.75f, RepLevel.VENGEFUL);
    }

    // The important flags stay plain memory flags: Eliza's market and the Kesteven home are also set and cleared that
    // way by the Eliza and ending classes until they move to the quest.
    private static void handOver(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.set(KestevenFlag.CHIP_HANDED_TO_ELIZA);
        KestevenPeople.getEliza().getRelToPlayer().adjustRelationship(0.05f, RepLevel.COOPERATIVE);
        if (Global.getSector().getFaction(Factions.PLAYER).getRelationship(Ids.KESTEVEN_FACTION_ID) > -0.35f) {
            Global.getSector().getFaction(Factions.PLAYER).setRelationship(Ids.KESTEVEN_FACTION_ID, -0.35f);
        }
        SectorEntityToken target = ctx.target();
        target.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        MarketAPI kestevenHome = SectorLookup.asteriaOrOutpost();
        if (kestevenHome != null) kestevenHome.getMemory().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        SectorEntityToken home = ctx.state().elizaMarket;
        home.getMemory().set(MemFlags.MEMORY_KEY_MISSION_IMPORTANT, true);

        target.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE);
        target.getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE);
        // The fleet stands down and flies home, where it despawns and Eliza returns to her market (onFleetGone).
        for (QuestFleet fleet : ctx.fleets().get(ROLE_INTERCEPT)) {
            if (fleet.fleet() != target) continue;
            fleet.info().target = home;
            ctx.fleets().reassign(fleet, ROLE_RETURNING);
        }
        checkFailure(ctx);
    }
}
