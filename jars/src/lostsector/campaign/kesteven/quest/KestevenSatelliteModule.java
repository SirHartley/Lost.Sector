package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.ids.Abilities;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.helper.SectorLookup;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestFleet;
import lostsector.quest.QuestModule;
import lostsector.world.systems.frost.Frost;

import java.util.List;

// The data-disk satellites: #3 at the job 3 target, #4 at the job 4 enemy target and the two empty ones in Unknown Site.
// Their dialog is the # KESTEVEN QUESTLINE: SATELLITES block of rules.csv, on a claim that lasts in every stage; the
// salvage, the woken guard, ALL_DISKS_RECOVERED and the Frost sighting after Alice's second tip are here. Active in
// every stage, because a placed satellite can be salvaged at any time, also after the questline.
final class KestevenSatelliteModule extends QuestModule<KestevenStage, KestevenState> {

    static final String TRIGGER = "nskr_kqSatellite";
    // The job 3 dormant fleet once its satellite is salvaged: it hunts the player in its system for 30 days.
    static final String ROLE_GUARD = "satelliteGuard";
    static final float GUARD_DAYS = 30f;

    // Entity memory: $kQuestArtifact<number> names the satellite; the empty key marks one with nothing left.
    static final String ARTIFACT_KEY = "$kQuestArtifact";
    static final String EMPTY_KEY = "$nskr_artifactKeyEmpty";
    static final int ALL_DISKS = 5;

    KestevenSatelliteModule() {
        super();
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.trigger(TRIGGER);
        d.role(ROLE_GUARD, FleetRole.of(FleetOrders.huntInSystem().withdrawAfter(GUARD_DAYS)));

        d.check("satelliteEmpty", ctx -> ctx.target() != null && ctx.target().getMemoryWithoutUpdate().contains(EMPTY_KEY));

        d.action("salvageSatellite", KestevenSatelliteModule::salvage);
        d.action("wakeSatelliteGuard", KestevenSatelliteModule::wakeGuard);

        // The dialog target's disk; every satellite that is not #3 salvages as #4, as before.
        d.token("satelliteDisk", ctx -> ctx.target() == null ? "" : isJob3(ctx.target()) ? "3" : "4");
        // The second satellite's keywords: Frost's name as world generation stored it, and the constellation of the
        // system Alice's distance hint names. The row picks that system first (hub action pickJob5FrostTip).
        d.token("keywordFrost", ctx -> Frost.getName());
        d.token("keywordConstellation", ctx -> {
            StarSystemAPI tip = ctx.state().job5FrostTipSystem;
            return tip == null || tip.getConstellation() == null ? "" : tip.getConstellation().getName();
        });
    }

