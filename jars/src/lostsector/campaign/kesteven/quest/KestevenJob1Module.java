package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import lostsector.helper.Ids;
import lostsector.helper.SectorLookup;
import lostsector.quest.Declarations;
import lostsector.quest.FleetOrders;
import lostsector.quest.FleetRole;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.List;

// Job 1, "Enemy Unknown": the intel entry, the tip system's dormant fleet and the move to JOB1_DONE once both
// deliveries are recorded. Briefings and hand-ins are the hub's (KestevenHubModule).
final class KestevenJob1Module extends QuestModule<KestevenStage, KestevenState> {

    static final String INTEL = "job1";
    // Placed by QuestHelper.getJob1Tip; persistent because the old dormant fleet outlived the job.
    static final String ROLE_TIP_DORMANT = "job1Dormant";

    KestevenJob1Module() {
        super(KestevenStage.JOB1_ACTIVE, KestevenStage.JOB1_DONE);
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.intel(INTEL, "job1", Tags.INTEL_IMPORTANT, Tags.INTEL_ACCEPTED, Tags.INTEL_MISSIONS).descriptionBullets();
        d.role(ROLE_TIP_DORMANT, FleetRole.of(FleetOrders.none()).persistent());

        // The job 3 intel rows read it too.
        d.check("kestevenHostile", ctx -> Global.getSector().getPlayerFaction().getRelationship(Ids.KESTEVEN_FACTION_ID) <= -0.50f);
        d.check("job1TipBase", ctx -> tipHasBase(ctx.state()));

        // Rows call it after recording a job 1 delivery or the tip, where the old frame poll reacted.
        d.action("job1Progress", KestevenJob1Module::progress);

        d.token("job1HomeName", ctx -> {
            MarketAPI home = SectorLookup.asteriaOrOutpost();
            return home == null ? "" : home.getName();
        });
        d.token("job1ArtifactCount", ctx -> String.valueOf(KestevenHubModule.JOB1_ARTIFACTS));
    }

    // The old intel picked the tip system on its first display, right after the job was accepted.
    @Override
    protected void onStart(QuestContext<KestevenStage, KestevenState> ctx) {
        QuestHelper.getJob1Tip();
        ctx.intel().show(INTEL);
        progress(ctx);
    }

    // A jump past the tasks assumes the sensor task is done; the deliveries are the hub's.
    @Override
    protected void onSkip(QuestContext<KestevenStage, KestevenState> ctx) {
        if (ctx.stage() == KestevenStage.JOB1_ACTIVE) ctx.set(KestevenFlag.JOB1_SENSOR_DATA);
    }

    // The old entry ended once the stage passed JOB1_DONE, or on failure; it ended after the vanilla delay only when
    // the player had starred it, which QuestIntels cannot see.
    @Override
    protected void onStop(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.intel().end(INTEL);
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
        return state.job1TipSystem != null && QuestHelper.hasEnigmaBase(state.job1TipSystem);
    }
}
