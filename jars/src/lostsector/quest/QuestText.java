package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemKeys;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.rules.RuleAPI;
import com.fs.starfarer.api.campaign.rules.RulesAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.Highlight;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetTextHighlightColors;
import com.fs.starfarer.api.impl.campaign.rulecmd.SetTextHighlights;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Rules text read outside any dialog: intel entries (README "Intel") and raid objectives (README "Raid objectives"). Rows are matched with a null dialog; the engine evaluates
// their conditions as written, and a command that throws is not caught, so these rows use only memory keys and
// nskr_quest condition verbs. Script and Options of the matched rows never run; SetTextHighlights and
// SetTextHighlightColors lines in the Script are read as highlight declarations. The rules check tool reads the
// constants below; this class must stay loadable without the game (no game calls in static initializers).
public final class QuestText {

    // Scratch keys written into the local memory before matching.
    public static final String KEY = "$nskr_intel_key";
    public static final String RECORD = "$nskr_intel_record";
    public static final String STATUS = "$nskr_intel_status";
    public static final String UPDATE = "$nskr_intel_update";
    public static final String MODE = "$nskr_intel_mode";
    public static final List<String> SCRATCH_KEYS = List.of(KEY, RECORD, STATUS, UPDATE, MODE);

    // Intel triggers are nskr_<q> followed by one of these suffixes.
    public static final String TITLE = "IntelTitle";
    public static final String BULLETS = "IntelBullets";
    public static final String DESC = "IntelDesc";
    public static final List<String> SUFFIXES = List.of(TITLE, BULLETS, DESC);
    // Every matching row of these triggers is shown; the title is a best match.
    public static final List<String> ALL_MATCHING_SUFFIXES = List.of(BULLETS, DESC);

    // Raid objective rows: nskr_<q> followed by a raid suffix, selected by the raid key. The name is a best match; the
    // tooltip and the result print every matching row.
    public static final String RAID_KEY = "$nskr_raid_key";
    public static final String RAID_NAME = "RaidName";
    public static final String RAID_TOOLTIP = "RaidTooltip";
    public static final String RAID_RESULT = "RaidResult";
    public static final List<String> RAID_SUFFIXES = List.of(RAID_NAME, RAID_TOOLTIP, RAID_RESULT);
    public static final List<String> RAID_ALL_MATCHING_SUFFIXES = List.of(RAID_TOOLTIP, RAID_RESULT);

    // Values of $nskr_intel_mode.
    public static final String MODE_LIST = "list";
    public static final String MODE_TOOLTIP = "tooltip";
    public static final String MODE_UPDATE = "update";
    public static final String MODE_DESC = "desc";

    private static final String TOKEN_MARK = "$nskr_";

    // One matched row's text after token replacement, with its highlighted phrases in text order and their colors.
    public static final class Line {

        public final String text;
        public final String[] highlights;
        public final Color[] colors;

        Line(String text, String[] highlights, Color[] colors) {
            this.text = text;
            this.highlights = highlights;
            this.colors = colors;
        }
    }

    // A phrase found in the final text.
    private static final class Mark {

        final int start;
        final int end;
        final String phrase;
        final Color color;

        Mark(int start, String phrase, Color color) {
            this.start = start;
            this.end = start + phrase.length();
            this.phrase = phrase;
            this.color = color;
        }
    }

    private QuestText() {
    }

    public static String trigger(String questId, String suffix) {
        return "nskr_" + questId + suffix;
    }

    // The memory map intel rows are matched with. Local is a fresh scratch memory holding the intel keys. Player and
    // global memory are read without the campaign plugins' fact refresh that getMemory() runs.
    // The record is empty for an entry shown without one.
    public static Map<String, MemoryAPI> intelMemory(String key, String record, String status, String update, String mode) {
        MemoryAPI local = Global.getFactory().createMemory();
        local.set(KEY, key);
        local.set(RECORD, record == null ? "" : record);
        local.set(STATUS, status);
        local.set(UPDATE, update == null ? "" : update);
        local.set(MODE, mode);
        Map<String, MemoryAPI> memoryMap = new HashMap<>();
        memoryMap.put(MemKeys.LOCAL, local);
        memoryMap.put(MemKeys.PLAYER, Global.getSector().getCharacterData().getMemoryWithoutUpdate());
        memoryMap.put(MemKeys.GLOBAL, Global.getSector().getMemoryWithoutUpdate());
        return memoryMap;
    }

