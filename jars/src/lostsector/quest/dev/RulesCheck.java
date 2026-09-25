package lostsector.quest.dev;

import lostsector.quest.Declarations;
import lostsector.quest.Quest;
import lostsector.quest.QuestCatalog;
import lostsector.quest.QuestFleets;
import lostsector.quest.dev.RuleExpression.Operator;
import lostsector.quest.dev.RuleExpression.Token;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// Static checks of data/campaign/rules.csv against the engine's loader and the quest framework's rules contract.
// Runs outside the game: it must not touch Global, Misc or any class whose static initializer needs the game.
// Usage and the list of checks are in the quest framework README, "Rules check tool".
public final class RulesCheck {

    enum Severity {
        ERROR,
        WARN
    }

    private static final String RULES = "data/campaign/rules.csv";
    private static final String SETTINGS = "data/config/settings.json";
    private static final String JAVA_SOURCES = "jars/src";
    private static final String OWN_SOURCES = "lostsector/quest/dev";

    // Scratch keys QuestText writes before it matches intel rows (README "Intel") and the fleet memory keys QuestFleets
    // writes on spawn; role flags $nskr_<q>_<role> are added per declared role.
    private static final Set<String> FRAMEWORK_KEYS = Set.of("$nskr_intel_key", "$nskr_intel_status", "$nskr_intel_update", "$nskr_intel_mode",
            QuestFleets.OWNER_KEY, QuestFleets.ROLE_KEY, QuestFleets.RECORD_KEY);
    // QuestText fires these for every quest; the last two select every matching row (README "Intel").
    private static final List<String> INTEL_SUFFIXES = List.of("IntelTitle", "IntelBullets", "IntelDesc");
    private static final Set<String> INTEL_ALL_SUFFIXES = Set.of("IntelBullets", "IntelDesc");
    // QuestTokens adds these for each quest person: $nskr_<q>_<key>_<suffix> (README "People").
    private static final Set<String> PERSON_TOKEN_SUFFIXES = Set.of("name", "heOrShe", "HeOrShe", "himOrHer", "HimOrHer", "hisOrHer", "HisOrHer");

    private static final String QUEST_COMMAND = "nskr_quest";
    private static final Set<String> CONDITION_VERBS = Set.of("is", "reached", "flag", "check");
    private static final Set<String> SCRIPT_VERBS = Set.of("advance", "set", "clear", "do");
    // Their first argument names a key to remove or re-time; it is not a read.
    private static final Set<String> KEY_COMMANDS = Set.of("unset", "unsetAll", "expire");
    private static final String OPTION_PREFIX_COMMAND = "nskr_optionStartsWith";

    // Quest ids have no underscore, so group 1 is the whole id.
    private static final Pattern QUEST_TOKEN = Pattern.compile("\\$nskr_([a-z][a-z0-9]*)_([A-Za-z0-9_]+)");
    private static final Pattern NSKR_KEY = Pattern.compile("\\$(?:[A-Za-z]+\\.)?(nskr_[A-Za-z0-9_]+)");
    private static final Pattern JAVA_STRING = Pattern.compile("\"((?:[^\"\\\\\\n]|\\\\.)*)\"");
    private static final Pattern COMMAND_PACKAGES = Pattern.compile("\"ruleCommandPackages\"\\s*:\\s*\\[([^\\]]*)\\]");
    private static final Pattern QUOTED = Pattern.compile("\"([^\"]+)\"");

    private final Path root;
    private final VanillaRules vanilla;
    private final List<Finding> findings = new ArrayList<>();

    private final Map<String, QuestInfo> quests = new LinkedHashMap<>();
    private final Set<String> javaStrings = new HashSet<>();
    private final Set<String> javaKeys = new HashSet<>();
    private final List<String> commandPackages = new ArrayList<>(VanillaRules.COMMAND_PACKAGES);
    private final Map<String, Boolean> commands = new HashMap<>();

    private final List<ParsedRow> rows = new ArrayList<>();

    record Finding(Severity severity, String check, int line, String ruleId, String message) {
    }

    // A role's defeat trigger is among the triggers: Declarations refuses a defeat trigger that is not declared.
    record QuestInfo(String id, Set<String> stages, Set<String> flags, Set<String> checks, Set<String> actions,
                     Set<String> tokens, Set<String> triggers, Set<String> roles, Set<String> people) {

        static QuestInfo of(Quest<?, ?> quest) {
            Declarations<?, ?> d = quest.declarations();
            return new QuestInfo(quest.id(), names(quest.stages()), names(quest.flags()), Set.copyOf(d.checks().keySet()),
                    Set.copyOf(d.actions().keySet()), new TreeSet<>(d.tokens().keySet()), Set.copyOf(d.triggers()),
                    Set.copyOf(d.roles().keySet()), new TreeSet<>(d.people()));
        }

        private static Set<String> names(Class<? extends Enum<?>> type) {
            Set<String> names = new LinkedHashSet<>();
            for (Enum<?> constant : type.getEnumConstants()) {
                names.add(constant.name());
            }
            return names;
        }
    }

