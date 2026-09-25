package lostsector.campaign.kesteven.quest;

import lostsector.quest.Quest;
import lostsector.quest.QuestContext;
import lostsector.quest.QuestManager;
import lostsector.quest.QuestModule;
import lostsector.quest.Quests;

import java.util.List;
import java.util.Random;

// No modules yet: QuestStageManager and the old dialog classes still run the questline on this state (T16 to T35).
// isAvailable() keeps the default: the old code runs the questline in every campaign and treats a missing
// Kesteven home as failure (stage 99), so the state must always exist.
public final class KestevenQuest extends Quest<KestevenStage, KestevenState> {

    public static final String ID = "kq";

    public KestevenQuest() {
        super(ID, KestevenStage.class, KestevenFlag.class, KestevenStage.NOT_STARTED);
    }

    @Override
    protected KestevenState createState() {
        return new KestevenState();
    }

    @Override
    protected List<QuestModule<KestevenStage, KestevenState>> createModules() {
        return List.of();
    }

    // Null before load and, in a new campaign, until QuestManager creates the state on the first unpaused frame.
    public static KestevenState state() {
        return Quests.state(ID);
    }

    // Null when state() is null; QuestManager logs the reason.
    @SuppressWarnings("unchecked")
    static QuestContext<KestevenStage, KestevenState> context() {
        QuestManager manager = QuestManager.get();
        if (manager == null) return null;
        return (QuestContext<KestevenStage, KestevenState>) manager.context(ID, "legacy", null, null, List.of());
    }

    // Throws before the state exists: an unsaved Random would reroll after a reload.
    public static Random random(String purpose) {
        QuestContext<KestevenStage, KestevenState> ctx = context();
        if (ctx == null) throw new IllegalStateException("[" + ID + "] random " + purpose + " requested before the quest state exists");
        return ctx.random(purpose);
    }
}
