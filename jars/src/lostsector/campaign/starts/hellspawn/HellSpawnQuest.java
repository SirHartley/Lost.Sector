package lostsector.campaign.starts.hellspawn;

import lostsector.campaign.starts.GameModeManager;
import lostsector.quest.Quest;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestManager;
import lostsector.quest.QuestModule;
import lostsector.quest.Quests;

import java.util.List;

// The Hellspawn judgement: THRN's warning at player level 15, a 40-day countdown, then the judgement scene
// and, unless the captain was peaceful, the Final Judgement fleet. The Descent itself stays in HellSpawnManager
// and HellSpawnEventIntel.
public final class HellSpawnQuest extends Quest<HellSpawnStage, HellSpawnState> {

    public static final String ID = "hs";

    public HellSpawnQuest() {
        super(ID, HellSpawnStage.class, HellSpawnFlag.class, HellSpawnStage.DORMANT);
    }

    @Override
    protected HellSpawnState createState() {
        return new HellSpawnState();
    }

    // HellSpawnThrnModule comes before the scene modules: their onStart can open a scene at once, and its first
    // row shows THRN, who must be registered by then.
    @Override
    protected List<QuestModule<HellSpawnStage, HellSpawnState>> createModules() {
        return List.of(
                new HellSpawnSummonsModule(),
                new HellSpawnThrnModule(),
                new HellSpawnWarningModule(),
                new HellSpawnCountdownModule(),
                new HellSpawnJudgementModule(),
                new HellSpawnFightModule());
    }

    // The game mode is set by the Nexerelin background before onGameLoad, so it is final when this is checked.
    @Override
    protected boolean isAvailable() {
        return GameModeManager.getMode() == GameModeManager.GameMode.HELLSPAWN;
    }

    // Null before QuestManager.startQuests() and outside a Hellspawn campaign.
    public static HellSpawnState state() {
        return Quests.state(ID);
    }

    // HellSpawnJudgementInteraction: the player left the judgement encounter, after a fight or not.
    public static void reportJudgementLeft() {
        if (!Quests.is(HellSpawnStage.FIGHT)) return;
        QuestContext<HellSpawnStage, HellSpawnState> ctx = context();
        if (ctx != null) ctx.advance(HellSpawnStage.FIGHT, HellSpawnStage.JUDGED);
    }

    @SuppressWarnings("unchecked")
    private static QuestContext<HellSpawnStage, HellSpawnState> context() {
        QuestManager manager = QuestManager.get();
        if (manager == null) return null;
        return (QuestContext<HellSpawnStage, HellSpawnState>) manager.context(ID, "HellSpawnJudgementInteraction", null, null, List.of());
    }
}
