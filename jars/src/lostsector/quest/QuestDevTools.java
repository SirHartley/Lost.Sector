package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// The dev menu's view of the quests and its actions (README "Dev tools"); nskr_questDev shows the lists and prints
// the lines. It lives in the framework package for access to the state and the manager's runs, as QuestVerbs does.
// Errors never throw: they are reported with the quest id and printed in the dialog, which is always in dev mode here.
public final class QuestDevTools {

    // Far beyond any quest wait, so an expired timer passes every "at least N days" check.
    static final float EXPIRED_TIMER_DAYS = 1000f;

    // One entry of a dev menu list: the key the option id carries and the label shown.
    public static final class Item {

        public final String key;
        public final String label;

        Item(String key, String label) {
            this.key = key;
            this.label = label;
        }
    }

    private QuestDevTools() {
    }

    public static List<Item> quests() {
        List<Item> items = new ArrayList<>();
        QuestManager manager = QuestManager.get();
        if (manager == null) return items;
        for (QuestManager.Run<?, ?> run : manager.runs()) {
            QuestState<?> state = run.state();
            items.add(new Item(run.id(), run.id() + " (" + (state == null ? "no state" : state.stage.name()) + ")"));
        }
        return items;
    }

    public static boolean isQuest(String questId) {
        QuestManager manager = QuestManager.get();
        return manager != null && manager.run(questId) != null;
    }

    // Stages in declaration order, which is story order.
    public static List<Item> stages(String questId) {
        List<Item> items = new ArrayList<>();
        QuestManager.Run<?, ?> run = run(questId);
        if (run == null) return items;
        QuestState<?> state = run.state();
        for (Enum<?> stage : run.quest.stages().getEnumConstants()) {
            boolean current = state != null && state.stage == stage;
            items.add(new Item(stage.name(), stage.name() + (current ? " (current)" : "")));
        }
        return items;
    }

    public static List<Item> flags(String questId) {
        List<Item> items = new ArrayList<>();
        QuestManager.Run<?, ?> run = run(questId);
        if (run == null) return items;
        QuestState<?> state = run.state();
        for (Enum<?> flag : run.quest.flags().getEnumConstants()) {
            boolean set = state != null && state.flags.contains(flag.name());
            items.add(new Item(flag.name(), (set ? "[x] " : "[ ] ") + flag.name()));
        }
        return items;
    }

    public static List<Item> timers(String questId) {
        List<Item> items = new ArrayList<>();
        QuestManager.Run<?, ?> run = run(questId);
        QuestState<?> state = run == null ? null : run.state();
        if (state == null) return items;
        for (Map.Entry<String, Long> timer : state.timers.entrySet()) {
            items.add(new Item(timer.getKey(), timer.getKey() + ", " + days(timer.getValue()) + " days"));
        }
        return items;
    }

    public static List<Item> triggers(String questId) {
        List<Item> items = new ArrayList<>();
        QuestManager.Run<?, ?> run = run(questId);
        if (run == null) return items;
        for (String trigger : run.quest.declarations().triggers()) {
            items.add(new Item(trigger, trigger));
        }
        return items;
    }

    public static boolean isDeclaredTrigger(String questId, String trigger) {
        QuestManager.Run<?, ?> run = run(questId);
        return run != null && run.quest.declarations().triggers().contains(trigger);
    }

    // Stage, flags, timers and every module's devInfo, one line each.
    public static List<String> info(String questId) {
        List<String> lines = new ArrayList<>();
        QuestManager.Run<?, ?> run = run(questId);
        if (run == null) return lines;
        QuestState<?> state = run.state();
        String header = "[" + questId + "] " + run.quest.getClass().getSimpleName() + (run.available ? "" : ", unavailable");
        if (state == null) {
            lines.add(header + ", no state");
            return lines;
        }
        lines.add(header);
        lines.add("Stage " + state.stage.name() + ", " + days(state.stageSince) + " days");
        lines.add("Flags: " + (state.flags.isEmpty() ? "none" : String.join(", ", state.flags)));
        List<String> timers = new ArrayList<>();
        for (Map.Entry<String, Long> timer : state.timers.entrySet()) {
            timers.add(timer.getKey() + " " + days(timer.getValue()) + " days");
        }
        lines.add("Timers: " + (timers.isEmpty() ? "none" : String.join(", ", timers)));
        addModuleInfo(run, lines);
        return lines;
    }

