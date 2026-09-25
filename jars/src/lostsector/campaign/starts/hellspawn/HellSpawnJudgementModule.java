package lostsector.campaign.starts.hellspawn;

import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.List;

// The judgement scene. Its rows pick the path by the Descent's progress and advance to SPARED or FIGHT.
// onSkip grants nothing: a jump past this stage does not say which path it takes.
final class HellSpawnJudgementModule extends QuestModule<HellSpawnStage, HellSpawnState> {

    static final String TRIGGER = "nskr_hsJudgement";
    static final int PEACEFUL_MAX_POINTS = 350;
    static final int NEUTRAL_MAX_POINTS = 2000;

    HellSpawnJudgementModule() {
        super(HellSpawnStage.JUDGEMENT);
    }

    @Override
    protected void declare(Declarations<HellSpawnStage, HellSpawnState> d) {
        d.trigger(TRIGGER);
        d.check("neutral", ctx -> {
            int progress = progress();
            return progress >= PEACEFUL_MAX_POINTS && progress < NEUTRAL_MAX_POINTS;
        });
        d.check("hell", ctx -> progress() >= NEUTRAL_MAX_POINTS);
        d.action("grantPeacefulHeart", ctx -> ctx.rewards().skill(HellSpawnPeacefulSkill.ID, 1f));
    }

    // A pending open cannot be withdrawn, so a stage jump opens no scene; the dev menu opens triggers itself.
    @Override
    protected void onStart(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        if (!ctx.isJump()) HellSpawnThrnModule.openScene(ctx, TRIGGER);
    }

    @Override
    protected void devInfo(QuestContext<HellSpawnStage, HellSpawnState> ctx, List<String> lines) {
        lines.add("Descent progress " + progress() + ": peaceful below " + PEACEFUL_MAX_POINTS + ", hell from " + NEUTRAL_MAX_POINTS);
    }

    // -1 without the Descent intel, which the Hellspawn background creates with the campaign.
    private static int progress() {
        HellSpawnEventIntel intel = HellSpawnEventIntel.get();
        return intel == null ? -1 : intel.getProgress();
    }
}
