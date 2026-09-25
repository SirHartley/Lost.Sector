package lostsector.quest;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyDecivListener;
import com.fs.starfarer.api.campaign.listeners.CurrentLocationChangedListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.util.Misc;
import lostsector.ModPlugin;
import lostsector.persistence.Saved;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Transient: ModPlugin.createManagers() builds it on every load and the EFS_LIST loop registers it.
// It is the only writer of quest stages (README "Lifecycle").
public final class QuestManager extends BaseCampaignEventListener
        implements EveryFrameScript, CurrentLocationChangedListener, ColonyDecivListener {

    static final String STORE_KEY = "quests";
    private static final int MAX_QUEUED_CHANGES = 20;
    // Clock timestamps are milliseconds with 86,400,000 per game day: CampaignClock.getElapsedDaysSince divides by 8.64E7.
    private static final long TIMESTAMP_PER_DAY = 86_400_000L;

    private static final Hook DAY = new Hook() {
        @Override
        public <S extends Enum<S> & QuestStage, T extends QuestState<S>> void call(QuestModule<S, T> module, QuestContext<S, T> ctx) {
            module.onDay(ctx);
        }
    };

    // Saved.loadPersistentData() replaces store.val after construction; always read it through the field.
    private final Saved<QuestStore> store;
    private final Map<String, Run<?, ?>> runs = new LinkedHashMap<>();
    private final List<Run<?, ?>> order = new ArrayList<>();
    private final Map<Class<?>, Run<?, ?>> runsByStages = new HashMap<>();
    private final Map<Class<?>, Run<?, ?>> runsByFlags = new HashMap<>();
    private boolean initialized;

    private static final class Change<S> {

        final S from;
        final S to;
        final String source;

        Change(S from, S to, String source) {
            this.from = from;
            this.to = to;
            this.source = source;
        }
    }

    // One module hook call. The method is generic so one instance serves every quest; anonymous classes
    // implement it because a lambda cannot implement a generic method.
    private interface Hook {

        <S extends Enum<S> & QuestStage, T extends QuestState<S>> void call(QuestModule<S, T> module, QuestContext<S, T> ctx);
    }

    // Runtime of one quest: its definition and the transient bookkeeping of stage changes.
    final class Run<S extends Enum<S> & QuestStage, T extends QuestState<S>> {

        final Quest<S, T> quest;
        boolean available;
        boolean changing;
        boolean jumping;
        private final List<Change<S>> queue = new ArrayList<>();
        private final Map<QuestModule<S, T>, QuestContext<S, T>> contexts = new IdentityHashMap<>();

        Run(Quest<S, T> quest) {
            this.quest = quest;
            for (QuestModule<S, T> module : quest.modules()) {
                contexts.put(module, new QuestContext<>(this, module.getClass().getSimpleName(), null, null, null, null));
            }
        }

        String id() {
            return quest.id();
        }

        @SuppressWarnings("unchecked")
        T state() {
            return (T) store.val.states.get(quest.id());
        }

        // One context per module, reused for every hook call; changes it requests are logged with the module as their source.
        QuestContext<S, T> context(QuestModule<S, T> module) {
            return contexts.get(module);
        }

        // README "Events": module order; each module's activity is checked when its turn comes, so a stage
        // change made by an earlier module takes effect at once.
        void deliver(Hook hook) {
            if (!available || state() == null) return;
            List<QuestModule<S, T>> modules = quest.modules();
            for (int i = 0; i < modules.size(); i++) {
                QuestModule<S, T> module = modules.get(i);
                if (module.isActiveIn(state().stage)) hook.call(module, context(module));
            }
        }

        // Kept apart from deliver so a frame allocates nothing.
        void frame(float amount) {
            if (!available || state() == null) return;
            List<QuestModule<S, T>> modules = quest.modules();
            for (int i = 0; i < modules.size(); i++) {
                QuestModule<S, T> module = modules.get(i);
                if (!module.isActiveIn(state().stage)) continue;
                QuestContext<S, T> ctx = context(module);
                if (module.wantsFrames(ctx)) module.onFrame(ctx, amount);
            }
        }

        // Creates a fresh state in the start stage and starts the start stage's modules.
        void start() {
            T state = quest.createState();
            state.questId = quest.id();
            state.seed = Misc.genRandomSeed();
            enter(state, quest.start());
            store.val.states.put(quest.id(), state);
            logInfo(id(), "started in " + quest.start());

            boolean outer = changing;
            changing = true;
            try {
                for (QuestModule<S, T> module : quest.modules()) {
                    if (module.isActiveIn(quest.start())) module.onStart(context(module));
                }
            } finally {
                changing = outer;
            }
        }

        void advance(S from, S to, String source) {
            Change<S> change = new Change<>(from, to, source);
            if (changing) {
                queue.add(change);
                return;
            }
            if (canApply(change)) change(to, source);
            applyQueued();
        }

        private boolean canApply(Change<S> change) {
            T state = state();
            if (state == null) {
                logError(id(), "advance to " + change.to + " (" + change.source + ") skipped: the quest has no state");
                return false;
            }
            if (change.from != null && state.stage != change.from) {
                logError(id(), "advance " + change.from + " -> " + change.to + " (" + change.source + ") skipped: stage is " + state.stage);
                return false;
            }
            if (state.stage == change.to) {
                logError(id(), "advance to " + change.to + " (" + change.source + ") skipped: already in that stage");
                return false;
            }
            return true;
        }

        // README "A stage change", steps 1 to 5. Advances made by the hooks are queued.
        private void change(S to, String source) {
            T state = state();
            S from = state.stage;
            List<QuestModule<S, T>> modules = quest.modules();
            boolean outer = changing;
            changing = true;
            try {
                for (int i = modules.size() - 1; i >= 0; i--) {
                    QuestModule<S, T> module = modules.get(i);
                    if (module.isActiveIn(from) && !module.isActiveIn(to)) module.onStop(context(module));
                }
                clearScoped(state, to);
                enter(state, to);
                logInfo(id(), from + " -> " + to + " (" + source + ")");
                for (QuestModule<S, T> module : modules) {
                    if (module.isActiveIn(to) && !module.isActiveIn(from)) module.onStart(context(module));
                }
                for (QuestModule<S, T> module : modules) {
                    if (module.isActiveIn(to)) module.onStage(context(module), from);
                }
            } finally {
                changing = outer;
            }
        }

        void applyQueued() {
            int applied = 0;
            while (!queue.isEmpty()) {
                if (applied == MAX_QUEUED_CHANGES) {
                    logError(id(), "more than " + MAX_QUEUED_CHANGES + " queued stage changes in a row; dropped " + queue.size());
                    queue.clear();
                    return;
                }
                Change<S> change = queue.remove(0);
                if (canApply(change)) {
                    change(change.to, change.source);
                    applied++;
                }
            }
        }

        // README "A stage jump". Advances queued by the hooks of intermediate stages are dropped, because the
        // jump decides the path; those queued by the target stage apply after the jump.
        void jump(S target) {
            if (changing) {
                logError(id(), "jump to " + target + " skipped: a stage change is running");
                return;
            }
            if (state() == null) {
                logError(id(), "jump to " + target + " skipped: the quest has no state");
                return;
            }
            List<S> path = pathTo(target);
            logInfo(id(), "jump " + state().stage + " -> " + target);
            jumping = true;
            changing = true;
            try {
                int index = path.indexOf(state().stage);
                if (index < 0 || state().stage == target) {
                    reset();
                    index = path.indexOf(quest.start());
                }
                List<S> steps = index < 0 ? List.of(target) : path.subList(index, path.size());
                for (S stage : steps) {
                    if (state().stage != stage) change(stage, "jump");
                    if (stage == target) break;
                    for (QuestModule<S, T> module : quest.modules()) {
                        if (module.isActiveIn(stage)) module.onSkip(context(module));
                    }
                    dropQueued();
                }
            } finally {
                jumping = false;
                changing = false;
            }
            applyQueued();
        }

        private List<S> pathTo(S target) {
            List<S> path = new ArrayList<>();
            for (S stage = target; stage != null; stage = quest.stages().cast(stage.previous())) {
                path.add(0, stage);
            }
            return path;
        }

        private void dropQueued() {
            for (Change<S> change : queue) {
                logInfo(id(), "jump drops advance to " + change.to + " (" + change.source + ")");
            }
            queue.clear();
        }

        // Stops every module and removes what the quest placed in the world, then starts a fresh state.
        private void reset() {
            T state = state();
            List<QuestModule<S, T>> modules = quest.modules();
            for (int i = modules.size() - 1; i >= 0; i--) {
                QuestModule<S, T> module = modules.get(i);
                if (module.isActiveIn(state.stage)) module.onStop(context(module));
            }
            clearScoped(state, null);
            logInfo(id(), "reset from " + state.stage);
            start();
        }

        private void enter(T state, S stage) {
            state.stage = stage;
            state.stageSince = Global.getSector().getClock().getTimestamp();
            state.reached.add(stage.name());
        }

        // Clears marks and claims whose scope does not include the stage; a null stage clears all of them.
        private void clearScoped(T state, S stage) {
            for (QuestState.Mark mark : new ArrayList<>(state.marks)) {
                if (stage == null || !mark.scope.contains(stage.name())) removeMark(state, mark);
            }
            for (QuestState.Claim claim : new ArrayList<>(state.claims)) {
                if (stage == null || !claim.scope.contains(stage.name())) QuestDialogs.release(state, claim);
            }
        }

        void mark(SectorEntityToken entity, PersonAPI person, Set<String> scope) {
            T state = state();
            QuestState.Mark existing = findMark(state, entity, person);
            if (existing != null) {
                existing.scope = scope;
                return;
            }
            if (entity != null) {
                Misc.makeImportant(entity, markReason());
            } else {
                Misc.makeImportant(person, markReason());
            }
            state.marks.add(new QuestState.Mark(entity, person, scope));
        }

        void unmark(SectorEntityToken entity, PersonAPI person) {
            T state = state();
            QuestState.Mark mark = findMark(state, entity, person);
            if (mark != null) removeMark(state, mark);
        }

        private QuestState.Mark findMark(T state, SectorEntityToken entity, PersonAPI person) {
            for (QuestState.Mark mark : state.marks) {
                if (entity != null ? mark.entity == entity : mark.person == person) return mark;
            }
            return null;
        }

        private void removeMark(T state, QuestState.Mark mark) {
            if (mark.entity != null) {
                Misc.makeUnimportant(mark.entity, markReason());
            } else {
                Misc.makeUnimportant(mark.person, markReason());
            }
            state.marks.remove(mark);
        }

        private String markReason() {
            return "nskr_" + quest.id();
        }
    }

    public QuestManager() {
        super(false);
        store = new Saved<>(STORE_KEY, new QuestStore(Global.getSector().getClock().getTimestamp()));
        for (Quest<?, ?> quest : QuestCatalog.create()) {
            register(quest);
        }
    }

    private <S extends Enum<S> & QuestStage, T extends QuestState<S>> void register(Quest<S, T> quest) {
        Run<S, T> run = new Run<>(quest);
        runs.put(quest.id(), run);
        order.add(run);
        runsByStages.put(quest.stages(), run);
        if (quest.flags() != NoFlags.class) runsByFlags.put(quest.flags(), run);
    }

    // The instance of the current load, or null before a campaign is loaded.
    public static QuestManager get() {
        for (BaseCampaignEventListener listener : ModPlugin.EFS_LIST) {
            if (listener instanceof QuestManager) return (QuestManager) listener;
        }
        return null;
    }

    // Retrying pending dialogs follows vanilla's Wait command, which also runs unpaused only and opens its
    // dialog once isShowingDialog() is false; module frames never run paused (README "QuestModule").
    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (!initialized) {
            initialized = true;
            startAvailableQuests();
        }
        deliverDay();
        for (int i = 0; i < order.size(); i++) {
            order.get(i).frame(amount);
        }
        openPendingDialogs();
    }

    // At most one day per frame; days missed during a long fast-forward are delivered on the following frames.
    private void deliverDay() {
        QuestStore quests = store.val;
        if (Global.getSector().getClock().getElapsedDaysSince(quests.lastDay) < 1f) return;
        quests.lastDay += TIMESTAMP_PER_DAY;
        deliver(DAY);
    }

    @Override
    public void reportCurrentLocationChanged(LocationAPI prev, LocationAPI curr) {
        deliver(new Hook() {
            @Override
            public <S extends Enum<S> & QuestStage, T extends QuestState<S>> void call(QuestModule<S, T> module, QuestContext<S, T> ctx) {
                module.onLocationChanged(ctx, prev, curr);
            }
        });
    }

    // Only the completed decivilization is routed (README "Events").
    @Override
    public void reportColonyAboutToBeDecivilized(MarketAPI market, boolean fullyDestroyed) {
    }

    @Override
    public void reportColonyDecivilized(MarketAPI market, boolean fullyDestroyed) {
        deliver(new Hook() {
            @Override
            public <S extends Enum<S> & QuestStage, T extends QuestState<S>> void call(QuestModule<S, T> module, QuestContext<S, T> ctx) {
                module.onDecivilized(ctx, market, fullyDestroyed);
            }
        });
    }

    private void deliver(Hook hook) {
        for (int i = 0; i < order.size(); i++) {
            order.get(i).deliver(hook);
        }
    }

    // Delivers to the listed quests only, in QuestCatalog order; for events owned by some quests, such as a quest fleet's.
    private void deliver(Set<String> questIds, Hook hook) {
        for (int i = 0; i < order.size(); i++) {
            Run<?, ?> run = order.get(i);
            if (questIds.contains(run.id())) run.deliver(hook);
        }
    }

    private void startAvailableQuests() {
        for (Run<?, ?> run : runs.values()) {
            run.available = run.quest.isAvailable();
            if (run.available && run.state() == null) {
                run.start();
                run.applyQueued();
            }
        }
    }

    // One dialog per frame: a successful open makes the UI busy for the rest.
    private void openPendingDialogs() {
        if (Global.getSector().getCampaignUI().isShowingDialog()) return;
        for (Run<?, ?> run : runs.values()) {
            QuestState<?> state = run.state();
            if (!run.available || state == null || state.pendingOpens.isEmpty()) continue;
            if (QuestDialogs.openPending(state)) return;
        }
    }

    public <S extends Enum<S> & QuestStage, T extends QuestState<S>> void jump(Quest<S, T> quest, S target) {
        Run<S, T> run = run(quest);
        if (run == null) {
            logError(quest.id(), "jump to " + target + " skipped: the quest is not in QuestCatalog");
            return;
        }
        run.jump(target);
    }

    // A context carrying the dialog of a rules call. Null when the quest is unknown or has no state.
    public QuestContext<?, ?> context(String questId, String ruleId, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, List<String> args) {
        Run<?, ?> run = runs.get(questId);
        if (run == null) {
            logError(questId, "unknown quest (rule " + ruleId + ")");
            return null;
        }
        if (run.state() == null) {
            logError(questId, "the quest has no state (rule " + ruleId + ")");
            return null;
        }
        return dialogContext(run, ruleId, dialog, memoryMap, args);
    }

    private static <S extends Enum<S> & QuestStage, T extends QuestState<S>> QuestContext<S, T> dialogContext(
            Run<S, T> run, String ruleId, InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap, List<String> args) {
        return new QuestContext<>(run, "rule " + ruleId, ruleId, dialog, memoryMap, args);
    }

    @SuppressWarnings("unchecked")
    private <S extends Enum<S> & QuestStage, T extends QuestState<S>> Run<S, T> run(Quest<S, T> quest) {
        Run<?, ?> run = runs.get(quest.id());
        return run != null && run.quest.stages() == quest.stages() ? (Run<S, T>) run : null;
    }

    Quest<?, ?> quest(String questId) {
        Run<?, ?> run = runs.get(questId);
        return run == null ? null : run.quest;
    }

    QuestState<?> state(String questId) {
        Run<?, ?> run = runs.get(questId);
        return run == null ? null : run.state();
    }

    QuestState<?> stateOf(QuestStage stage) {
        if (!(stage instanceof Enum)) return null;
        Run<?, ?> run = runsByStages.get(((Enum<?>) stage).getDeclaringClass());
        return run == null ? null : run.state();
    }

    QuestState<?> stateOf(Enum<?> flag) {
        if (flag == null) return null;
        Run<?, ?> run = runsByFlags.get(flag.getDeclaringClass());
        return run == null ? null : run.state();
    }

    static void logInfo(String questId, String message) {
        Global.getLogger(QuestManager.class).info("[" + questId + "] " + message);
    }

    static void logError(String questId, String message) {
        Global.getLogger(QuestManager.class).error("[" + questId + "] " + message);
    }
}
