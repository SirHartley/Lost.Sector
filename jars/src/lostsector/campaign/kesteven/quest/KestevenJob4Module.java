package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.SectorEntityToken.VisibilityLevel;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.IntelSortTier;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import lostsector.campaign.events.EnvironmentalStorytelling;
import lostsector.helper.Ids;
import lostsector.helper.MathHelper;
import lostsector.helper.SectorLookup;
import lostsector.helper.SystemHelper;
import lostsector.helper.fleet.FleetInfo;
import lostsector.helper.fleet.SimpleFleet;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestFleet;
import lostsector.quest.QuestModule;
import lostsector.quest.Quests;
import org.lazywizard.lazylib.MathUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

// Job 4, "Operation Lifesaver": the wait before Alice offers it, the intel entry, the strike group, the Special
// Operations fleet and the splinter patrols, the hint wrecks, completion and the failure for attacking the Special
// Operations fleet. The Special Operations conversation and the hint wreck are rows in # KESTEVEN QUESTLINE: JOB 4;
// briefing, acceptance and turn-in are the hub's.
// Active until job 5's disks, because the job's fleets stay in the world until they withdraw (fleetsWithdraw).
final class KestevenJob4Module extends QuestModule<KestevenStage, KestevenState> {

    static final String INTEL = "job4";
    // Satellite #4's salvage wakes the strike group by this role (KestevenSatelliteModule.wakeGuard).
    static final String ROLE_STRIKE_GROUP = "job4StrikeGroup";
    static final String ROLE_SPECIAL_OPS = "job4SpecialOps";
    static final String ROLE_SPECIAL_OPS_LEAVING = "job4SpecialOpsLeaving";
    static final String ROLE_SPLINTER = "job4Splinter";
    static final String TRIGGER_HINT_WRECK = "nskr_kqHintWreck";

    static final float WAIT_DAYS = 30f;
    static final int SPLINTER_COUNT = 10;
    // The rows take these amounts with the same literals.
    static final int HELP_SUPPLIES = 250;
    static final int HELP_FUEL = 400;
    static final float BEATEN_STRENGTH = 0.20f;

    static final String UPDATE_DONE = "done";
    static final String UPDATE_FAILED = "failed";

    // The old code despawned the job's fleets from "stage 17 on" (FAILED included) or "stage 14 on with satellite #4
    // salvaged", in legacy stage numbers.
    private static final Set<KestevenStage> FROM_CACHE_KNOWN = EnumSet.of(
            KestevenStage.CACHE_KNOWN, KestevenStage.CACHE_CLEARED, KestevenStage.CHIP_RECOVERED,
            KestevenStage.COMPLETED, KestevenStage.FAILED);
    private static final Set<KestevenStage> FROM_JOB5 = EnumSet.of(
            KestevenStage.JOB5_OFFERED, KestevenStage.JOB5_MEETING, KestevenStage.JOB5_DISKS);

    // The hint wreck kept its old entity id prefix; nothing reads it since the wreck's dialog became a claim.
    private static final String HINT_WRECK_ID = "$job4HintWreck";

