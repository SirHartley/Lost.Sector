package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import lostsector.campaign.enigma.DormantSpawner;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.List;

// Job 3, "Hostile Takeover": the intel entry and the objects placed when the job is accepted. Briefing, acceptance
// and refusal are the hub's.
// TODO T19: move the expedition fleet, its countdown, completion and failure here from QuestStageManager once
// FleetOrders can run the expedition's schedule (prepare at home, travel, orbit the target, return and stand down).
final class KestevenJob3Module extends QuestModule<KestevenStage, KestevenState> {

    static final String INTEL = "job3";

    KestevenJob3Module() {
        super(KestevenStage.JOB3_ACTIVE, KestevenStage.JOB3_TARGET_KNOWN);
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.intel(INTEL, "job3", Tags.INTEL_IMPORTANT, Tags.INTEL_ACCEPTED, Tags.INTEL_MISSIONS);

        d.check("job3TimeLeft", ctx -> ctx.state().job3TimeLeft > 0f);

        // Whole days, cut down, as the old intel showed them.
        d.token("job3DaysLeft", ctx -> String.valueOf((int) ctx.state().job3TimeLeft));
        d.token("job3MarketName", ctx -> {
            SectorEntityToken start = ctx.state().job3Start;
            return start == null || start.getMarket() == null ? "" : start.getMarket().getName();
        });
        d.token("job3MarketSystem", ctx -> systemName(ctx.state().job3Start));
        d.token("job3TargetSystem", ctx -> systemName(ctx.state().job3Target));
    }

    // QuestStageManager did this on the first unpaused frame at JOB3_ACTIVE, before it spawns the expedition. The start
    // and target are read in the order the old intel's first display picked them, which keeps the random sequence.
    // TODO T20/T21: replace the Java bar event with AddBarEvents rows.
    @Override
    protected void onStart(QuestContext<KestevenStage, KestevenState> ctx) {
        QuestHelper.getJob3Start();
        SectorEntityToken target = QuestHelper.getJob3Target();
        ctx.intel().show(INTEL);
        ctx.intel().setMapLocation(INTEL, mapLocation(ctx));
        PortsideBarData.getInstance().addEvent(new HostileTakeoverBarEvent());
        DormantSpawner.addDormant(target, "enigma", 45f, 50f, 0f, 1f, 1f, 1f, 1, 1);
        ctx.log("job 3 dormant fleet placed at " + target.getName() + " in " + target.getStarSystem().getName());
    }

    @Override
    protected void onStage(QuestContext<KestevenStage, KestevenState> ctx, KestevenStage from) {
        if (ctx.stage() == KestevenStage.JOB3_TARGET_KNOWN) ctx.intel().setMapLocation(INTEL, mapLocation(ctx));
    }

    // The old entry ended once the stage reached JOB3_DONE, or on failure; it ended after the vanilla delay only when
    // the player had starred it, which QuestIntels cannot see.
    @Override
    protected void onStop(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.intel().end(INTEL);
    }

    @Override
    protected void devInfo(QuestContext<KestevenStage, KestevenState> ctx, List<String> lines) {
        KestevenState s = ctx.state();
        lines.add("start: " + (s.job3Start == null ? "none" : s.job3Start.getName()) + ", target: " + (s.job3Target == null ? "none" : s.job3Target.getName() + " in " + systemName(s.job3Target)));
        lines.add("days left: " + s.job3TimeLeft + ", intel shown: " + ctx.intel().isShown(INTEL));
    }

    private static SectorEntityToken mapLocation(QuestContext<KestevenStage, KestevenState> ctx) {
        return ctx.stage() == KestevenStage.JOB3_TARGET_KNOWN ? ctx.state().job3Target : ctx.state().job3Start;
    }

    private static String systemName(SectorEntityToken entity) {
        return entity == null || entity.getStarSystem() == null ? "" : entity.getStarSystem().getName();
    }
}
