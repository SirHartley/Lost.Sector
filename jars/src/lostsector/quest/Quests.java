package lostsector.quest;

// Queries for features outside a quest's package. All return safe values before a campaign is loaded.
public final class Quests {

    private Quests() {
    }

    // Null before load or when the quest has no state.
    @SuppressWarnings("unchecked")
    public static <T extends QuestState<?>> T state(String questId) {
        QuestManager manager = QuestManager.get();
        return manager == null ? null : (T) manager.state(questId);
    }

    public static boolean is(QuestStage... stages) {
        QuestManager manager = QuestManager.get();
        if (manager == null) return false;
        for (QuestStage stage : stages) {
            QuestState<?> state = manager.stateOf(stage);
            if (state != null && state.stage == stage) return true;
        }
        return false;
    }

    public static boolean reached(QuestStage stage) {
        QuestManager manager = QuestManager.get();
        QuestState<?> state = manager == null ? null : manager.stateOf(stage);
        return state != null && state.reached.contains(((Enum<?>) stage).name());
    }

    public static boolean has(Enum<?> flag) {
        QuestManager manager = QuestManager.get();
        QuestState<?> state = manager == null ? null : manager.stateOf(flag);
        return state != null && state.flags.contains(flag.name());
    }
}
