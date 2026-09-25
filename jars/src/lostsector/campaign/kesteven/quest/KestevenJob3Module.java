package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.IntelSortTier;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.helper.Ids;
import lostsector.helper.SectorLookup;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestFleet;
import lostsector.quest.QuestModule;

import java.util.List;

// Job 3, "Hostile Takeover": the Tri-Tachyon expedition, its countdown, the job's success or failure, the intel entry
// and the objects placed when the job is accepted. Briefing, acceptance, refusal and turn-in are the hub's.
final class KestevenJob3Module extends QuestModule<KestevenStage, KestevenState> {

    static final String INTEL = "job3";
    static final String ROLE_EXPEDITION = "job3Expedition";
    // Persistent: the old expedition despawned only once out of the player's sight, whatever the stage.
    static final String ROLE_WITHDRAWING = "job3ExpeditionOver";

    static final float TIME_LIMIT = 90f;
    static final float PREPARE_DAYS = 10f;
    static final float RETURN_AFTER_DAYS = 70f;
    static final float BEATEN_STRENGTH = 0.20f;

    static final String UPDATE_DONE = "done";
    static final String UPDATE_TIMEOUT = "timeout";
    static final String UPDATE_STEALTH = "stealth";

    KestevenJob3Module() {
        super(KestevenStage.JOB3_ACTIVE, KestevenStage.JOB3_TARGET_KNOWN, KestevenStage.JOB3_DONE);
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.intel(INTEL, "job3", Tags.INTEL_IMPORTANT, Tags.INTEL_ACCEPTED, Tags.INTEL_MISSIONS)
                .tier(IntelSortTier.TIER_2).majorPosting().faction(Ids.KESTEVEN_FACTION_ID).deletable().descriptionBullets();
        d.role(ROLE_EXPEDITION, FleetRole.of(FleetOrders.expedition(PREPARE_DAYS, RETURN_AFTER_DAYS)));
        d.role(ROLE_WITHDRAWING, FleetRole.of(FleetOrders.withdraw()).persistent());

        d.check("job3TimeLeft", ctx -> daysLeft(ctx) > 0f);

        // Whole days, cut down, as the old intel showed them.
        d.token("job3DaysLeft", ctx -> String.valueOf((int) daysLeft(ctx)));
        d.token("job3HomeMarket", ctx -> {
            SectorEntityToken start = ctx.state().job3Start;
            return start == null || start.getMarket() == null ? "" : start.getMarket().getName();
        });
        d.token("job3HomeSystem", ctx -> systemName(ctx.state().job3Start));
    }

    // The objects and the expedition are placed in the order QuestStageManager placed them on the first unpaused frame
    // at JOB3_ACTIVE; the start and target are read in the order the old intel's first display picked them. That keeps
    // the questline's shared random sequence.
    @Override
    protected void onStart(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.isJump() && !isActiveIn(ctx.jumpTarget())) {
            placeLeftovers();
            return;
        }
        SectorEntityToken start = QuestHelper.getJob3Start();
        SectorEntityToken target = QuestHelper.getJob3Target();
        ctx.intel().show(INTEL);
        ctx.intel().setMapLocation(INTEL, start);
        DormantSpawner.addDormant(target, "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
        ctx.log("job 3 dormant fleet placed at " + target.getName() + " in " + target.getStarSystem().getName());

        if (ctx.fleets().spawn(ROLE_EXPEDITION, KestevenFleets.job3Expedition(start, ctx.random(KestevenState.RANDOM_QUEST))) != null) {
            ctx.fleets().first(ROLE_EXPEDITION).info().target = target;
        }
        QuestHelper.spawnArtifact(target, 3);
        ctx.startTimer(KestevenState.TIMER_JOB3);
    }

    @Override
    protected void onStage(QuestContext<KestevenStage, KestevenState> ctx, KestevenStage from) {
        if (ctx.stage() == KestevenStage.JOB3_TARGET_KNOWN) {
            if (ctx.intel().isShown(INTEL)) ctx.intel().setMapLocation(INTEL, ctx.state().job3Target);
        } else if (ctx.stage() == KestevenStage.JOB3_DONE) {
            withdrawExpedition(ctx);
            // Reached without the expedition's outcome, such as by a jump.
            if (ctx.intel().isShown(INTEL)) {
                ctx.intel().setMapLocation(INTEL, home());
                if (ctx.has(KestevenFlag.JOB3_FAILED)) ctx.intel().fail(INTEL);
                else ctx.intel().complete(INTEL);
            }
        }
    }

    // Failure or a jump: the expedition withdraws out of sight and an entry that was not finished ends at once, as
    // the old ones did once the stage passed JOB3_TARGET_KNOWN.
    @Override
    protected void onStop(QuestContext<KestevenStage, KestevenState> ctx) {
        withdrawExpedition(ctx);
        if (ctx.intel().isShown(INTEL)) ctx.intel().end(INTEL);
    }

