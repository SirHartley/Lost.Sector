package lostsector.quest;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.campaign.rules.RuleTokenReplacementGeneratorPlugin;
import com.fs.starfarer.api.characters.PersonAPI;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;

// Registered once per load in ModPlugin.onGameLoad; the engine keeps generators in a transient list.
// Keys include the leading '$': the engine replaces each with replaceAll("(?s)\\" + key, value), longest key first,
// and passes the value to replaceAll unescaped, so every value goes through Matcher.quoteReplacement.
public final class QuestTokens implements RuleTokenReplacementGeneratorPlugin {

    private static final String PREFIX = "nskr_";

    // Tokens whose lambda threw, logged once per load.
    private final Set<String> failed = new HashSet<>();

    @Override
    public Map<String, String> getTokenReplacements(String ruleId, Object entity, Map<String, MemoryAPI> memoryMap) {
        SectorEntityToken target = entity instanceof SectorEntityToken ? (SectorEntityToken) entity : null;
        Map<String, String> tokens = values(ruleId, target, memoryMap, failed);
        tokens.replaceAll((key, value) -> Matcher.quoteReplacement(value));
        return tokens;
    }

    // The unescaped token values for a rule id, keyed like getTokenReplacements; QuestText reads them to highlight
    // token values. A token whose lambda throws is "" and is logged when failed is not null.
    static Map<String, String> values(String ruleId, Map<String, MemoryAPI> memoryMap, Set<String> failed) {
        return values(ruleId, null, memoryMap, failed);
    }

    // target: the entity the engine passes with the text, which for dialog text is the dialog's interaction target
    // (FireBest/FireAll addText, Misc.Token.getStringWithTokenReplacement); tokens read it as ctx.target().
    static Map<String, String> values(String ruleId, SectorEntityToken target, Map<String, MemoryAPI> memoryMap, Set<String> failed) {
        if (ruleId == null || !ruleId.startsWith(PREFIX)) return Collections.emptyMap();
        int end = ruleId.indexOf('_', PREFIX.length());
        if (end < 0) return Collections.emptyMap();
        QuestManager manager = QuestManager.get();
        QuestManager.Run<?, ?> run = manager == null ? null : manager.run(ruleId.substring(PREFIX.length(), end));
        if (run == null || run.state() == null) return Collections.emptyMap();
        Map<String, String> tokens = new HashMap<>();
        putDeclared(tokens, run, ruleId, target, memoryMap, failed);
        putPeople(tokens, run);
        return tokens;
    }

    private static <S extends Enum<S> & QuestStage, T extends QuestState<S>> void putDeclared(
            Map<String, String> tokens, QuestManager.Run<S, T> run, String ruleId, SectorEntityToken target,
            Map<String, MemoryAPI> memoryMap, Set<String> failed) {
        QuestContext<S, T> ctx = new QuestContext<>(run, "token", ruleId, null, memoryMap, null, target);
        for (Map.Entry<String, Function<QuestContext<S, T>, String>> token : run.quest.declarations().tokens().entrySet()) {
            put(tokens, run.id(), token.getKey(), value(run.id(), token.getKey(), token.getValue(), ctx, failed));
        }
    }

    // A token that throws shows as empty text instead of breaking the dialog.
    private static <C> String value(String questId, String name, Function<C, String> token, C ctx, Set<String> failed) {
        try {
            String value = token.apply(ctx);
            return value == null ? "" : value;
        } catch (RuntimeException e) {
            if (failed != null && failed.add(questId + "." + name)) {
                QuestManager.logError(questId, "token " + name + " threw " + e);
            }
            return "";
        }
    }

    private static void putPeople(Map<String, String> tokens, QuestManager.Run<?, ?> run) {
        for (Map.Entry<String, PersonAPI> entry : run.state().people.entrySet()) {
            PersonAPI person = entry.getValue();
            String key = entry.getKey();
            boolean male = person.isMale();
            put(tokens, run.id(), key + "_name", person.getName().getFullName());
            put(tokens, run.id(), key + "_heOrShe", male ? "he" : "she");
            put(tokens, run.id(), key + "_HeOrShe", male ? "He" : "She");
            put(tokens, run.id(), key + "_himOrHer", male ? "him" : "her");
            put(tokens, run.id(), key + "_HimOrHer", male ? "Him" : "Her");
            put(tokens, run.id(), key + "_hisOrHer", male ? "his" : "her");
            put(tokens, run.id(), key + "_HisOrHer", male ? "His" : "Her");
            put(tokens, run.id(), key + "_himOrHerself", male ? "himself" : "herself");
            put(tokens, run.id(), key + "_HimOrHerself", male ? "Himself" : "Herself");
            put(tokens, run.id(), key + "_manOrWoman", male ? "man" : "woman");
            put(tokens, run.id(), key + "_ManOrWoman", male ? "Man" : "Woman");
        }
    }

    private static void put(Map<String, String> tokens, String questId, String name, String value) {
        tokens.put("$" + PREFIX + questId + "_" + name, value);
    }
}
