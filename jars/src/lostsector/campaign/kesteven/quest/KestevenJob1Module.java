package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.IntelSortTier;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.helper.Ids;
import lostsector.helper.SectorLookup;
import lostsector.helper.SystemHelper;
import lostsector.helper.fleet.SystemPicker;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Job 1, "Enemy Unknown": the sensor task and the Enigma fight before it, the intel entry, the tip system's dormant
// fleet and the move to JOB1_DONE once both deliveries are recorded. Briefings and hand-ins are the hub's
// (KestevenHubModule). Active in JOB3_OFFERED only to complete the intel entry once the job is turned in.
final class KestevenJob1Module extends QuestModule<KestevenStage, KestevenState> {

    static final String INTEL = "job1";
    static final String UPDATE_SENSOR_DATA = "sensorData";
    // Placed by pickTip; persistent because the old dormant fleet outlived the job.
    static final String ROLE_TIP_DORMANT = "job1Dormant";
    private static final String ENIGMA_BASE_TYPE = "nskr_enigmabase";

    KestevenJob1Module() {
        super(KestevenStage.NOT_STARTED, KestevenStage.JOB1_ACTIVE, KestevenStage.JOB1_DONE, KestevenStage.JOB3_OFFERED);
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.intel(INTEL, "job1", Tags.INTEL_IMPORTANT, Tags.INTEL_ACCEPTED, Tags.INTEL_MISSIONS)
                .tier(IntelSortTier.TIER_2).majorPosting().faction(Ids.KESTEVEN_FACTION_ID).deletable().descriptionBullets();
        d.role(ROLE_TIP_DORMANT, FleetRole.of(FleetOrders.none()).persistent());

        // The job 3 intel rows read it too.
        d.check("kestevenHostile", ctx -> Global.getSector().getPlayerFaction().getRelationship(Ids.KESTEVEN_FACTION_ID) <= -0.50f);
        d.check("job1TipBase", ctx -> tipHasBase(ctx.state()));

        // Rows call it after recording a job 1 delivery or the tip, where the old frame poll reacted.
        d.action("job1Progress", KestevenJob1Module::progress);

        // Asteria or the Outpost, whichever hosts the questline; the job 3 intel rows read it too.
        d.token("homeName", ctx -> {
            MarketAPI home = SectorLookup.asteriaOrOutpost();
            return home == null ? "" : home.getName();
        });
        d.token("job1ArtifactCount", ctx -> String.valueOf(KestevenHubModule.JOB1_ARTIFACTS));
    }

    // The old intel picked the tip system on its first display, right after the job was accepted. A jump that passes the
    // whole job, such as the story skip, picks no tip and shows no intel, as the old skip did.
    @Override
    protected void onStage(QuestContext<KestevenStage, KestevenState> ctx, KestevenStage from) {
        if (ctx.isJump() && !isActiveIn(ctx.jumpTarget())) return;
        if (ctx.stage() == KestevenStage.JOB1_ACTIVE) {
            pickTip(ctx);
            ctx.intel().show(INTEL);
            progress(ctx);
        } else if (ctx.stage() == KestevenStage.JOB3_OFFERED && ctx.intel().isShown(INTEL)) {
            ctx.intel().complete(INTEL);
        }
    }

    // Any won fight against Enigma in which the player's share of the enemy's lost ships reaches one: before the job it
    // unlocks Jack's "I've already fought them.", during it the sensor task is done. Every such win at JOB1_ACTIVE
    // reports again, as the old check did.
    @Override
    protected void onEncounterLoot(QuestContext<KestevenStage, KestevenState> ctx, FleetEncounterContextPlugin plugin, CargoAPI loot) {
        KestevenStage stage = ctx.stage();
        if (stage != KestevenStage.NOT_STARTED && stage != KestevenStage.JOB1_ACTIVE) return;
        CampaignFleetAPI loser = plugin.getLoser();
        if (loser == null || !loser.getFaction().getId().equals(Ids.ENIGMA_FACTION_ID)) return;
        float kills = 0f;
        for (FleetEncounterContextPlugin.FleetMemberData member : plugin.getLoserData().getOwnCasualties()) {
            if (member.getStatus() != FleetEncounterContextPlugin.Status.NORMAL) kills += plugin.computePlayerContribFraction();
        }
        if (kills < 1f) return;
        if (stage == KestevenStage.NOT_STARTED) {
            ctx.set(KestevenFlag.FOUGHT_ENIGMA);
            return;
        }
        ctx.set(KestevenFlag.JOB1_SENSOR_DATA);
        // The old campaign message played the minor message sound (MessageIntel.getCommMessageSound).
        if (ctx.intel().isShown(INTEL)) ctx.intel().update(INTEL, UPDATE_SENSOR_DATA, BaseIntelPlugin.getSoundMinorMessage());
    }