    // Stealth broken: the player hurt the expedition in a fight it saw them in with the transponder on. Loot comes
    // before the battle report, so this outcome wins over a success in the same fight, as it did before.
    @Override
    protected void onLoot(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet, FleetEncounterContextPlugin plugin, CargoAPI loot) {
        if (!fleet.isRole(ROLE_EXPEDITION) || !running(ctx)) return;
        CampaignFleetAPI expedition = fleet.fleet();
        if (plugin.getLoser() != expedition || !expedition.getMemoryWithoutUpdate().contains(MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON)) return;
        for (FleetEncounterContextPlugin.FleetMemberData member : plugin.getLoserData().getOwnCasualties()) {
            if (member.getStatus() != FleetEncounterContextPlugin.Status.NORMAL && plugin.computePlayerContribFraction() > 0f) {
                fail(ctx, UPDATE_STEALTH);
                return;
            }
        }
    }

    @Override
    protected void onBattle(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet, BattleAPI battle, CampaignFleetAPI primaryWinner) {
        if (fleet.isRole(ROLE_EXPEDITION) && fleet.fleet().getFleetPoints() < fleet.info().strength * BEATEN_STRENGTH) succeed(ctx, fleet);
    }

    @Override
    protected void onFleetGone(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet, FleetDespawnReason reason, Object param) {
        if (fleet.isRole(ROLE_EXPEDITION) && fleet.wasDestroyed(reason)) succeed(ctx, fleet);
    }

    // The old limit was the expedition's age, checked every 0.1 days; the timer starts with the expedition.
    @Override
    protected void onDay(QuestContext<KestevenStage, KestevenState> ctx) {
        if (running(ctx) && ctx.hasTimer(KestevenState.TIMER_JOB3) && ctx.days(KestevenState.TIMER_JOB3) > TIME_LIMIT) {
            QuestHelper.spawnEnvironmentalStorytelling();
            fail(ctx, UPDATE_TIMEOUT);
        }
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        KestevenState s = ctx.state();
        lines.add("start: " + (s.job3Start == null ? "none" : s.job3Start.getName()) + ", target: " + (s.job3Target == null ? "none" : s.job3Target.getName() + " in " + systemName(s.job3Target)));
        QuestFleet expedition = ctx.fleets().first(ROLE_EXPEDITION);
        lines.add("expedition: " + (expedition == null ? "none" : expedition.fleet().getFleetPoints() + " of " + expedition.info().strength + " points, age " + expedition.info().age)
                + ", withdrawing: " + ctx.fleets().get(ROLE_WITHDRAWING).size());
        lines.add("days left: " + daysLeft(ctx) + ", intel shown: " + ctx.intel().isShown(INTEL));
    }

    // A jump past the whole job leaves what job 5 finds at the target, satellite #3 and the dormant fleet, in the order
    // and with the draws of the old story skip; no intel, expedition or countdown.
    private static void placeLeftovers() {
        SectorEntityToken target = QuestHelper.getJob3Target();
        QuestHelper.spawnArtifact(target, 3);
        DormantSpawner.addDormant(target, "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
    }

    private static void succeed(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet) {
        if (!running(ctx)) return;
        fleet.fleet().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        finish(ctx, false, UPDATE_DONE);
    }

    private static void fail(QuestContext<KestevenStage, KestevenState> ctx, String updateKey) {
        ctx.set(KestevenFlag.JOB3_FAILED);
        finish(ctx, true, updateKey);
    }

    // The update replaces the old plain campaign message and plays its sound (MessageIntel's minor message sound).
    private static void finish(QuestContext<KestevenStage, KestevenState> ctx, boolean failed, String updateKey) {
        if (ctx.intel().isShown(INTEL)) {
            ctx.intel().setMapLocation(INTEL, home());
            if (failed) ctx.intel().fail(INTEL, updateKey, BaseIntelPlugin.getSoundMinorMessage());
            else ctx.intel().complete(INTEL, updateKey, BaseIntelPlugin.getSoundMinorMessage());
        }
        ctx.advance(ctx.stage(), KestevenStage.JOB3_DONE);
    }

    private static void withdrawExpedition(QuestContext<KestevenStage, KestevenState> ctx) {
        for (QuestFleet fleet : ctx.fleets().get(ROLE_EXPEDITION)) ctx.fleets().reassign(fleet, ROLE_WITHDRAWING);
    }

    private static boolean running(QuestContext<KestevenStage, KestevenState> ctx) {
        return ctx.stage() == KestevenStage.JOB3_ACTIVE || ctx.stage() == KestevenStage.JOB3_TARGET_KNOWN;
    }

    private static float daysLeft(QuestContext<KestevenStage, KestevenState> ctx) {
        return TIME_LIMIT - ctx.days(KestevenState.TIMER_JOB3);
    }

    private static SectorEntityToken home() {
        MarketAPI home = SectorLookup.asteriaOrOutpost();
        return home == null ? null : home.getPrimaryEntity();
    }

    private static String systemName(SectorEntityToken entity) {
        return entity == null || entity.getStarSystem() == null ? "" : entity.getStarSystem().getName();
    }
}
