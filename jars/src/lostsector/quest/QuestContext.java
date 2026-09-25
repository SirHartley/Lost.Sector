package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

// QuestManager keeps one per module for hooks and builds one per rules call; never saved.
public final class QuestContext<S extends Enum<S> & QuestStage, T extends QuestState<S>> {

    // The multiplier vanilla's BarEventManager.getSeed uses to derive seeds from hash codes.
    private static final long SEED_MULT = 181783497276652981L;

    private final QuestManager.Run<S, T> run;
    private final String source;
    private final String ruleId;
    private final InteractionDialogAPI dialog;
    private final Map<String, MemoryAPI> memoryMap;
    private final List<String> args;

    QuestContext(QuestManager.Run<S, T> run, String source, String ruleId, InteractionDialogAPI dialog,
                 Map<String, MemoryAPI> memoryMap, List<String> args) {
        this.run = run;
        this.source = source;
        this.ruleId = ruleId;
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        this.args = args == null ? List.of() : List.copyOf(args);
    }

    public Quest<S, T> quest() {
        return run.quest;
    }

    public T state() {
        return run.state();
    }

    public S stage() {
        return state().stage;
    }

    public boolean isJump() {
        return run.jumping;
    }

    public void advance(S to) {
        run.advance(null, to, source);
    }

    // False and no change when the stage is not `from`.
    public boolean advance(S from, S to) {
        if (stage() != from) {
            error("advance " + from + " -> " + to + " refused: stage is " + stage());
            return false;
        }
        run.advance(from, to, source);
        return true;
    }

    public boolean has(Enum<?> flag) {
        return checkFlag(flag) && state().flags.contains(flag.name());
    }

    public void set(Enum<?> flag) {
        if (checkFlag(flag)) state().flags.add(flag.name());
    }

    public void clear(Enum<?> flag) {
        if (checkFlag(flag)) state().flags.remove(flag.name());
    }

    private boolean checkFlag(Enum<?> flag) {
        if (run.quest.ownsFlag(flag)) return true;
        error("unknown flag " + flag);
        return false;
    }

    // One saved sequence per purpose, so a reload continues it instead of rerolling.
    public Random random(String purpose) {
        T state = state();
        return state.randoms.computeIfAbsent(purpose, key -> new Random(state.seed + key.hashCode() * SEED_MULT));
    }

    public void startTimer(String name) {
        state().timers.put(name, Global.getSector().getClock().getTimestamp());
    }

    public boolean hasTimer(String name) {
        return state().timers.containsKey(name);
    }

    // Days since the timer started; 0 when it has not started.
    public float days(String name) {
        Long start = state().timers.get(name);
        return start == null ? 0f : Global.getSector().getClock().getElapsedDaysSince(start);
    }

    public void clearTimer(String name) {
        state().timers.remove(name);
    }

    // A scope of no stages means the current stage only.
    @SafeVarargs
    public final void mark(SectorEntityToken entity, S... scope) {
        if (entity == null) {
            error("mark of a null entity refused");
            return;
        }
        run.mark(entity, null, scope(scope));
    }

    @SafeVarargs
    public final void mark(PersonAPI person, S... scope) {
        if (person == null) {
            error("mark of a null person refused");
            return;
        }
        run.mark(null, person, scope(scope));
    }

    public void unmark(SectorEntityToken entity) {
        if (entity != null) run.unmark(entity, null);
    }

    public void unmark(PersonAPI person) {
        if (person != null) run.unmark(null, person);
    }

    @SafeVarargs
    public final void claimDialog(SectorEntityToken entity, String trigger, S... scope) {
        if (entity == null) {
            error("claim of " + trigger + " on a null entity refused");
            return;
        }
        if (!isDeclaredTrigger(trigger)) return;
        QuestDialogs.claim(state(), entity, trigger, scope(scope));
    }

    public void releaseDialog(SectorEntityToken entity) {
        if (entity != null) QuestDialogs.release(state(), entity);
    }

    public void open(SectorEntityToken target, String trigger) {
        if (target == null) {
            error("open of " + trigger + " on a null target refused");
            return;
        }
        if (!isDeclaredTrigger(trigger)) return;
        QuestDialogs.open(state(), target, trigger);
    }

    private boolean isDeclaredTrigger(String trigger) {
        if (run.quest.declarations().triggers().contains(trigger)) return true;
        error("trigger " + trigger + " is not declared");
        return false;
    }

    @SafeVarargs
    private Set<String> scope(S... stages) {
        Set<String> scope = new LinkedHashSet<>();
        if (stages.length == 0) {
            scope.add(stage().name());
        }
        for (S stage : stages) {
            scope.add(stage.name());
        }
        return scope;
    }

    public QuestFleets fleets() {
        return run.fleets;
    }

    public InteractionDialogAPI dialog() {
        return dialog;
    }

    public Map<String, MemoryAPI> memoryMap() {
        return memoryMap;
    }

    public SectorEntityToken target() {
        return dialog == null ? null : dialog.getInteractionTarget();
    }

    public TextPanelAPI textPanel() {
        return dialog == null ? null : dialog.getTextPanel();
    }

    // Extra rules arguments; empty from Java.
    public List<String> args() {
        return args;
    }

    public void log(String message) {
        QuestManager.logInfo(run.id(), message);
    }

    // Errors from rules or state never throw (README "Errors and logging").
    void error(String message) {
        String line = ruleId == null ? message : message + " (rule " + ruleId + ")";
        QuestManager.logError(run.id(), line);
        TextPanelAPI panel = textPanel();
        if (panel != null && Global.getSettings().isDevMode()) {
            panel.addPara("[" + run.id() + "] " + line, Misc.getNegativeHighlightColor());
        }
    }
}
