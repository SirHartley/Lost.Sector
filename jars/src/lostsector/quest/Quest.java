package lostsector.quest;

import java.util.List;
import java.util.regex.Pattern;

// Definitions are pure: the constructor, createModules() and declare() must not call Global or any game API,
// because the rules check tool builds them outside the game (README "Quest and stages").
public abstract class Quest<S extends Enum<S> & QuestStage, T extends QuestState<S>> {

    private static final Pattern ID = Pattern.compile("[a-z][a-z0-9]*");

    private final String id;
    private final Class<S> stages;
    private final Class<? extends Enum<?>> flags;
    private final S start;

    private List<QuestModule<S, T>> modules;
    private Declarations<S, T> declarations;

    protected Quest(String id, Class<S> stages, Class<? extends Enum<?>> flags, S start) {
        if (id == null || !ID.matcher(id).matches()) {
            throw new IllegalArgumentException("Quest id must be lowercase letters and digits: " + id);
        }
        if (stages == null || flags == null || start == null) {
            throw new IllegalArgumentException("[" + id + "] stages, flags and start must not be null");
        }
        if (!flags.isEnum()) {
            throw new IllegalArgumentException("[" + id + "] flags must be an enum: " + flags.getName());
        }
        if (start.previous() != null) {
            throw new IllegalArgumentException("[" + id + "] start stage " + start + " must have no previous stage");
        }
        checkStagePaths(id, stages);
        this.id = id;
        this.stages = stages;
        this.flags = flags;
        this.start = start;
    }

    // A previous() chain must stay in the quest's enum and end, or jumps would never finish.
    private static void checkStagePaths(String id, Class<?> stages) {
        Object[] constants = stages.getEnumConstants();
        for (Object constant : constants) {
            QuestStage stage = (QuestStage) constant;
            for (int steps = 0; stage != null; steps++) {
                if (steps > constants.length) {
                    throw new IllegalArgumentException("[" + id + "] previous() of " + constant + " forms a cycle");
                }
                QuestStage previous = stage.previous();
                if (previous != null && !stages.isInstance(previous)) {
                    throw new IllegalArgumentException("[" + id + "] previous() of " + stage + " is not a stage of " + stages.getName());
                }
                stage = previous;
            }
        }
    }

    public final String id() {
        return id;
    }

    public final Class<S> stages() {
        return stages;
    }

    public final Class<? extends Enum<?>> flags() {
        return flags;
    }

    public final S start() {
        return start;
    }

    protected abstract T createState();

    protected abstract List<QuestModule<S, T>> createModules();

    // Checked on the first frame after each load.
    protected boolean isAvailable() {
        return true;
    }

    public final List<QuestModule<S, T>> modules() {
        requireBuilt();
        return modules;
    }

    public final Declarations<S, T> declarations() {
        requireBuilt();
        return declarations;
    }

    // Called once by QuestCatalog.create(), after the subclass constructor has finished.
    final void build() {
        if (declarations != null) return;
        List<QuestModule<S, T>> created = createModules();
        if (created == null) {
            throw new IllegalStateException("[" + id + "] createModules() returned null");
        }
        modules = List.copyOf(created);
        Declarations<S, T> declared = new Declarations<>(id);
        declared.declareAll(modules);
        declarations = declared;
    }

    private void requireBuilt() {
        if (declarations == null) {
            throw new IllegalStateException("[" + id + "] is not built; obtain definitions from QuestCatalog.create()");
        }
    }

    final boolean ownsFlag(Enum<?> flag) {
        return flag != null && flag.getDeclaringClass() == flags;
    }
}