    private static <S extends Enum<S> & QuestStage, T extends QuestState<S>> void addModuleInfo(QuestManager.Run<S, T> run, List<String> lines) {
        for (QuestModule<S, T> module : run.quest.modules()) {
            List<String> moduleLines = new ArrayList<>();
            module.devInfo(run.context(module), moduleLines);
            for (String line : moduleLines) {
                lines.add(module.getClass().getSimpleName() + ": " + line);
            }
        }
    }

    // Through QuestManager.jump, the path the player's story skip uses.
    public static boolean jump(String questId, String stageName, String ruleId, InteractionDialogAPI dialog) {
        QuestManager.Run<?, ?> run = run(questId);
        if (run == null) return unknownQuest(questId, ruleId, dialog);
        return jump(run, stageName, ruleId, dialog);
    }

    private static <S extends Enum<S> & QuestStage, T extends QuestState<S>> boolean jump(
            QuestManager.Run<S, T> run, String stageName, String ruleId, InteractionDialogAPI dialog) {
        for (S stage : run.quest.stages().getEnumConstants()) {
            if (stage.name().equals(stageName)) {
                QuestManager.get().jump(run.quest, stage);
                return true;
            }
        }
        QuestContext.report(run.id(), ruleId, dialog, "unknown stage " + stageName);
        return false;
    }

    // A jump to the start stage always resets the quest (README "A stage jump").
    public static boolean reset(String questId, String ruleId, InteractionDialogAPI dialog) {
        QuestManager.Run<?, ?> run = run(questId);
        if (run == null) return unknownQuest(questId, ruleId, dialog);
        return reset(run);
    }

    private static <S extends Enum<S> & QuestStage, T extends QuestState<S>> boolean reset(QuestManager.Run<S, T> run) {
        QuestManager.get().jump(run.quest, run.quest.start());
        return true;
    }

    // Sets the flag when it is clear and clears it when it is set; true when the flag is set afterwards.
    public static boolean toggleFlag(String questId, String flagName, String ruleId, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        QuestManager.Run<?, ?> run = run(questId);
        if (run == null) return unknownQuest(questId, ruleId, dialog);
        if (run.state() == null) return noState(questId, ruleId, dialog);
        return toggleFlag(run, flagName, ruleId, dialog, memoryMap);
    }

    private static <S extends Enum<S> & QuestStage, T extends QuestState<S>> boolean toggleFlag(
            QuestManager.Run<S, T> run, String flagName, String ruleId, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        QuestContext<S, T> ctx = new QuestContext<>(run, "dev menu", ruleId, dialog, memoryMap, null);
        for (Enum<?> flag : run.quest.flags().getEnumConstants()) {
            if (!flag.name().equals(flagName)) continue;
            if (ctx.has(flag)) {
                ctx.clear(flag);
            } else {
                ctx.set(flag);
            }
            QuestManager.logInfo(run.id(), "dev menu " + (ctx.has(flag) ? "set " : "cleared ") + flagName);
            return ctx.has(flag);
        }
        ctx.error("unknown flag " + flagName);
        return false;
    }

    // Moves the timer's start back so every wait on it has passed.
    public static boolean expireTimer(String questId, String timer, String ruleId, InteractionDialogAPI dialog) {
        QuestManager.Run<?, ?> run = run(questId);
        if (run == null) return unknownQuest(questId, ruleId, dialog);
        QuestState<?> state = run.state();
        if (state == null) return noState(questId, ruleId, dialog);
        if (!state.timers.containsKey(timer)) {
            QuestContext.report(questId, ruleId, dialog, "unknown timer " + timer);
            return false;
        }
        long now = Global.getSector().getClock().getTimestamp();
        state.timers.put(timer, now - (long) (EXPIRED_TIMER_DAYS * QuestManager.TIMESTAMP_PER_DAY));
        QuestManager.logInfo(questId, "dev menu expired timer " + timer);
        return true;
    }

    private static QuestManager.Run<?, ?> run(String questId) {
        QuestManager manager = QuestManager.get();
        return manager == null ? null : manager.run(questId);
    }

    private static String days(long timestamp) {
        return Misc.getRoundedValueMaxOneAfterDecimal(Global.getSector().getClock().getElapsedDaysSince(timestamp));
    }

    private static boolean unknownQuest(String questId, String ruleId, InteractionDialogAPI dialog) {
        QuestContext.report(questId, ruleId, dialog, "unknown quest");
        return false;
    }

    private static boolean noState(String questId, String ruleId, InteractionDialogAPI dialog) {
        QuestContext.report(questId, ruleId, dialog, "the quest has no state");
        return false;
    }
}
