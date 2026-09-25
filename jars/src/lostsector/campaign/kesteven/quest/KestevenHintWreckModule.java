package lostsector.campaign.kesteven.quest;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import lostsector.quest.Declarations;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

// The job 4 hint wreck: the first derelict KestevenJob4Module places near the enemy target. Its dialog is the hint wreck
// rows of # KESTEVEN QUESTLINE: JOB 4, on a claim that lasts in every stage until the player reads it. Active in every
// stage, because the wreck stays unread in the world after job 4 and the rows' action must run whenever it is read.
final class KestevenHintWreckModule extends QuestModule<KestevenStage, KestevenState> {

    static final String TRIGGER = "nskr_kqHintWreck";

    KestevenHintWreckModule() {
        super();
    }

    @Override
    protected void declare(Declarations<KestevenStage, KestevenState> d) {
        d.trigger(TRIGGER);
        d.action("readHintWreck", KestevenHintWreckModule::read);
    }

    static void claim(QuestContext<KestevenStage, KestevenState> ctx, SectorEntityToken wreck) {
        ctx.claimDialog(wreck, TRIGGER, KestevenStage.values());
    }

    // The vanilla derelict dialog returns once the claim is released.
    private static void read(QuestContext<KestevenStage, KestevenState> ctx) {
        ctx.set(KestevenFlag.JOB4_HINT_WRECK_READ);
        if (ctx.target() != null) ctx.releaseDialog(ctx.target());
        KestevenJob4Module.refreshMap(ctx);
    }
}