    // World generation placed the Unknown Site satellites before the state existed; a reset claims the placed #3 and #4
    // again. Satellites placed later are claimed by QuestHelper.spawnArtifact.
    @Override
    protected void onStart(QuestContext<KestevenStage, KestevenState> ctx) {
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (SectorEntityToken entity : system.getCustomEntities()) {
                if (isSatellite(entity)) claim(ctx, entity);
            }
        }
    }

    // The old frame check also ran when the stage started with the conditions already met; only a jump can do that.
    @Override
    protected void onStage(QuestContext<KestevenStage, KestevenState> ctx, KestevenStage from) {
        if (ctx.stage() != KestevenStage.JOB5_DISKS) return;
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (player != null) checkFrost(ctx, player.getContainingLocation());
        checkAllDisks(ctx);
    }

    // A jump past JOB5_DISKS has salvaged both satellites. The placed ones stay unsalvaged, as after the old story skip,
    // which counted them the same way.
    @Override
    protected void onSkip(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() != KestevenStage.JOB5_DISKS) return;
        KestevenState s = ctx.state();
        if (!ctx.has(KestevenFlag.SATELLITE3_RECOVERED)) {
            ctx.set(KestevenFlag.SATELLITE3_RECOVERED);
            s.disksRecovered++;
        }
        if (!ctx.has(KestevenFlag.SATELLITE4_RECOVERED)) {
            ctx.set(KestevenFlag.SATELLITE4_RECOVERED);
            s.disksRecovered++;
        }
        s.satellitesRecovered = 2;
        checkAllDisks(ctx);
    }

    @Override
    protected void onLocationChanged(QuestContext<KestevenStage, KestevenState> ctx, LocationAPI prev, LocationAPI curr) {
        checkFrost(ctx, curr);
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        KestevenState s = ctx.state();
        lines.add("satellites salvaged: " + s.satellitesRecovered + ", disks: " + s.disksRecovered);
        lines.add("woken guards: " + ctx.fleets().get(ROLE_GUARD).size());
    }

    static void claim(QuestContext<KestevenStage, KestevenState> ctx, SectorEntityToken satellite) {
        ctx.claimDialog(satellite, TRIGGER, KestevenStage.values());
    }

    // Every change of the disk count ends here. The old frame check set the flag only at JOB5_DISKS.
    static void checkAllDisks(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() == KestevenStage.JOB5_DISKS && ctx.state().disksRecovered >= ALL_DISKS) {
            ctx.set(KestevenFlag.ALL_DISKS_RECOVERED);
        }
    }

    private static void checkFrost(QuestContext<KestevenStage, KestevenState> ctx, LocationAPI location) {
        if (ctx.stage() != KestevenStage.JOB5_DISKS || ctx.has(KestevenFlag.FROST_FOUND) || !ctx.has(KestevenFlag.JOB5_ALICE_TIP2)) return;
        if (location != null && location == SectorLookup.getFrost()) ctx.set(KestevenFlag.FROST_FOUND);
    }

    private static void salvage(QuestContext<KestevenStage, KestevenState> ctx) {
        SectorEntityToken satellite = ctx.target();
        if (satellite == null) {
            ctx.log("salvageSatellite skipped: no dialog target");
            return;
        }
        KestevenState s = ctx.state();
        s.satellitesRecovered++;
        satellite.getMemory().set(EMPTY_KEY, true);
        s.disksRecovered++;
        checkAllDisks(ctx);
        ctx.set(isJob3(satellite) ? KestevenFlag.SATELLITE3_RECOVERED : KestevenFlag.SATELLITE4_RECOVERED);
        ctx.unmark(satellite);
    }

    // Satellite #3 wakes every Enigma dormant fleet in its location and turns it into a guard; #4 turns the job 4 strike
    // group in its location on the player. The memory clear keeps only the dormant flag and the no-disengage flag, as
    // before, plus the quest keys of a fleet that was already a quest fleet, such as the job 1 tip system's.
    private static void wakeGuard(QuestContext<KestevenStage, KestevenState> ctx) {
        SectorEntityToken satellite = ctx.target();
        if (satellite == null) {
            ctx.log("wakeSatelliteGuard skipped: no dialog target");
            return;
        }
        LocationAPI location = satellite.getContainingLocation();
        CampaignFleetAPI player = Global.getSector().getPlayerFleet();
        if (!isJob3(satellite)) {
            // The role's patrolHomeAfterChase keeps the intercept while the fleet shares the player's location; its
            // withdrawWhen then lets it despawn out of sight from JOB5_OFFERED on, as the old despawn check did.
            for (QuestFleet strikeGroup : ctx.fleets().get(KestevenJob4Module.ROLE_STRIKE_GROUP)) {
                if (strikeGroup.fleet().getContainingLocation() != location) continue;
                strikeGroup.fleet().clearAssignments();
                strikeGroup.fleet().addAssignment(FleetAssignment.INTERCEPT, player, Float.MAX_VALUE, "intercepting your fleet");
            }
            return;
        }
        for (SectorEntityToken entity : location.getAllEntities()) {
            MemoryAPI memory = entity.getMemoryWithoutUpdate();
            if (memory == null || !memory.contains(DormantSpawner.DORMANT_KEY)) continue;
            CampaignFleetAPI fleet = (CampaignFleetAPI) entity;
            QuestFleet known = ctx.fleets().registered(fleet);
            fleet.addAbility(Abilities.EMERGENCY_BURN);
            fleet.addAbility(Abilities.SENSOR_BURST);
            fleet.addAbility(Abilities.GO_DARK);
            fleet.setAI(Global.getFactory().createFleetAI(fleet));
            ctx.fleets().clearMemory(fleet);
            fleet.getMemoryWithoutUpdate().set(DormantSpawner.DORMANT_KEY, true);
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_PREVENT_DISENGAGE, true);
            if (known == null) {
                ctx.fleets().adopt(ROLE_GUARD, fleet);
            } else {
                // Refused, and logged, for another quest's fleet, which keeps its own role. The old code tracked the
                // woken fleet as a new entry, so the guard's 30 days count from waking.
                ctx.fleets().reassign(known, ROLE_GUARD);
                if (known.isRole(ROLE_GUARD)) known.info().age = 0f;
            }
            fleet.clearAssignments();
            fleet.addAssignment(FleetAssignment.INTERCEPT, player, Float.MAX_VALUE, "intercepting your fleet");
        }
    }

    private static boolean isJob3(SectorEntityToken satellite) {
        return satellite.getMemoryWithoutUpdate().contains(ARTIFACT_KEY + 3);
    }

    private static boolean isSatellite(SectorEntityToken entity) {
        MemoryAPI memory = entity.getMemoryWithoutUpdate();
        if (memory == null) return false;
        for (String key : memory.getKeys()) {
            if (key != null && key.startsWith(ARTIFACT_KEY)) return true;
        }
        return false;
    }
}