    // A jump past the tasks assumes the sensor task is done; the deliveries are the hub's.
    @Override
    protected void onSkip(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() == KestevenStage.JOB1_ACTIVE) ctx.set(KestevenFlag.JOB1_SENSOR_DATA);
    }

    // Failure or a jump: an entry that was not completed ends at once, as the old one did on failure.
    @Override
    protected void onStop(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.intel().isShown(INTEL)) ctx.intel().end(INTEL);
    }

    // Catches changes no row reports: an Enigma base destroyed, the home market moved, flags set from the dev menu.
    @Override
    protected void onDay(QuestContext<KestevenStage, KestevenState> ctx) {
        progress(ctx);
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        StarSystemAPI tip = ctx.state().job1TipSystem;
        lines.add("tip system: " + (tip == null ? "none" : tip.getName() + (tipHasBase(ctx.state()) ? ", Enigma base" : ", no Enigma base")));
        lines.add("tip dormant fleets: " + ctx.fleets().get(ROLE_TIP_DORMANT).size());
        lines.add("intel shown: " + ctx.intel().isShown(INTEL));
    }

    private static void progress(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.intel().isShown(INTEL)) ctx.intel().setMapLocation(INTEL, mapLocation(ctx));
        if (ctx.stage() == KestevenStage.JOB1_ACTIVE
                && ctx.has(KestevenFlag.JOB1_DATA_DELIVERED) && ctx.has(KestevenFlag.JOB1_ELECTRONICS_DELIVERED)) {
            ctx.advance(KestevenStage.JOB1_ACTIVE, KestevenStage.JOB1_DONE);
        }
    }

    private static SectorEntityToken mapLocation(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.has(KestevenFlag.JOB1_TIP_GIVEN) && tipHasBase(ctx.state())) return ctx.state().job1TipSystem.getHyperspaceAnchor();
        MarketAPI home = SectorLookup.asteriaOrOutpost();
        return home == null ? null : home.getPrimaryEntity();
    }

    private static boolean tipHasBase(KestevenState state) {
        return state.job1TipSystem != null && hasEnigmaBase(state.job1TipSystem);
    }

    // A system with an Enigma base, picked on first use (Jack's tip or the job's start) and kept; the first pick also
    // adds a dormant Enigma fleet there. Both draw from the questline's shared random. Null when no system qualifies.
    static StarSystemAPI pickTip(QuestContext<KestevenStage, KestevenState> ctx) {
        KestevenState s = ctx.state();
        if (s.job1TipSystem != null) return s.job1TipSystem;
        StarSystemAPI system = pickSystemWithEnigmaBase(ctx, ctx.random(KestevenState.RANDOM_QUEST));
        if (system == null) return null;
        s.job1TipSystem = system;
        SectorEntityToken dormant = DormantSpawner.addDormant(
                SystemHelper.getRandomLocationInSystem(system, true, false, ctx.random(KestevenState.RANDOM_QUEST)), "enigma", 20f);
        if (dormant instanceof CampaignFleetAPI) ctx.fleets().adopt(ROLE_TIP_DORMANT, (CampaignFleetAPI) dormant);
        return system;
    }

    private static StarSystemAPI pickSystemWithEnigmaBase(QuestContext<KestevenStage, KestevenState> ctx, Random random) {
        SystemPicker picker = new SystemPicker(random, 1);
        picker.pickEntities = new ArrayList<>(List.of(ENIGMA_BASE_TYPE));
        picker.pickOnlyInProcgen = true;
        if (!picker.get().isEmpty()) {
            StarSystemAPI pick = picker.pick();
            ctx.log("job 1 tip system " + pick.getName());
            return pick;
        }
        ctx.log("no system with an Enigma base for the job 1 tip");
        return null;
    }

    private static boolean hasEnigmaBase(StarSystemAPI system) {
        for (SectorEntityToken entity : system.getAllEntities()) {
            if (ENIGMA_BASE_TYPE.equals(entity.getCustomEntityType())) return true;
        }
        return false;
    }
}
