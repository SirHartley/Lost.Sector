package lostsector.quest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

// Filled once while the definition is built; errors throw so the first dev run finds them.
// Names are unique per kind within the quest, because rules look each kind up separately.
public final class Declarations<S extends Enum<S> & QuestStage, T extends QuestState<S>> {

    private static final Pattern NAME = Pattern.compile("[a-z][A-Za-z0-9]*");
    private static final Pattern TRIGGER = Pattern.compile("\\S+");

    private final String questId;

    private final Map<String, Predicate<QuestContext<S, T>>> checks = new LinkedHashMap<>();
    private final Map<String, Consumer<QuestContext<S, T>>> actions = new LinkedHashMap<>();
    private final Map<String, QuestModule<S, T>> actionModules = new HashMap<>();
    private final Map<String, Function<QuestContext<S, T>, String>> tokens = new LinkedHashMap<>();
    private final Map<String, FleetRole> roles = new LinkedHashMap<>();
    private final Map<String, QuestModule<S, T>> roleModules = new HashMap<>();
    private final Set<String> triggers = new LinkedHashSet<>();
    private final Set<String> people = new LinkedHashSet<>();
    private final Map<String, IntelSpec> intels = new LinkedHashMap<>();
    private final Map<String, String> raids = new LinkedHashMap<>();

    private QuestModule<S, T> declaring;
    private boolean sealed;

    Declarations(String questId) {
        this.questId = questId;
    }

    void declareAll(List<QuestModule<S, T>> modules) {
        for (QuestModule<S, T> module : modules) {
            declaring = module;
            module.declare(this);
        }
        declaring = null;
        sealed = true;
        for (IntelSpec intel : intels.values()) {
            intel.freeze();
            String check = intel.deletableCheckOrNull();
            if (check != null && !checks.containsKey(check)) {
                throw new IllegalStateException("[" + questId + "] check " + check + " of intel " + intel.key + " is not declared");
            }
        }
        for (Map.Entry<String, FleetRole> role : roles.entrySet()) {
            String trigger = role.getValue().defeatTriggerName();
            if (trigger != null && !triggers.contains(trigger)) {
                throw new IllegalStateException("[" + questId + "] defeat trigger " + trigger + " of role " + role.getKey() + " is not declared");
            }
        }
        for (Map.Entry<String, String> raid : raids.entrySet()) {
            if (!actions.containsKey(raid.getValue())) {
                throw new IllegalStateException("[" + questId + "] action " + raid.getValue() + " of raid " + raid.getKey() + " is not declared");
            }
        }
    }

    public void check(String name, Predicate<QuestContext<S, T>> check) {
        put(checks, "check", name, check);
    }

    public void action(String name, Consumer<QuestContext<S, T>> action) {
        put(actions, "action", name, action);
        actionModules.put(name, declaring);
    }

    public void token(String name, Function<QuestContext<S, T>, String> token) {
        put(tokens, "token", name, token);
    }

    // Fleets of the role are despawned when the declaring module stops, unless the role is persistent().
    public void role(String name, FleetRole role) {
        put(roles, "role", name, role);
        roleModules.put(name, declaring);
    }

    public void trigger(String trigger) {
        requireOpen();
        if (trigger == null || !TRIGGER.matcher(trigger).matches()) {
            throw new IllegalArgumentException("[" + questId + "] invalid trigger '" + trigger + "'");
        }
        if (!triggers.add(trigger)) {
            throw new IllegalArgumentException("[" + questId + "] duplicate trigger " + trigger);
        }
    }

    // A quest person key; QuestPeople creates people only under declared keys, and the rules check tool reads the
    // keys to check person tokens $nskr_<q>_<key>_<suffix>.
    public void person(String key) {
        requireOpen();
        if (key == null || !NAME.matcher(key).matches()) {
            throw new IllegalArgumentException("[" + questId + "] person key must be lowerCamel: " + key);
        }
        if (!people.add(key)) {
            throw new IllegalArgumentException("[" + questId + "] duplicate person " + key);
        }
    }

    // An intel entry that QuestIntels shows; its text comes from the quest's intel rows (README "Intel"). The returned
    // spec takes the entry's presentation options.
    public IntelSpec intel(String key, String icon, String... tags) {
        if (icon == null || icon.isBlank() || tags == null || Arrays.asList(tags).contains(null)) {
            throw new IllegalArgumentException("[" + questId + "] intel " + key + " needs an icon and non-null tags");
        }
        IntelSpec spec = new IntelSpec(questId, key, icon, List.of(tags));
        put(intels, "intel", key, spec);
        return spec;
    }

    // A raid objective that QuestContext.raidObjective builds; its name, tooltip and result lines come from the quest's
    // raid rows and a successful raid runs the declared action (README "Raid objectives").
    public void raid(String key, String action) {
        put(raids, "raid", key, action);
    }

    public Map<String, Predicate<QuestContext<S, T>>> checks() {
        return Collections.unmodifiableMap(checks);
    }

    public Map<String, Consumer<QuestContext<S, T>>> actions() {
        return Collections.unmodifiableMap(actions);
    }

    // The module that declared the action; the action runs only while that module is active.
    public QuestModule<S, T> actionModule(String name) {
        return actionModules.get(name);
    }

    public Map<String, Function<QuestContext<S, T>, String>> tokens() {
        return Collections.unmodifiableMap(tokens);
    }

    public Map<String, FleetRole> roles() {
        return Collections.unmodifiableMap(roles);
    }

    public QuestModule<S, T> roleModule(String name) {
        return roleModules.get(name);
    }

    public Set<String> triggers() {
        return Collections.unmodifiableSet(triggers);
    }

    public Set<String> people() {
        return Collections.unmodifiableSet(people);
    }

    public Set<String> intels() {
        return Collections.unmodifiableSet(intels.keySet());
    }

    public Set<String> raids() {
        return Collections.unmodifiableSet(raids.keySet());
    }

    // The action a successful raid of the key runs; null for an undeclared key.
    public String raidAction(String key) {
        return raids.get(key);
    }

    IntelSpec intelDeclaration(String key) {
        return intels.get(key);
    }

    private <V> void put(Map<String, V> map, String kind, String name, V value) {
        requireOpen();
        if (name == null || !NAME.matcher(name).matches()) {
            throw new IllegalArgumentException("[" + questId + "] " + kind + " name must be lowerCamel: " + name);
        }
        if (value == null) {
            throw new IllegalArgumentException("[" + questId + "] " + kind + " " + name + " is null");
        }
        if (map.containsKey(name)) {
            throw new IllegalArgumentException("[" + questId + "] duplicate " + kind + " " + name);
        }
        map.put(name, value);
    }

    private void requireOpen() {
        if (sealed || declaring == null) {
            throw new IllegalStateException("[" + questId + "] declarations are made only in QuestModule.declare");
        }
    }
}
