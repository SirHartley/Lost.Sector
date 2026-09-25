package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.graid.GroundRaidObjectivePlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
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
    private final SectorEntityToken tokenTarget;
    private QuestRewards rewards;
    private QuestIntels intels;

    QuestContext(QuestManager.Run<S, T> run, String source, String ruleId, InteractionDialogAPI dialog,
                 Map<String, MemoryAPI> memoryMap, List<String> args) {
        this(run, source, ruleId, dialog, memoryMap, args, null);
    }

    // tokenTarget: for a token context, the entity the engine passes to the token generator, which is the dialog's
    // interaction target for dialog text (FireBest/FireAll addText, Misc.Token.getStringWithTokenReplacement).
    QuestContext(QuestManager.Run<S, T> run, String source, String ruleId, InteractionDialogAPI dialog,
                 Map<String, MemoryAPI> memoryMap, List<String> args, SectorEntityToken tokenTarget) {
        this.run = run;
        this.source = source;
        this.ruleId = ruleId;
        this.dialog = dialog;
        this.memoryMap = memoryMap;
        this.args = args == null ? List.of() : List.copyOf(args);
        this.tokenTarget = tokenTarget;
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
        return random(state(), purpose);
    }

    static Random random(QuestState<?> state, String purpose) {
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
        run.mark(entity, null, null, scope(scope));
    }

    @SafeVarargs
    public final void mark(PersonAPI person, S... scope) {
        if (person == null) {
            error("mark of a null person refused");
            return;
        }
        run.mark(null, person, null, scope(scope));
    }

    // Marks the market's own memory; Misc.doesMarketHaveMissionImportantPeopleOrIsMarketMissionImportant reads it for
    // the market's primary entity.
    @SafeVarargs
    public final void mark(MarketAPI market, S... scope) {
        if (market == null) {
            error("mark of a null market refused");
            return;
        }
        run.mark(null, null, market, scope(scope));
    }

    public void unmark(SectorEntityToken entity) {
        if (entity != null) run.unmark(entity, null, null);
    }

    public void unmark(PersonAPI person) {
        if (person != null) run.unmark(null, person, null);
    }

    public void unmark(MarketAPI market) {
        if (market != null) run.unmark(null, null, market);
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

    // Hands the open dialog to a rules dialog on the target that fires the trigger, as the engage verb hands it to a
    // fleet encounter: the target and plugin are replaced and the new plugin's init fires the trigger at once. For a
    // Java dialog, such as a fleet encounter delegate, that continues with a quest scene without closing the window.
    public void continueDialog(SectorEntityToken target, String trigger) {
        if (dialog == null || target == null) {
            error("continue with " + trigger + " refused: no dialog or no target");
            return;
        }
        if (!isDeclaredTrigger(trigger)) return;
        InteractionDialogPlugin plugin = QuestDialogs.plugin(trigger);
        dialog.setInteractionTarget(target);
        dialog.setPlugin(plugin);
        plugin.init(dialog);
        QuestManager.logInfo(run.id(), "continued the dialog with " + trigger + " on " + target.getId());
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

    public QuestPeople people() {
        return run.people;
    }

    // Bound to this context's dialog, which decides where new entries and updates are printed.
    public QuestIntels intel() {
        if (intels == null) intels = new QuestIntels(this);
        return intels;
    }

    // A raid objective declared with d.raid, for onRaidObjectives to add; null (logged) for an undeclared key.
    // iconCommodityId: the commodity whose icon the raid menu shows; null for none.
    public GroundRaidObjectivePlugin raidObjective(String key, MarketAPI market, RaidDangerLevel danger, String iconCommodityId) {
        if (run.quest.declarations().raidAction(key) == null) {
            error("raid " + key + " is not declared");
            return null;
        }
        return new QuestRaidObjective(run.id(), key, market, danger, iconCommodityId);
    }

    // Bound to this context's dialog, which decides whether receipts are printed.
    public QuestRewards rewards() {
        if (rewards == null) rewards = new QuestRewards(this);
        return rewards;
    }

    public InteractionDialogAPI dialog() {
        return dialog;
    }

    public Map<String, MemoryAPI> memoryMap() {
        return memoryMap;
    }

    // The dialog target; in a token, the entity whose text is being replaced; null otherwise.
    public SectorEntityToken target() {
        return dialog == null ? tokenTarget : dialog.getInteractionTarget();
    }

    public TextPanelAPI textPanel() {
        return dialog == null ? null : dialog.getTextPanel();
    }

    // The record of the intel entry whose rows are being matched or replaced (QuestText.RECORD in the scratch local
    // memory); null for an entry without a record and outside intel text.
    public String intelRecord() {
        MemoryAPI local = memoryMap == null ? null : memoryMap.get(MemKeys.LOCAL);
        String record = local == null ? null : local.getString(QuestText.RECORD);
        return record == null || record.isEmpty() ? null : record;
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
        report(run.id(), ruleId, dialog, message);
    }

    static void report(String questId, String ruleId, InteractionDialogAPI dialog, String message) {
        String line = ruleId == null ? message : message + " (rule " + ruleId + ")";
        QuestManager.logError(questId, line);
        TextPanelAPI panel = dialog == null ? null : dialog.getTextPanel();
        if (panel != null && Global.getSettings().isDevMode()) {
            panel.addPara("[" + questId + "] " + line, Misc.getNegativeHighlightColor());
        }
    }
}