    // The memory map raid objective rows are matched with: a scratch local memory holding the raid key, as for intel.
    public static Map<String, MemoryAPI> raidMemory(String key) {
        MemoryAPI local = Global.getFactory().createMemory();
        local.set(RAID_KEY, key);
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
        Line line = rule == null ? null : line(rules, rule, memoryMap, replacementMemory(memoryMap), false);
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
            Line line = line(rules, rule, memoryMap, replacement, true);
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

    private static Line line(RulesAPI rules, RuleAPI rule, Map<String, MemoryAPI> memoryMap, Map<String, MemoryAPI> replacement,
                             boolean highlight) {
        String raw = rule.pickText();
        if (raw == null || raw.isBlank()) return null;
        // Vanilla's and the framework's generator keys and every memory token start with '$'.
        String text = raw.indexOf('$') < 0 ? raw : rules.performTokenReplacement(rule.getId(), raw, null, replacement);
        if (!highlight) return new Line(text, new String[0], new Color[0]);
        List<Mark> marks = new ArrayList<>();
        if (raw.contains(TOKEN_MARK)) {
            List<String> values = tokenValues(raw, QuestTokens.values(rule.getId(), replacement, null));
            find(text, values, List.of(Misc.getHighlightColor()), marks);
        }
        declaredHighlights(rules, rule, text, memoryMap, replacement, marks);
        marks.sort((a, b) -> Integer.compare(a.start, b.start));
        String[] phrases = new String[marks.size()];
        Color[] colors = new Color[marks.size()];
        for (int i = 0; i < marks.size(); i++) {
            phrases[i] = marks.get(i).phrase;
            colors[i] = marks.get(i).color;
        }
        return new Line(text, phrases, colors);
    }

    // The value of every quest token in the raw text, once per occurrence and in text order. Empty values are skipped.
    private static List<String> tokenValues(String raw, Map<String, String> tokens) {
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
        return found;
    }

    // SetTextHighlights (and its subclass Highlight) and SetTextHighlightColors lines, read without running them. Each
    // argument resolves as in the commands: SetTextHighlights reads Token.getStringWithTokenReplacement, which is
    // getString(memoryMap) followed by performTokenReplacement; SetTextHighlightColors reads Token.getColor(memoryMap).
    // Declared phrases replace token highlights they overlap.
    private static void declaredHighlights(RulesAPI rules, RuleAPI rule, String text, Map<String, MemoryAPI> memoryMap,
                                           Map<String, MemoryAPI> replacement, List<Mark> marks) {
        List<String> phrases = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        for (RuleScript.Command command : RuleScript.commands(rule)) {
            if (command.className.equals(SetTextHighlights.class.getName()) || command.className.equals(Highlight.class.getName())) {
                for (Token token : command.params) {
                    String phrase = token.getString(memoryMap);
                    if (phrase == null) continue;
                    phrase = rules.performTokenReplacement(rule.getId(), phrase, null, replacement);
                    if (!phrase.isEmpty()) phrases.add(phrase);
                }
            } else if (command.className.equals(SetTextHighlightColors.class.getName())) {
                for (Token token : command.params) {
                    colors.add(token.getColor(memoryMap));
                }
            }
        }
        if (phrases.isEmpty()) return;
        List<Mark> declared = new ArrayList<>();
        find(text, phrases, colors.isEmpty() ? List.of(Misc.getHighlightColor()) : colors, declared);
        marks.removeIf(mark -> overlaps(mark, declared));
        marks.addAll(declared);
    }

    // Each phrase is searched after the previous one, as LabelAPI.setHighlight does. Phrase i takes color i; phrases past
    // the list take the last color, which SetTextHighlightColors sets as the paragraph's highlight color.
    private static void find(String text, List<String> phrases, List<Color> colors, List<Mark> marks) {
        int from = 0;
        for (int i = 0; i < phrases.size(); i++) {
            String phrase = phrases.get(i);
            int at = text.indexOf(phrase, from);
            if (at < 0) continue;
            Color color = colors.get(Math.min(i, colors.size() - 1));
            marks.add(new Mark(at, phrase, color == null ? Misc.getHighlightColor() : color));
            from = at + phrase.length();
        }
    }

    private static boolean overlaps(Mark mark, List<Mark> others) {
        for (Mark other : others) {
            if (mark.start < other.end && other.start < mark.end) return true;
        }
        return false;
    }
}
