package lostsector.campaign.starts.hellspawn;

import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

// THRN's warning. Its rows advance to COUNTDOWN on the second screen.
final class HellSpawnWarningModule extends QuestModule<HellSpawnStage, HellSpawnState> {

    static final String TRIGGER = "nskr_hsWarning";

    HellSpawnWarningModule() {
        super(HellSpawnStage.WARNING);
    }

    @Override
    protected void declare(Declarations<HellSpawnStage, HellSpawnState> d) {
        d.trigger(TRIGGER);
    }

    // A pending open cannot be withdrawn, so a stage jump opens no scene; the dev menu opens triggers itself.
    @Override
    protected void onStart(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        if (!ctx.isJump()) HellSpawnThrnModule.openScene(ctx, TRIGGER);
    }
}
