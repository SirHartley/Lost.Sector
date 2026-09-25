package lostsector.campaign.starts.hellspawn;

import com.fs.starfarer.api.Global;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.List;

// The days between the warning and the judgement, shown by HellSpawnJudgementIntel until T11 moves quest intel to rows.
final class HellSpawnCountdownModule extends QuestModule<HellSpawnStage, HellSpawnState> {

    static final float JUDGEMENT_DAYS = 40f;

    HellSpawnCountdownModule() {
        super(HellSpawnStage.COUNTDOWN);
    }

    @Override
    protected void onStart(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        ctx.startTimer(HellSpawnState.TIMER_JUDGEMENT);
        Global.getSector().getIntelManager().addIntel(new HellSpawnJudgementIntel(Global.getSector().getClock().getTimestamp()), false);
    }

    @Override
    protected void onDay(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        if (ctx.days(HellSpawnState.TIMER_JUDGEMENT) > JUDGEMENT_DAYS) {
            ctx.advance(HellSpawnStage.COUNTDOWN, HellSpawnStage.JUDGEMENT);
        }
    }

    @Override
    protected void onStop(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        HellSpawnJudgementIntel intel = HellSpawnJudgementIntel.get();
        if (intel != null) intel.endImmediately();
    }

    @Override
    protected void devInfo(QuestContext<HellSpawnStage, HellSpawnState> ctx, List<String> lines) {
        lines.add("judgement after " + (int) JUDGEMENT_DAYS + " days, " + (int) ctx.days(HellSpawnState.TIMER_JUDGEMENT) + " passed");
    }
}