    record ParsedRow(RulesFile.Row row, List<RuleExpression> conditions, List<RuleExpression> script, List<RulesFile.Option> options) {

        static ParsedRow of(RulesFile.Row row) {
            return new ParsedRow(row, parse(row.conditions()), parse(row.script()), RulesFile.options(row.options()));
        }

        private static List<RuleExpression> parse(String cell) {
            List<RuleExpression> lines = new ArrayList<>();
            for (String line : RulesFile.lines(cell)) {
                lines.add(RuleExpression.parse(line));
            }
            return lines;
        }

        String id() {
            return row.id();
        }

        String trigger() {
            return row.trigger();
        }

        int line() {
            return row.line();
        }

        List<RuleExpression> all() {
            List<RuleExpression> all = new ArrayList<>(conditions);
            all.addAll(script);
            return all;
        }
    }

    private RulesCheck(Path root, VanillaRules vanilla) {
        this.root = root;
        this.vanilla = vanilla;
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out));
    }

    // 0: no errors, 1: errors found, 2: usage or input problem.
    static int run(String[] args, PrintStream out) {
        try {
            if (args.length == 3 && args[0].equals("--index")) {
                VanillaRules.fromCsv(Path.of(args[1])).write(out, args[2]);
                return 0;
            }
            if (args.length < 1 || args.length > 2 || args[0].startsWith("--")) {
                System.err.println("usage: RulesCheck <repository root> [<vanilla rules.csv>]");
                System.err.println("       RulesCheck --index <vanilla rules.csv> <game version>");
                return 2;
            }
            Path root = Path.of(args[0]);
            VanillaRules vanilla = args.length == 2
                    ? VanillaRules.fromCsv(Path.of(args[1]))
                    : VanillaRules.fromIndex(root.resolve(VanillaRules.INDEX));
            RulesCheck check = new RulesCheck(root, vanilla);
            check.check();
            return check.report(out);
        } catch (IOException e) {
            System.err.println("RulesCheck: " + e);
            return 2;
        }
    }

    private void check() throws IOException {
        loadQuests();
        scanJavaSources();
        readCommandPackages();
        RulesFile file = RulesFile.read(root.resolve(RULES));
        if (file.error != null) {
            add(Severity.ERROR, "csv", 0, "-", file.error);
        }
        if (!readRows(file)) return;
        checkLoading();
        checkQuestCalls();
        checkTokens();
        checkOptionTokens();
        checkTriggers();
        checkOptionHandlers();
        checkCase();
        checkKeys();
        checkIdenticalConditions();
        checkIntelRows();
        checkQuestNaming();
        checkDeclarations();
    }

    // Loading

    private void loadQuests() {
        List<Quest<?, ?>> catalog;
        try {
            catalog = QuestCatalog.create();
        } catch (RuntimeException | LinkageError e) {
            add(Severity.ERROR, "definitions", 0, "QuestCatalog",
                    "building the definitions failed; they must not call the game (README \"Definitions are pure\"): " + e);
            return;
        }
        for (Quest<?, ?> quest : catalog) {
            quests.put(quest.id(), QuestInfo.of(quest));
        }
    }

    // String literals of the mod's Java code stand in for triggers and keys that legacy code fires or writes.
    private void scanJavaSources() throws IOException {
        Path sources = root.resolve(JAVA_SOURCES);
        Path own = sources.resolve(OWN_SOURCES);
        List<Path> files;
        try (Stream<Path> walk = Files.walk(sources)) {
            files = walk.filter(p -> p.toString().endsWith(".java") && !p.startsWith(own)).toList();
        }
        for (Path file : files) {
            Matcher literal = JAVA_STRING.matcher(Files.readString(file, StandardCharsets.UTF_8));
            while (literal.find()) {
                String value = literal.group(1);
                javaStrings.add(value);
                Matcher key = NSKR_KEY.matcher(value);
                while (key.find()) {
                    javaKeys.add("$" + key.group(1));
                }
            }
        }
    }

    private void readCommandPackages() throws IOException {
        Matcher list = COMMAND_PACKAGES.matcher(Files.readString(root.resolve(SETTINGS), StandardCharsets.UTF_8));
        if (!list.find()) return;
        Matcher name = QUOTED.matcher(list.group(1));
        while (name.find()) {
            commandPackages.add(name.group(1));
        }
    }

    private boolean readRows(RulesFile file) {
        if (file.records.isEmpty() || !file.records.get(0).fields().equals(RulesFile.HEADER)) {
            add(Severity.ERROR, "columns", 1, "-", "the header must be " + String.join(",", RulesFile.HEADER));
            return false;
        }
        for (RulesFile.Record record : file.records.subList(1, file.records.size())) {
            String first = record.fields().get(0);
            if (record.fields().size() != RulesFile.HEADER.size()) {
                add(Severity.ERROR, "columns", record.line(), first.isEmpty() ? "-" : first,
                        "has " + record.fields().size() + " columns; rows have " + RulesFile.HEADER.size());
                continue;
            }
            RulesFile.Row row = RulesFile.Row.of(record);
            if (row.id().isBlank()) {
                if (!record.isBlank()) {
                    add(Severity.WARN, "empty-id", record.line(), "-", "the row has content but no id; the loader skips it");
                }
                continue;
            }
            if (row.isLoaded()) rows.add(ParsedRow.of(row));
        }
        Map<String, ParsedRow> seen = new HashMap<>();
        for (ParsedRow row : rows) {
            ParsedRow earlier = seen.putIfAbsent(row.id(), row);
            if (earlier == null) continue;
            if (earlier.trigger().equals(row.trigger())) {
                add(Severity.ERROR, "duplicate-id", row, "the id is also used at line " + earlier.line() + " under the same trigger; the file fails to load");
            } else {
                add(Severity.WARN, "duplicate-id", row, "the id is also used at line " + earlier.line() + " under trigger " + earlier.trigger());
            }
        }
        return true;
    }

    // Engine load errors and CSV cell rules

    private void checkLoading() {
        for (ParsedRow row : rows) {
            Set<String> missing = new TreeSet<>();
            for (RuleExpression e : row.conditions()) {
                checkExpression(row, e, "Conditions", missing);
                if (e.error == null && e.operator == Operator.ASSIGN) {
                    add(Severity.ERROR, "load", row, "assignment in Conditions: " + e.source + "; the file fails to load");
                }
                if (e.isCommand("FireAll") || e.isCommand("FireBest")) {
                    add(Severity.ERROR, "fire-in-conditions", row, e.command + " in Conditions runs text, options and script while rows are matched");
                }
            }
            for (RuleExpression e : row.script()) {
                checkExpression(row, e, "Script", missing);
                if (e.error == null && e.operator == Operator.EQUAL) {
                    add(Severity.ERROR, "load", row, "== in Script: " + e.source + "; the file fails to load");
                }
            }
            for (RulesFile.Option option : row.options()) {
                if (option.line().isBlank()) {
                    add(Severity.ERROR, "whitespace", row, "an Options line holds only spaces; the file fails to load");
                } else if (option.error() != null) {
                    add(Severity.ERROR, "option-format", row, "option \"" + option.line() + "\": " + option.error());
                } else if (option.id().startsWith("$")) {
                    add(Severity.ERROR, "option-format", row, "option id " + option.id() + " starts with $; the file fails to load");
                }
            }
            for (String command : missing) {
                add(Severity.ERROR, "command", row, "command " + command + " is not a class in the rule command packages; the file fails to load");
            }
            if (row.row().text().indexOf('\r') >= 0) {
                add(Severity.WARN, "text-cr", row, "Text holds a carriage return, which stops OR variants from splitting");
            }
        }
    }

    private void checkExpression(ParsedRow row, RuleExpression e, String column, Set<String> missingCommands) {
        if (e.error != null) {
            boolean blank = e.source.isEmpty();
            add(Severity.ERROR, blank ? "whitespace" : "load", row,
                    blank ? "a " + column + " line holds only spaces; the file fails to load"
                            : column + " line \"" + e.source + "\": " + e.error + "; the file fails to load");
        } else if (e.command != null && !commandExists(e.command)) {
            missingCommands.add(e.command);
        }
    }

    // The engine resolves a command as <package>.<name> over ruleCommandPackages. Only the class file is looked up,
    // so no class is initialized.
    private boolean commandExists(String name) {
        return commands.computeIfAbsent(name, n -> {
            ClassLoader loader = RulesCheck.class.getClassLoader();
            for (String pkg : commandPackages) {
                if (loader.getResource(pkg.replace('.', '/') + "/" + n + ".class") != null) return true;
            }
            return false;
        });
    }

    // nskr_quest calls

    private void checkQuestCalls() {
        for (ParsedRow row : rows) {
            for (RuleExpression e : row.conditions()) {
                if (e.isCommand(QUEST_COMMAND)) checkQuestCall(row, e, true);
            }
            for (RuleExpression e : row.script()) {
                if (e.isCommand(QUEST_COMMAND)) checkQuestCall(row, e, false);
            }
        }
    }

    private void checkQuestCall(ParsedRow row, RuleExpression e, boolean condition) {
        List<Token> params = e.params;
        String call = e.source;
        if (params.size() < 2) {
            add(Severity.ERROR, "quest-call", row, call + ": needs a quest id and a verb");
            return;
        }
        for (Token token : params) {
            if (token.isVariable()) {
                add(Severity.WARN, "quest-call", row, call + ": " + token.text() + " is read from memory, so it is not checked");
                return;
            }
        }
        String questId = params.get(0).text();
        String verb = params.get(1).text();
        List<String> args = new ArrayList<>();
        for (Token token : params.subList(2, params.size())) {
            args.add(token.text());
        }
        QuestInfo quest = quests.get(questId);
        if (quest == null) {
            add(Severity.ERROR, "quest-call", row, call + ": unknown quest id " + questId);
            return;
        }
        if (!CONDITION_VERBS.contains(verb) && !SCRIPT_VERBS.contains(verb)) {
            add(Severity.ERROR, "quest-call", row, call + ": unknown verb " + verb);
            return;
        }
        if (condition != CONDITION_VERBS.contains(verb)) {
            add(Severity.ERROR, "quest-call", row, call + ": " + verb + " belongs in " + (condition ? "Script" : "Conditions"));
        }
        switch (verb) {
            case "is" -> {
                if (args.isEmpty()) add(Severity.ERROR, "quest-call", row, call + ": is needs at least one stage");
                checkNames(row, call, "stage", args, quest.stages());
            }
            case "reached" -> checkOne(row, call, "stage", args, quest.stages());
            case "flag", "set", "clear" -> checkOne(row, call, "flag", args, quest.flags());
            case "check" -> checkDeclared(row, call, "check", args, quest.checks());
            case "do" -> checkDeclared(row, call, "action", args, quest.actions());
            case "advance" -> {
                if (args.size() != 2) {
                    add(Severity.ERROR, "quest-call", row, call + ": advance needs FROM and TO stages");
                    return;
                }
                checkNames(row, call, "stage", args, quest.stages());
                if (args.get(0).equals(args.get(1))) {
                    add(Severity.ERROR, "advance", row, call + ": advances from a stage to itself");
                }
            }
            default -> throw new IllegalStateException(verb);
        }
    }

    private void checkOne(ParsedRow row, String call, String kind, List<String> args, Set<String> known) {
        if (args.size() != 1) {
            add(Severity.ERROR, "quest-call", row, call + ": needs exactly one " + kind);
            return;
        }
        checkNames(row, call, kind, args, known);
    }

    private void checkDeclared(ParsedRow row, String call, String kind, List<String> args, Set<String> known) {
        if (args.isEmpty()) {
            add(Severity.ERROR, "quest-call", row, call + ": needs a " + kind + " name");
            return;
        }
        checkNames(row, call, kind, args.subList(0, 1), known);
    }

    private void checkNames(ParsedRow row, String call, String kind, List<String> names, Set<String> known) {
        for (String name : names) {
            if (!known.contains(name)) add(Severity.ERROR, "quest-call", row, call + ": unknown " + kind + " " + name);
        }
    }

    // Quest tokens

    private void checkTokens() {
        for (ParsedRow row : rows) {
            Set<String> shown = new LinkedHashSet<>(questTokens(row.row().text()));
            for (RulesFile.Option option : row.options()) {
                if (option.error() == null) shown.addAll(questTokens(option.text()));
            }
            for (RuleExpression e : row.script()) {
                for (Token token : e.params) {
                    if (!token.isVariable()) shown.addAll(questTokens(token.text()));
                }
            }
            for (String token : shown) {
                Matcher m = QUEST_TOKEN.matcher(token);
                if (!m.matches()) continue;
                QuestInfo quest = quests.get(m.group(1));
                if (!quest.tokens().contains(m.group(2)) && !isPersonToken(quest, m.group(2))) {
                    add(Severity.ERROR, "token", row, "unknown token " + token);
                }
                if (!row.id().startsWith("nskr_" + quest.id() + "_")) {
                    add(Severity.ERROR, "token", row, token + " is replaced only in rows whose id starts with nskr_" + quest.id() + "_");
                }
            }
            for (RuleExpression e : row.all()) {
                for (Token token : VanillaRules.tokens(e)) {
                    if (!token.isVariable() || !isDeclaredToken(token.key())) continue;
                    boolean assigned = token == e.first && isWrite(e);
                    add(Severity.ERROR, assigned ? "token-assign" : "token", row, assigned
                            ? "assigns " + token.key() + ", which is a token name"
                            : "reads " + token.key() + " as memory; tokens are display values and never in memory");
                }
            }
        }
    }

    // The engine replaces tokens in Options-column labels with the id of the rule that fired the row's trigger, not the
    // row's own id: RuleBasedInteractionDialogPluginImpl fires its initial trigger and DialogOptionSelected with
    // FireBest.fire(null, ...), and FireAll/FireBest pass their caller's id to OptionAdder.add. Quest tokens reach an
    // option label only when every source of the trigger is a FireAll/FireBest in a row of the same quest (README "Tokens").
    private void checkOptionTokens() {
        Map<String, Set<String>> firingQuests = new HashMap<>();
        for (ParsedRow row : rows) {
            for (RuleExpression e : row.all()) {
                String target = fireTarget(e);
                if (target == null) continue;
                String quest = rowQuest(row);
                firingQuests.computeIfAbsent(target, t -> new HashSet<>()).add(quest == null ? "" : quest);
            }
        }
        Set<String> declared = declaredTriggers();
        for (ParsedRow row : rows) {
            Set<String> tokens = new LinkedHashSet<>();
            for (RulesFile.Option option : row.options()) {
                if (option.error() == null) tokens.addAll(questTokens(option.text()));
            }
            for (String token : tokens) {
                Matcher m = QUEST_TOKEN.matcher(token);
                if (!m.matches()) continue;
                String reason = optionRuleIdProblem(row.trigger(), m.group(1), firingQuests, declared);
                if (reason != null) add(Severity.ERROR, "option-token", row, token + " in an option label is not replaced: " + reason);
            }
        }
    }

    private String optionRuleIdProblem(String trigger, String quest, Map<String, Set<String>> firingQuests, Set<String> declared) {
        if (VanillaRules.OPTION_TRIGGERS.contains(trigger)) {
            return "the dialog applies " + trigger + " rows with rule id null; fire a private option trigger from this row";
        }
        if (declared.contains(trigger)) return "Java opens or fires " + trigger + " with rule id null";
        if (VanillaRules.engineFires(trigger) || vanilla.triggers().contains(trigger)
                || vanilla.fireAll().contains(trigger) || vanilla.fireBest().contains(trigger)) {
            return "the engine or vanilla rows fire " + trigger + ", passing a rule id of another feature or null";
        }
        Set<String> firing = firingQuests.getOrDefault(trigger, Set.of());
        if (!firing.equals(Set.of(quest))) {
            return trigger + " is not fired only by FireAll or FireBest in rows of quest " + quest;
        }
        return null;
    }

    // Tokens of known quests; $nskr_<x>_ names of other features are legacy memory keys.
    private List<String> questTokens(String text) {
        List<String> found = new ArrayList<>();
        Matcher m = QUEST_TOKEN.matcher(text);
        while (m.find()) {
            if (quests.containsKey(m.group(1))) found.add(m.group());
        }
        return found;
    }

    private static boolean isPersonToken(QuestInfo quest, String name) {
        int split = name.lastIndexOf('_');
        return split > 0 && PERSON_TOKEN_SUFFIXES.contains(name.substring(split + 1)) && quest.people().contains(name.substring(0, split));
    }

    private boolean isDeclaredToken(String key) {
        Matcher m = QUEST_TOKEN.matcher(key);
        if (!m.matches()) return false;
        QuestInfo quest = quests.get(m.group(1));
        return quest != null && quest.tokens().contains(m.group(2));
    }

    private boolean isQuestToken(String key) {
        Matcher m = QUEST_TOKEN.matcher(key);
        return m.matches() && quests.containsKey(m.group(1))
                && (quests.get(m.group(1)).tokens().contains(m.group(2)) || isPersonToken(quests.get(m.group(1)), m.group(2)));
    }

    // Fleet memory flag QuestFleets writes for every fleet of a declared role.
    private boolean isRoleFlag(String key) {
        Matcher m = QUEST_TOKEN.matcher(key);
        return m.matches() && quests.containsKey(m.group(1)) && quests.get(m.group(1)).roles().contains(m.group(2));
    }

    private static boolean isWrite(RuleExpression e) {
        return e.operator == Operator.ASSIGN || e.operator == Operator.INCREMENT || e.operator == Operator.DECREMENT;
    }

    // Triggers

    private void checkTriggers() {
        Set<String> rowTriggers = rowTriggers();
        for (ParsedRow row : rows) {
            for (RuleExpression e : row.all()) {
                String target = fireTarget(e);
                if (target != null && !rowTriggers.contains(target) && !vanilla.triggers().contains(target)) {
                    add(Severity.ERROR, "fire-target", row, e.command + " " + target + ": no row uses this trigger");
                }
            }
        }
        Map<String, List<ParsedRow>> byTrigger = byTrigger();
        Set<String> fired = firedByRows();
        for (Map.Entry<String, List<ParsedRow>> entry : byTrigger.entrySet()) {
            String trigger = entry.getKey();
            if (isFired(trigger, fired)) continue;
            String owner = questOwner(trigger);
            List<ParsedRow> onTrigger = entry.getValue();
            add(owner != null ? Severity.ERROR : Severity.WARN, "unreachable", onTrigger.get(0),
                    "nothing fires trigger " + trigger + " (" + onTrigger.size() + (onTrigger.size() == 1 ? " row)" : " rows)")
                            + (owner != null ? "; declare it in quest " + owner + " if Java fires it" : ""));
        }
    }

    private boolean isFired(String trigger, Set<String> firedByRows) {
        if (firedByRows.contains(trigger) || VanillaRules.engineFires(trigger)) return true;
        if (vanilla.triggers().contains(trigger) || vanilla.fireAll().contains(trigger) || vanilla.fireBest().contains(trigger)) return true;
        if (declaredTriggers().contains(trigger) || intelTriggers(false).contains(trigger)) return true;
        return questOwner(trigger) == null && javaStrings.contains(trigger);
    }

    // The quest a trigger belongs to by the naming contract nskr_<q><Purpose>; null for other triggers.
    private String questOwner(String trigger) {
        for (String id : quests.keySet()) {
            String prefix = "nskr_" + id;
            if (!trigger.startsWith(prefix)) continue;
            if (trigger.length() == prefix.length()) return id;
            char next = trigger.charAt(prefix.length());
            if (next == '_' || Character.isUpperCase(next)) return id;
        }
        return null;
    }

    private Set<String> rowTriggers() {
        Set<String> triggers = new HashSet<>();
        for (ParsedRow row : rows) {
            triggers.add(row.trigger());
        }
        return triggers;
    }

    private Map<String, List<ParsedRow>> byTrigger() {
        Map<String, List<ParsedRow>> byTrigger = new LinkedHashMap<>();
        for (ParsedRow row : rows) {
            byTrigger.computeIfAbsent(row.trigger(), t -> new ArrayList<>()).add(row);
        }
        return byTrigger;
    }

    private Set<String> firedByRows() {
        Set<String> fired = new HashSet<>();
        for (ParsedRow row : rows) {
            for (RuleExpression e : row.all()) {
                String target = fireTarget(e);
                if (target != null) fired.add(target);
            }
        }
        return fired;
    }

    private static String fireTarget(RuleExpression e) {
        if (!e.isCommand("FireAll") && !e.isCommand("FireBest")) return null;
        if (e.params.isEmpty() || e.params.get(0).isVariable()) return null;
        return e.params.get(0).text();
    }

    private Set<String> declaredTriggers() {
        Set<String> triggers = new HashSet<>();
        for (QuestInfo quest : quests.values()) {
            triggers.addAll(quest.triggers());
        }
        return triggers;
    }

    private Set<String> intelTriggers(boolean allMatchingOnly) {
        Set<String> triggers = new HashSet<>();
        for (String id : quests.keySet()) {
            for (String suffix : INTEL_SUFFIXES) {
                if (!allMatchingOnly || INTEL_ALL_SUFFIXES.contains(suffix)) triggers.add("nskr_" + id + suffix);
            }
        }
        return triggers;
    }

    // Option handlers

    private void checkOptionHandlers() {
        Set<String> handled = new HashSet<>(vanilla.options());
        List<String> prefixes = new ArrayList<>();
        for (ParsedRow row : rows) {
            if (!VanillaRules.OPTION_TRIGGERS.contains(row.trigger())) continue;
            for (RuleExpression e : row.conditions()) {
                if (VanillaRules.isOptionTest(e)) handled.add(e.second.text());
                if (e.isCommand(OPTION_PREFIX_COMMAND) && !e.params.isEmpty() && !e.params.get(0).isVariable()) {
                    prefixes.add(e.params.get(0).text());
                }
            }
        }
        for (ParsedRow row : rows) {
            Set<String> offered = new LinkedHashSet<>();
            for (RulesFile.Option option : row.options()) {
                if (option.error() == null && !option.id().startsWith("$")) offered.add(option.id());
            }
            for (RuleExpression e : row.script()) {
                if (e.isCommand("AddBarEvent") && !e.params.isEmpty() && !e.params.get(0).isVariable()) {
                    offered.add(e.params.get(0).text());
                }
                if (e.error == null && e.operator == Operator.ASSIGN && e.first.text().equals("$option") && !e.second.isVariable()) {
                    offered.add(e.second.text());
                }
            }
            for (String id : offered) {
                if (handled.contains(id) || prefixes.stream().anyMatch(id::startsWith)) continue;
                add(Severity.ERROR, "handler", row, "option " + id + " has no DialogOptionSelected row with $option == " + id);
            }
        }
    }

    // Spelling

    private void checkCase() {
        Map<String, Set<String>> triggerSpellings = new HashMap<>();
        Map<String, Set<String>> keySpellings = new HashMap<>();
        List<String> vanillaTriggers = new ArrayList<>(vanilla.triggers());
        vanillaTriggers.addAll(vanilla.fireAll());
        vanillaTriggers.addAll(vanilla.fireBest());
        for (VanillaRules.EngineTrigger engine : VanillaRules.ENGINE) {
            vanillaTriggers.add(engine.trigger());
        }
        vanillaTriggers.addAll(declaredTriggers());
        spell(triggerSpellings, vanillaTriggers);
        spell(keySpellings, vanilla.keys());
        Map<ParsedRow, Set<String>> rowTriggers = new LinkedHashMap<>();
        Map<ParsedRow, Set<String>> rowKeys = new LinkedHashMap<>();
        for (ParsedRow row : rows) {
            Set<String> triggers = new LinkedHashSet<>();
            triggers.add(row.trigger());
            Set<String> keys = new LinkedHashSet<>();
            for (RuleExpression e : row.all()) {
                String target = fireTarget(e);
                if (target != null) triggers.add(target);
                for (Token token : VanillaRules.tokens(e)) {
                    if (token.isVariable()) keys.add(token.key());
                }
            }
            spell(triggerSpellings, triggers);
            spell(keySpellings, keys);
            rowTriggers.put(row, triggers);
            rowKeys.put(row, keys);
        }
        for (ParsedRow row : rows) {
            reportCase(row, "trigger", rowTriggers.get(row), triggerSpellings);
            reportCase(row, "key", rowKeys.get(row), keySpellings);
        }
    }

    private static void spell(Map<String, Set<String>> spellings, Iterable<String> names) {
        for (String name : names) {
            spellings.computeIfAbsent(name.toLowerCase(), n -> new TreeSet<>()).add(name);
        }
    }

    private void reportCase(ParsedRow row, String kind, Set<String> names, Map<String, Set<String>> spellings) {
        for (String name : names) {
            Set<String> others = new TreeSet<>(spellings.get(name.toLowerCase()));
            others.remove(name);
            if (!others.isEmpty()) {
                add(Severity.WARN, "case", row, kind + " " + name + " differs only by case from " + String.join(", ", others));
            }
        }
    }

    // Memory keys

    private void checkKeys() {
        Set<String> written = new HashSet<>();
        for (ParsedRow row : rows) {
            for (RuleExpression e : row.script()) {
                if (e.error == null && isWrite(e)) written.add(e.first.key());
            }
        }
        for (ParsedRow row : rows) {
            Set<String> read = new LinkedHashSet<>();
            for (RuleExpression e : row.conditions()) {
                if (e.error != null || e.operator == Operator.ASSIGN) continue;
                for (Token token : VanillaRules.tokens(e)) {
                    if (token.isVariable()) read.add(token.key());
                }
            }
            for (RuleExpression e : row.script()) {
                if (e.error != null) continue;
                if (e.command != null) {
                    List<Token> params = KEY_COMMANDS.contains(e.command) && !e.params.isEmpty() ? e.params.subList(1, e.params.size()) : e.params;
                    for (Token token : params) {
                        if (token.isVariable()) read.add(token.key());
                    }
                } else if (e.operator == Operator.ASSIGN) {
                    if (e.second.isVariable()) read.add(e.second.key());
                } else {
                    read.add(e.first.key());
                }
            }
            List<String> shown = new ArrayList<>();
            shown.add(row.row().text());
            for (RulesFile.Option option : row.options()) {
                if (option.error() == null) shown.add(option.text());
            }
            for (String text : shown) {
                Matcher m = NSKR_KEY.matcher(text);
                while (m.find()) {
                    if (questTokens(m.group()).isEmpty()) read.add("$" + m.group(1));
                }
            }
            for (String key : read) {
                if (!key.startsWith("$nskr_") || written.contains(key) || javaKeys.contains(key) || FRAMEWORK_KEYS.contains(key)) continue;
                if (isRoleFlag(key)) continue;
                if (isQuestToken(key)) continue;
                add(Severity.WARN, "unwritten", row, key + " is read but no row writes it and no Java string names it");
            }
        }
    }

    // Identical conditions on a FireBest trigger make the engine pick one row at random.

    private void checkIdenticalConditions() {
        Set<String> fireAll = new HashSet<>(vanilla.fireAll());
        fireAll.addAll(intelTriggers(true));
        for (ParsedRow row : rows) {
            for (RuleExpression e : row.all()) {
                if (e.isCommand("FireAll") && fireTarget(e) != null) fireAll.add(fireTarget(e));
            }
        }
        for (Map.Entry<String, List<ParsedRow>> entry : byTrigger().entrySet()) {
            if (fireAll.contains(entry.getKey()) || VanillaRules.engineFiresAll(entry.getKey())) continue;
            Map<List<String>, ParsedRow> first = new HashMap<>();
            for (ParsedRow row : entry.getValue()) {
                List<String> key = conditionKey(row);
                if (key == null) continue;
                ParsedRow earlier = first.putIfAbsent(key, row);
                if (earlier != null && !(isVariant(earlier) && isVariant(row))) {
                    add(Severity.WARN, "identical", row, "same trigger and conditions as " + earlier.id() + " (line " + earlier.line()
                            + "); FireBest picks one at random. Write variant in both rows' notes if that is intended");
                }
            }
        }
    }

    private static List<String> conditionKey(ParsedRow row) {
        List<String> key = new ArrayList<>();
        for (RuleExpression e : row.conditions()) {
            if (e.error != null) return null;
            key.add(e.source);
        }
        key.sort(Comparator.naturalOrder());
        return key;
    }

    private static boolean isVariant(ParsedRow row) {
        return row.row().notes().toLowerCase().contains("variant");
    }

    // README "Rules contract": a quest's rows are named nskr_<q>_..., keep speaker flags as $nskr_<q>_<name> on the
    // speaker or shared knowledge as $player.nskr_<name>, and never use $global.

    private void checkQuestNaming() {
        for (ParsedRow row : rows) {
            String owner = questOwner(row.trigger());
            if (owner != null && !row.id().startsWith("nskr_" + owner + "_")) {
                add(Severity.WARN, "naming", row, "rows on quest " + owner + "'s trigger " + row.trigger() + " are named nskr_" + owner + "_...");
            }
            String quest = rowQuest(row);
            if (quest == null) continue;
            for (RuleExpression e : row.script()) {
                if (e.error != null || !isWrite(e)) continue;
                String text = e.first.text();
                String key = e.first.key();
                if (text.startsWith("$global.")) {
                    add(Severity.ERROR, "naming", row, "writes " + text + "; quest content never uses $global");
                } else if (key.startsWith("$nskr_") && !(text.startsWith("$player.") || isSpeakerKey(text, quest))) {
                    add(Severity.WARN, "naming", row, "writes " + text + "; quest rows write $nskr_" + quest + "_<name> on the speaker or $player.nskr_<name>");
                }
            }
        }
    }

    // The quest whose rows this is, by the rule id pattern nskr_<q>_.
    private String rowQuest(ParsedRow row) {
        for (String id : quests.keySet()) {
            if (row.id().startsWith("nskr_" + id + "_")) return id;
        }
        return null;
    }

    private static boolean isSpeakerKey(String text, String quest) {
        String local = text.startsWith("$local.") ? "$" + text.substring("$local.".length()) : text;
        return local.startsWith("$nskr_" + quest + "_");
    }

    // Intel rows are matched outside any dialog; their Script and Options are ignored.

    private void checkIntelRows() {
        Set<String> intel = intelTriggers(false);
        for (ParsedRow row : rows) {
            if (!intel.contains(row.trigger())) continue;
            if (!row.row().script().isBlank() || !row.row().options().isBlank()) {
                add(Severity.WARN, "intel", row, "intel rows ignore Script and Options");
            }
            for (RuleExpression e : row.conditions()) {
                if (e.command != null && !e.command.equals(QUEST_COMMAND)) {
                    add(Severity.ERROR, "intel", row, "command " + e.command + " in an intel row; intel rows use memory keys and nskr_quest only");
                }
            }
        }
    }

    // Definitions

    private void checkDeclarations() {
        Set<String> rowTriggers = rowTriggers();
        for (QuestInfo quest : quests.values()) {
            String where = "(quest " + quest.id() + ")";
            for (String trigger : quest.triggers()) {
                if (!rowTriggers.contains(trigger)) {
                    add(Severity.ERROR, "declared-trigger", 0, where, "declared trigger " + trigger + " has no rows");
                }
            }
            for (String token : quest.tokens()) {
                for (String other : quest.tokens()) {
                    if (!token.equals(other) && other.startsWith(token)) {
                        add(Severity.ERROR, "token-prefix", 0, where, "token " + token + " is a prefix of token " + other);
                    }
                }
                for (String person : quest.people()) {
                    if (person.startsWith(token)) {
                        add(Severity.ERROR, "token-prefix", 0, where, "token " + token + " is a prefix of the person tokens of " + person);
                    }
                }
            }
        }
    }

    // Report

    private void add(Severity severity, String check, ParsedRow row, String message) {
        add(severity, check, row.line(), row.id(), message);
    }

    private void add(Severity severity, String check, int line, String ruleId, String message) {
        findings.add(new Finding(severity, check, line, ruleId, message));
    }

    private int report(PrintStream out) {
        List<Finding> sorted = new ArrayList<>(findings);
        sorted.sort(Comparator.comparingInt(Finding::line));
        Map<String, int[]> counts = new TreeMap<>();
        int errors = 0;
        for (Finding f : sorted) {
            out.println(f.severity + " " + RULES + ":" + (f.line > 0 ? f.line : "-") + " " + f.ruleId + " [" + f.check + "] " + f.message);
            counts.computeIfAbsent(f.check, c -> new int[2])[f.severity.ordinal()]++;
            if (f.severity == Severity.ERROR) errors++;
        }
        out.println();
        out.println("RulesCheck: " + rows.size() + " rows, " + quests.size() + " quests, "
                + errors + " errors, " + (sorted.size() - errors) + " warnings");
        for (Map.Entry<String, int[]> entry : counts.entrySet()) {
            out.println("  " + entry.getKey() + ": " + entry.getValue()[0] + " errors, " + entry.getValue()[1] + " warnings");
        }
        return errors > 0 ? 1 : 0;
    }
}
