package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.rules.RuleAPI;
import com.fs.starfarer.api.campaign.rules.RulesAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Rules text read outside any dialog (README "Intel"). Rows are matched with a null dialog; the engine evaluates
// their conditions as written, and a command that throws is not caught, so these rows use only memory keys and
// nskr_quest condition verbs. Script and Options of the matched rows never run. The rules check tool reads the
// constants below; this class must stay loadable without the game (no game calls in static initializers).
public final class QuestText {

    // Scratch keys written into the local memory before matching.
    public static final String KEY = "$nskr_intel_key";
    public static final String STATUS = "$nskr_intel_status";
    public static final String UPDATE = "$nskr_intel_update";
    public static final String MODE = "$nskr_intel_mode";
    public static final List<String> SCRATCH_KEYS = List.of(KEY, STATUS, UPDATE, MODE);

    // Intel triggers are nskr_<q> followed by one of these suffixes.
    public static final String TITLE = "IntelTitle";
    public static final String BULLETS = "IntelBullets";
    public static final String DESC = "IntelDesc";
    public static final List<String> SUFFIXES = List.of(TITLE, BULLETS, DESC);
    // Every matching row of these triggers is shown; the title is a best match.
    public static final List<String> ALL_MATCHING_SUFFIXES = List.of(BULLETS, DESC);

    // Values of $nskr_intel_mode.
    public static final String MODE_LIST = "list";
    public static final String MODE_TOOLTIP = "tooltip";
    public static final String MODE_UPDATE = "update";
    public static final String MODE_DESC = "desc";

    private static final String TOKEN_MARK = "$nskr_";

    // One matched row's text after token replacement, with the quest token values it shows in text order.
    public static final class Line {

        public final String text;
        public final String[] highlights;

        Line(String text, String[] highlights) {
            this.text = text;
            this.highlights = highlights;
        }
    }

    private QuestText() {
    }

    public static String trigger(String questId, String suffix) {
        return "nskr_" + questId + suffix;
    }

    // The memory map intel rows are matched with. Local is a fresh scratch memory holding the intel keys. Player and
    // global memory are read without the campaign plugins' fact refresh that getMemory() runs.
    public static Map<String, MemoryAPI> intelMemory(String key, String status, String update, String mode) {
        MemoryAPI local = Global.getFactory().createMemory();
        local.set(KEY, key);
        local.set(STATUS, status);
        local.set(UPDATE, update == null ? "" : update);
        local.set(MODE, mode);
        Map<String, MemoryAPI> memoryMap = new HashMap<>();
        memoryMap.put(MemKeys.LOCAL, local);
        memoryMap.put(MemKeys.PLAYER, Global.getSector().getCharacterData().getMemoryWithoutUpdate());
        memoryMap.put(MemKeys.GLOBAL, Global.getSector().getMemoryWithoutUpdate());
        return memoryMap;
    }

    // The best-matching row's text; null when no row matches or its text is blank. Equal scores are picked at
    // random by the engine, so title rows must not tie.
    public static String title(String trigger, Map<String, MemoryAPI> memoryMap) {
        RulesAPI rules = Global.getSector().getRules();
        RuleAPI rule = rules.getBestMatching(null, trigger, null, memoryMap);
        Line line = rule == null ? null : line(rules, rule, replacementMemory(memoryMap), false);
        return line == null ? null : line.text;
    }

    // Every matching row in rules.csv order, one line each; rows with blank text are skipped.
    public static List<Line> lines(String trigger, Map<String, MemoryAPI> memoryMap) {
        RulesAPI rules = Global.getSector().getRules();
        List<RuleAPI> matches = rules.getAllMatching(null, trigger, null, memoryMap);
        List<Line> lines = new ArrayList<>(matches.size());
        if (matches.isEmpty()) return lines;
        Map<String, MemoryAPI> replacement = replacementMemory(memoryMap);
        for (RuleAPI rule : matches) {
            Line line = line(rules, rule, replacement, true);
            if (line != null) lines.add(line);
        }
        return lines;
    }

    // Token replacement gets the local scratch memory only: its memory pass runs two regex replacements for every key
    // of every memory in the map (Misc.replaceTokensFromMemory), which for sector memory costs far more than the
    // matching itself.
    private static Map<String, MemoryAPI> replacementMemory(Map<String, MemoryAPI> memoryMap) {
        Map<String, MemoryAPI> replacement = new HashMap<>();
        replacement.put(MemKeys.LOCAL, memoryMap.get(MemKeys.LOCAL));
        return replacement;
    }

    private static Line line(RulesAPI rules, RuleAPI rule, Map<String, MemoryAPI> replacement, boolean highlight) {
        String raw = rule.pickText();
        if (raw == null || raw.isBlank()) return null;
        // Vanilla's and the framework's generator keys and every memory token start with '$'.
        if (raw.indexOf('$') < 0) return new Line(raw, new String[0]);
        String[] highlights = highlight && raw.contains(TOKEN_MARK)
                ? highlights(raw, QuestTokens.values(rule.getId(), replacement, null))
                : new String[0];
        return new Line(rules.performTokenReplacement(rule.getId(), raw, null, replacement), highlights);
    }

    // The value of every quest token in the raw text, once per occurrence and in text order, which is the order
    // LabelAPI.setHighlight searches in. Empty values are skipped.
    private static String[] highlights(String raw, Map<String, String> tokens) {
        List<String> found = new ArrayList<>();
        for (int at = raw.indexOf(TOKEN_MARK); at >= 0; at = raw.indexOf(TOKEN_MARK, at + 1)) {
            String longest = null;
            for (String token : tokens.keySet()) {
                if (raw.startsWith(token, at) && (longest == null || token.length() > longest.length())) longest = token;
            }
            if (longest == null) continue;
            String value = tokens.get(longest);
            if (!value.isEmpty()) found.add(value);
        }
        return found.toArray(new String[0]);
    }
}
