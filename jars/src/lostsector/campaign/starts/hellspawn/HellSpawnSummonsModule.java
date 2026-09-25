package lostsector.campaign.starts.hellspawn;

import com.fs.starfarer.api.Global;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestModule;

import java.util.List;

// Waits for the player level that brings THRN's warning. Levels are gained without a callback
// (ThronesGiftManager documents CharacterStats.addXP), so the check runs once a day.
final class HellSpawnSummonsModule extends QuestModule<HellSpawnStage, HellSpawnState> {

    static final int WARNING_LEVEL = 15;

    HellSpawnSummonsModule() {
        super(HellSpawnStage.DORMANT);
    }

    @Override
    protected void onDay(QuestContext<HellSpawnStage, HellSpawnState> ctx) {
        if (Global.getSector().getPlayerStats().getLevel() >= WARNING_LEVEL) {
            ctx.advance(HellSpawnStage.DORMANT, HellSpawnStage.WARNING);
        }
    }

    @Override
    protected void devInfo(QuestContext<HellSpawnStage, HellSpawnState> ctx, List<String> lines) {
        lines.add("player level " + Global.getSector().getPlayerStats().getLevel() + ", warning at " + WARNING_LEVEL);
    }
}