    KestevenJob4Module() {
        super(KestevenStage.JOB4_WAITING, KestevenStage.JOB4_ACTIVE, KestevenStage.JOB4_DONE,
                KestevenStage.JOB5_OFFERED, KestevenStage.JOB5_MEETING, KestevenStage.JOB5_DISKS);
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.intel(INTEL, "job4", Tags.INTEL_IMPORTANT, Tags.INTEL_ACCEPTED, Tags.INTEL_MISSIONS)
                .tier(IntelSortTier.TIER_2).majorPosting().faction(Ids.KESTEVEN_FACTION_ID).deletable().descriptionBullets();

        // Persistent: the fleets outlive the job and withdraw out of the player's sight, as they did before.
        d.role(ROLE_STRIKE_GROUP, FleetRole.of(FleetOrders.patrolHomeAfterChase("unknown")
                .withdrawWhen(info -> fleetsWithdraw() || beaten(info))).persistent());
        d.role(ROLE_SPECIAL_OPS, FleetRole.of(FleetOrders.keep().withdrawWhen(info -> fleetsWithdraw())).persistent());
        d.role(ROLE_SPECIAL_OPS_LEAVING, FleetRole.of(FleetOrders.leave("travelling back to")
                .withdrawWhen(info -> fleetsWithdraw())).persistent());
        d.role(ROLE_SPLINTER, FleetRole.of(FleetOrders.keep().withdrawWhen(info -> fleetsWithdraw())).persistent());
        d.trigger(TRIGGER_HINT_WRECK);

        d.check("job4CanHelp", ctx -> {
            CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
            return cargo.getSupplies() >= HELP_SUPPLIES && cargo.getFuel() >= HELP_FUEL;
        });
        // The old intel named the entity only when its name was not "Null".
        d.check("job4FriendlyNamed", ctx -> named(ctx.state().job4FriendlyTarget));
        d.check("job4TargetNamed", ctx -> named(ctx.state().job4EnemyTarget));

        d.action("recordJob4FleetTalk", KestevenJob4Module::recordFleetTalk);
        d.action("sendJob4FleetHome", KestevenJob4Module::sendFleetHome);
        d.action("readHintWreck", KestevenJob4Module::readHintWreck);

        d.token("job4FriendlySystem", ctx -> systemName(ctx.state().job4FriendlyTarget));
        d.token("job4FriendlyEntity", ctx -> ctx.state().job4FriendlyTarget == null ? "" : ctx.state().job4FriendlyTarget.getName());
        d.token("job4TargetEntity", ctx -> ctx.state().job4EnemyTarget == null ? "" : ctx.state().job4EnemyTarget.getName());
        d.token("job4OutpostSystem", ctx -> {
            SectorEntityToken outpost = SectorLookup.getOutpost();
            return outpost == null || outpost.getStarSystem() == null ? "" : outpost.getStarSystem().getName();
        });
        d.token("job4SearchArea", ctx -> {
            Constellation constellation = constellation(ctx);
            return constellation == null ? "" : QuestHelper.parseConstellation(constellation.getNameWithType());
        });
        // Whole units, cut down, as the old dialog printed them.
        d.token("job4Supplies", ctx -> String.valueOf((int) Global.getSector().getPlayerFleet().getCargo().getSupplies()));
        d.token("job4Fuel", ctx -> String.valueOf((int) Global.getSector().getPlayerFleet().getCargo().getFuel()));
    }

    @Override
    protected void onStage(QuestContext<KestevenStage, KestevenState> ctx, KestevenStage from) {
        switch (ctx.stage()) {
            case JOB4_WAITING:
                if (!ctx.hasTimer(KestevenState.TIMER_JOB4_WAIT)) ctx.startTimer(KestevenState.TIMER_JOB4_WAIT);
                break;
            case JOB4_ACTIVE:
                if (ctx.isJump() && !isActiveIn(ctx.jumpTarget())) placeForLaterStages(ctx);
                else start(ctx);
                break;
            case JOB4_DONE:
                // Reached without the job's completion, such as by a jump.
                if (ctx.intel().isShown(INTEL)) {
                    ctx.intel().setMapLocation(INTEL, home());
                    ctx.intel().complete(INTEL);
                }
                break;
            default:
                break;
        }
    }

    // The wait ends as onDay ends it. JOB4_FRIENDLY_FOUND stays unset: the hint wreck's result row reads it in every
    // stage, and the story skip, which places the wreck, never found the Special Operations fleet.
    @Override
    protected void onSkip(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() == KestevenStage.JOB4_WAITING) {
            ctx.set(KestevenFlag.JOB4_WAIT_OVER);
            ctx.clearTimer(KestevenState.TIMER_JOB4_WAIT);
        } else if (ctx.stage() == KestevenStage.JOB4_ACTIVE) {
            ctx.set(KestevenFlag.JOB4_TARGET_DESTROYED);
        }
    }

    // The old entry ended at once when the questline ended; a completed entry has long ended by CACHE_KNOWN.
    @Override
    protected void onStop(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.intel().end(INTEL);
    }

    // The wait was 300 frame seconds of unpaused time, 30 days; the old doubling applied only during the new-game
    // time advance (SectorAPI.isInFastAdvance), never at this stage. The hub rows read JOB4_WAIT_OVER.
    @Override
    protected void onDay(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() == KestevenStage.JOB4_WAITING) {
            if (!ctx.hasTimer(KestevenState.TIMER_JOB4_WAIT)) ctx.startTimer(KestevenState.TIMER_JOB4_WAIT);
            if (!ctx.has(KestevenFlag.JOB4_WAIT_OVER) && ctx.days(KestevenState.TIMER_JOB4_WAIT) > WAIT_DAYS) {
                ctx.set(KestevenFlag.JOB4_WAIT_OVER);
                ctx.clearTimer(KestevenState.TIMER_JOB4_WAIT);
            }
        } else if (ctx.stage() == KestevenStage.JOB4_ACTIVE) {
            respawnSpecialOps(ctx);
            refreshMap(ctx);
        }
    }

    // The old check ran on unpaused frames only, so a conversation that sets the last flag completes the job after
    // it closes.
    @Override
    protected boolean wantsFrames(QuestContext<KestevenStage, KestevenState> ctx) {
        return ctx.stage() == KestevenStage.JOB4_ACTIVE
                && ctx.has(KestevenFlag.JOB4_FRIENDLY_FOUND) && ctx.has(KestevenFlag.JOB4_TARGET_DESTROYED);
    }

    // The update replaces the old plain campaign message and plays its sound (MessageIntel's minor message sound).
    @Override
    protected void onFrame(QuestContext<KestevenStage, KestevenState> ctx, float amount) {
        if (ctx.intel().isShown(INTEL)) {
            ctx.intel().setMapLocation(INTEL, home());
            ctx.intel().complete(INTEL, UPDATE_DONE, BaseIntelPlugin.getSoundMinorMessage());
        }
        ctx.advance(KestevenStage.JOB4_ACTIVE, KestevenStage.JOB4_DONE);
    }

    // The old checks tested isVisibleToPlayerFleet every 0.1 days; this callback reports each change of that view.
    @Override
    protected void onFleetDetected(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet, VisibilityLevel level) {
        if (level == VisibilityLevel.NONE) return;
        if (isSpecialOps(fleet) && !ctx.has(KestevenFlag.JOB4_FRIENDLY_FOUND)) {
            ctx.set(KestevenFlag.JOB4_FRIENDLY_FOUND);
            refreshMap(ctx);
        } else if (fleet.isRole(ROLE_STRIKE_GROUP)
                && !ctx.has(KestevenFlag.JOB4_TARGET_FOUND) && !ctx.has(KestevenFlag.JOB4_TARGET_DESTROYED)) {
            ctx.set(KestevenFlag.JOB4_TARGET_FOUND);
            refreshMap(ctx);
        }
    }

    // Fleet points drop only in battles; the strike group's role withdraws it by the same test.
    @Override
    protected void onBattle(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet, BattleAPI battle, CampaignFleetAPI primaryWinner) {
        if (fleet.isRole(ROLE_STRIKE_GROUP) && beaten(fleet.info())) strikeGroupDestroyed(ctx, fleet);
    }

    @Override
    protected void onFleetGone(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet, FleetDespawnReason reason, Object param) {
        if (fleet.isRole(ROLE_STRIKE_GROUP) && fleet.wasDestroyed(reason)) strikeGroupDestroyed(ctx, fleet);
    }

    // The player hurt the Special Operations fleet in a fight they won. As before, the questline moves to JOB5_OFFERED
    // and then fails, unless it has already ended.
    @Override
    protected void onLoot(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet, FleetEncounterContextPlugin plugin, CargoAPI loot) {
        if (fleet.isRole(ROLE_STRIKE_GROUP) && beaten(fleet.info())) strikeGroupDestroyed(ctx, fleet);
        if (!isSpecialOps(fleet) || plugin.getLoser() != fleet.fleet()) return;
        for (FleetEncounterContextPlugin.FleetMemberData member : plugin.getLoserData().getOwnCasualties()) {
            if (member.getStatus() != FleetEncounterContextPlugin.Status.NORMAL && plugin.computePlayerContribFraction() > 0f) {
                attackedSpecialOps(ctx);
                return;
            }
        }
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        KestevenState s = ctx.state();
        lines.add("wait: " + (ctx.hasTimer(KestevenState.TIMER_JOB4_WAIT) ? ctx.days(KestevenState.TIMER_JOB4_WAIT) + " of " + WAIT_DAYS + " days" : "not running")
                + ", intel shown: " + ctx.intel().isShown(INTEL));
        lines.add("friendly target: " + (s.job4FriendlyTarget == null ? "none" : s.job4FriendlyTarget.getName() + " in " + systemName(s.job4FriendlyTarget))
                + ", enemy target: " + (s.job4EnemyTarget == null ? "none" : s.job4EnemyTarget.getName() + " in " + systemName(s.job4EnemyTarget)));
        QuestFleet strikeGroup = ctx.fleets().first(ROLE_STRIKE_GROUP);
        lines.add("strike group: " + (strikeGroup == null ? "none" : strikeGroup.fleet().getFleetPoints() + " of " + strikeGroup.info().strength + " points")
                + ", special operations: " + ctx.fleets().get(ROLE_SPECIAL_OPS).size() + " (" + ctx.fleets().get(ROLE_SPECIAL_OPS_LEAVING).size() + " leaving)"
                + ", splinters: " + ctx.fleets().get(ROLE_SPLINTER).size());
    }

    // Placement, in the order QuestStageManager used on the first unpaused frame at JOB4_ACTIVE, which keeps the
    // questline's shared random sequence. The intel comes first, so its posting message has no strike group yet.
    private static void start(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.intel().show(INTEL);
        spawnStrikeGroup(ctx);
        spawnSpecialOps(ctx);
        for (int i = 0; i < SPLINTER_COUNT; i++) {
            CampaignFleetAPI splinter = ctx.fleets().spawn(ROLE_SPLINTER, KestevenFleets.job4Splinter(ctx.random(KestevenState.RANDOM_QUEST)));
            if (splinter != null) SystemHelper.spawnAwayFromStarFixer(splinter);
        }
        placeWrecks(ctx);
        refreshMap(ctx);
    }

    // A jump past the whole job, such as the story skip, leaves what job 5 finds: the strike group guarding satellite #4
    // and the wrecks, in the order and with the draws of the old story skip; no intel, Special Operations or splinters.
    private static void placeForLaterStages(QuestContext<KestevenStage, KestevenState> ctx) {
        spawnStrikeGroup(ctx);
        placeWrecks(ctx);
    }

    // The strike group and satellite #4 at the enemy target.
    private static void spawnStrikeGroup(QuestContext<KestevenStage, KestevenState> ctx) {
        SimpleFleet spec = KestevenFleets.job4StrikeGroup(ctx.random(KestevenState.RANDOM_QUEST));
        ctx.state().job4EnemyTarget = spec.loc;
        CampaignFleetAPI fleet = ctx.fleets().spawn(ROLE_STRIKE_GROUP, spec);
        if (fleet != null) SystemHelper.spawnAwayFromStarFixer(fleet, 2.0f);
        QuestHelper.spawnArtifact(spec.loc, 4);
    }

    private static void spawnSpecialOps(QuestContext<KestevenStage, KestevenState> ctx) {
        CampaignFleetAPI fleet = ctx.fleets().spawn(ROLE_SPECIAL_OPS, KestevenFleets.job4SpecialOps(ctx.random(KestevenState.RANDOM_QUEST)));
        if (fleet != null) SystemHelper.spawnAwayFromStarFixer(fleet, 1.5f);
    }

    // The job cannot finish without finding the Special Operations fleet, so one that something else destroyed first is
    // replaced. The old check waited until the destroyed fleet left its list, which happened once the player was out
    // of hyperspace sensor range of it; the fleet held position in the friendly target's system.
    private static void respawnSpecialOps(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.has(KestevenFlag.JOB4_FRIENDLY_FOUND) || ctx.fleets().first(ROLE_SPECIAL_OPS) != null) return;
        SectorEntityToken target = ctx.state().job4FriendlyTarget;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (target == null || player == null) return;
        if (MathUtils.getDistance(player.getLocationInHyperspace(), target.getLocationInHyperspace()) <= Global.getSettings().getMaxSensorRangeHyper()) return;
        spawnSpecialOps(ctx);
        ctx.log("job 4 Special Operations fleet respawned");
    }

    // A debris field and three Kesteven derelicts near the enemy target; the first derelict holds the hint.
    // The recovery roll keeps the old Math.random.
    private static void placeWrecks(QuestContext<KestevenStage, KestevenState> ctx) {
        Random random = ctx.random(KestevenState.RANDOM_QUEST);
        SectorEntityToken loc = ctx.state().job4EnemyTarget;
        StarSystemAPI system = loc.getStarSystem();

        DebrisFieldTerrainPlugin.DebrisFieldParams params = new DebrisFieldTerrainPlugin.DebrisFieldParams(
                350f, // field radius - should not go above 1000 for performance reasons
                1.2f, // density, visual - affects number of debris pieces
                10000000f, // duration in days
                0f); // days the field will keep generating glowing pieces
        params.source = DebrisFieldTerrainPlugin.DebrisFieldSource.MIXED;
        params.baseSalvageXP = 500;
        SectorEntityToken debrisField = Misc.addDebrisField(system, params, random);
        debrisField.setSensorProfile(1000f);
        debrisField.setDiscoverable(true);
        float dist = MathHelper.getSeededRandomNumberInRange(150f, 250f, random);
        float days = MathHelper.getSeededRandomNumberInRange(30f, 60f, random);
        float angle = MathHelper.getSeededRandomNumberInRange(0f, 360f, random);
        debrisField.setCircularOrbit(loc, angle, dist, days);
        debrisField.setId("nskr_debrisField_" + random.nextLong());

        float recoveryChance = 0.25f;
        int count = 3;
        for (int y = 0; y < count; y++) {
            SectorEntityToken derelict = EnvironmentalStorytelling.addDerelict(
                    system, pickKestevenVariant(random), EnvironmentalStorytelling.randomCondition(), Math.random() < recoveryChance, null);
            derelict.setCircularOrbit(loc,
                    angle + MathHelper.getSeededRandomNumberInRange(-45f, 45f, random),
                    dist + MathHelper.getSeededRandomNumberInRange(-125f, 125f, random),
                    days + MathHelper.getSeededRandomNumberInRange(-3f, 3f, random));
            if (y == 0) {
                derelict.setId(HINT_WRECK_ID + random.nextLong());
                // Unread in every stage, as before; readHintWreck releases it and the vanilla derelict dialog returns.
                ctx.claimDialog(derelict, TRIGGER_HINT_WRECK, KestevenStage.values());
            }
        }
    }

    private static String pickKestevenVariant(Random random) {
        FactionAPI faction = Global.getSector().getFaction(Ids.KESTEVEN_FACTION_ID);
        List<String> variants = new ArrayList<>();
        String variant = "";
        while (variants.isEmpty()) {
            variants = new ArrayList<>(faction.getVariantsForRole(EnvironmentalStorytelling.randomRole(random, false)));
            variant = variants.get(MathHelper.getSeededRandomNumberInRange(0, variants.size() - 1, random));
            //only pick kesteven ships
            if (!Global.getSettings().getVariant(variant).getHullSpec().hasTag("kesteven")) {
                variants.clear();
            }
        }
        return variant;
    }

    // Actions

    // The first conversation with the Special Operations fleet: it is found, and it gives the strike group's location
    // unless the player already found or beat it.
    private static void recordFleetTalk(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.set(KestevenFlag.JOB4_FRIENDLY_TALKED);
        if (!ctx.has(KestevenFlag.JOB4_TARGET_FOUND) && !ctx.has(KestevenFlag.JOB4_TARGET_DESTROYED)) ctx.set(KestevenFlag.JOB4_TARGET_HINT);
        ctx.set(KestevenFlag.JOB4_FRIENDLY_FOUND);
        refreshMap(ctx);
    }

    // After the supplies and fuel: the fleet flies home and despawns there. Without a Kesteven home it stays, as before.
    private static void sendFleetHome(QuestContext<KestevenStage, KestevenState> ctx) {
        MarketAPI home = SectorLookup.asteriaOrOutpost();
        if (home == null) return;
        QuestFleet fleet = null;
        for (QuestFleet candidate : ctx.fleets().get(ROLE_SPECIAL_OPS)) {
            if (fleet == null || candidate.fleet() == ctx.target()) fleet = candidate;
        }
        if (fleet == null) return;
        fleet.fleet().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        fleet.info().target = home.getPrimaryEntity();
        ctx.fleets().reassign(fleet, ROLE_SPECIAL_OPS_LEAVING);
    }

    private static void readHintWreck(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.set(KestevenFlag.JOB4_HINT_WRECK_READ);
        if (ctx.target() != null) ctx.releaseDialog(ctx.target());
        refreshMap(ctx);
    }

    // Helpers

    private static void strikeGroupDestroyed(QuestContext<KestevenStage, KestevenState> ctx, QuestFleet fleet) {
        fleet.fleet().getMemoryWithoutUpdate().unset(MemFlags.MEMORY_KEY_MISSION_IMPORTANT);
        if (ctx.has(KestevenFlag.JOB4_TARGET_DESTROYED)) return;
        ctx.set(KestevenFlag.JOB4_TARGET_DESTROYED);
        refreshMap(ctx);
    }

    private static void attackedSpecialOps(QuestContext<KestevenStage, KestevenState> ctx) {
        if (!ctx.has(KestevenFlag.JOB4_FAILED)) {
            if (ctx.intel().isShown(INTEL)) ctx.intel().fail(INTEL, UPDATE_FAILED, BaseIntelPlugin.getSoundMinorMessage());
            ctx.set(KestevenFlag.JOB4_FAILED);
        }
        if (ctx.stage() != KestevenStage.JOB5_OFFERED) ctx.advance(KestevenStage.JOB5_OFFERED);
        if (!ctx.has(KestevenFlag.ENDED)) {
            ctx.advance(KestevenStage.JOB5_OFFERED, KestevenStage.FAILED);
            ctx.set(KestevenFlag.ENDED);
        }
    }

    // The map marker of the old intel: the most precise lead the player has.
    private static void refreshMap(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() != KestevenStage.JOB4_ACTIVE || !ctx.intel().isShown(INTEL)) return;
        ctx.intel().setMapLocation(INTEL, mapLocation(ctx));
    }

    private static SectorEntityToken mapLocation(QuestContext<KestevenStage, KestevenState> ctx) {
        KestevenState s = ctx.state();
        boolean targetActive = s.job4EnemyTarget != null && !ctx.has(KestevenFlag.JOB4_TARGET_DESTROYED);
        if (targetActive && (ctx.has(KestevenFlag.JOB4_TARGET_FOUND) || ctx.has(KestevenFlag.JOB4_TARGET_HINT))) return s.job4EnemyTarget;
        if (!ctx.has(KestevenFlag.JOB4_FRIENDLY_FOUND) && ctx.has(KestevenFlag.JOB4_HINT_WRECK_READ)) return s.job4FriendlyTarget;
        if (targetActive && s.nicholasDialogStage >= 1) return s.job4EnemyTarget.getStarSystem().getCenter();
        if (ctx.has(KestevenFlag.JOB4_FRIENDLY_FOUND)) return s.job4FriendlyTarget;
        Constellation constellation = constellation(ctx);
        return constellation == null ? null : Global.getSector().getHyperspace().createToken(constellation.getLocation());
    }

    static boolean fleetsWithdraw() {
        KestevenStage stage = KestevenQuest.stage();
        return FROM_CACHE_KNOWN.contains(stage) || FROM_JOB5.contains(stage) && Quests.has(KestevenFlag.SATELLITE4_RECOVERED);
    }

    private static boolean beaten(FleetInfo info) {
        return info.fleet.getFleetPoints() < info.strength * BEATEN_STRENGTH;
    }

    private static boolean isSpecialOps(QuestFleet fleet) {
        return fleet.isRole(ROLE_SPECIAL_OPS) || fleet.isRole(ROLE_SPECIAL_OPS_LEAVING);
    }

    private static Constellation constellation(QuestContext<KestevenStage, KestevenState> ctx) {
        SectorEntityToken target = ctx.state().job4FriendlyTarget;
        return target == null ? null : target.getConstellation();
    }

    private static boolean named(SectorEntityToken entity) {
        return entity != null && !entity.getName().equals("Null");
    }

    private static SectorEntityToken home() {
        MarketAPI home = SectorLookup.asteriaOrOutpost();
        return home == null ? null : home.getPrimaryEntity();
    }

    private static String systemName(SectorEntityToken entity) {
        return entity == null || entity.getStarSystem() == null ? "" : entity.getStarSystem().getName();
    }
}
