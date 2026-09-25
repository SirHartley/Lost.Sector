package lostsector.quest;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

// The verbs of the nskr_quest rules command (README "The quest command"); the command class only resolves its
// tokens. Condition verbs never write state and never touch the dialog, so they work with a null dialog. Errors
// never throw: they are logged with the quest and rule ids and the verb returns false.
public final class QuestVerbs {

    private QuestVerbs() {
    }

    public static boolean execute(String ruleId, InteractionDialogAPI dialog, List<String> words, Map<String, MemoryAPI> memoryMap) {
        if (words.isEmpty() || words.get(0) == null) {
            QuestContext.report("?", ruleId, dialog, "nskr_quest needs a quest id");
            return false;
        }
        String questId = words.get(0);
        if (questId.equals("confirm")) return confirm(ruleId, dialog, words.subList(1, words.size()), memoryMap);
        QuestManager manager = QuestManager.get();
        QuestManager.Run<?, ?> run = manager == null ? null : manager.run(questId);
        if (run == null) {
            QuestContext.report(questId, ruleId, dialog, "unknown quest");
            return false;
        }
        if (run.state() == null) {
            QuestContext.report(questId, ruleId, dialog, "the quest has no state");
            return false;
        }
        if (words.size() < 2 || words.get(1) == null) {
            QuestContext.report(questId, ruleId, dialog, "nskr_quest " + questId + " needs a verb");
            return false;
        }
        return execute(run, ruleId, dialog, words.get(1), words.subList(2, words.size()), memoryMap);
    }

    private static <S extends Enum<S> & QuestStage, T extends QuestState<S>> boolean execute(
            QuestManager.Run<S, T> run, String ruleId, InteractionDialogAPI dialog, String verb, List<String> args,
            Map<String, MemoryAPI> memoryMap) {
        QuestContext<S, T> ctx = new QuestContext<>(run, "rule " + ruleId, ruleId, dialog, memoryMap, args.size() > 1 ? args.subList(1, args.size()) : null);
        T state = run.state();
        switch (verb) {
            case "is": {
                if (args.isEmpty()) return usage(ctx, "is <STAGE> [<STAGE> ...]");
                boolean match = false;
                for (String name : args) {
                    S stage = stage(ctx, name);
                    if (stage == null) return false;
                    if (state.stage == stage) match = true;
                }
                return match;
            }
            case "reached": {
                if (args.size() != 1) return usage(ctx, "reached <STAGE>");
                S stage = stage(ctx, args.get(0));
                return stage != null && state.reached(stage);
            }
            case "flag": {
                if (args.size() != 1) return usage(ctx, "flag <FLAG>");
                Enum<?> flag = flag(ctx, args.get(0));
                return flag != null && state.flags.contains(flag.name());
            }
            case "check": {
                if (args.isEmpty()) return usage(ctx, "check <name> [args]");
                Predicate<QuestContext<S, T>> check = run.quest.declarations().checks().get(args.get(0));
                if (check == null) return unknown(ctx, "check", args.get(0));
                return check.test(ctx);
            }
            case "advance": {
                if (args.size() != 2) return usage(ctx, "advance <FROM> <TO>");
                S from = stage(ctx, args.get(0));
                S to = stage(ctx, args.get(1));
                return from != null && to != null && ctx.advance(from, to);
            }
            case "set":
            case "clear": {
                if (args.size() != 1) return usage(ctx, verb + " <FLAG>");
                Enum<?> flag = flag(ctx, args.get(0));
                if (flag == null) return false;
                if (verb.equals("set")) ctx.set(flag); else ctx.clear(flag);
                return true;
            }
            case "do": {
                if (args.isEmpty()) return usage(ctx, "do <name> [args]");
                String name = args.get(0);
                Consumer<QuestContext<S, T>> action = run.quest.declarations().actions().get(name);
                if (action == null) return unknown(ctx, "action", name);
                QuestModule<S, T> module = run.quest.declarations().actionModule(name);
                if (!module.isActiveIn(state.stage)) {
                    ctx.error("action " + name + " skipped: " + module.getClass().getSimpleName() + " is not active in " + state.stage);
                    return false;
                }
                action.accept(ctx);
                return true;
            }
            case "engage": {
                if (args.size() != 1) return usage(ctx, "engage <role>");
                if (dialog == null) {
                    ctx.error("engage needs a dialog");
                    return false;
                }
                String problem = run.fleets.engage(args.get(0), dialog);
                if (problem != null) {
                    ctx.error("engage " + args.get(0) + " refused: " + problem);
                    return false;
                }
                return true;
            }
            default:
                ctx.error("unknown verb " + verb);
                return false;
        }
    }

    // A yes/no prompt on an option that already exists; the text gets token replacement as SetTooltip does.
    // addOptionConfirmation silently ignores an unknown option id, so the verb checks hasOption first.
    private static boolean confirm(String ruleId, InteractionDialogAPI dialog, List<String> args, Map<String, MemoryAPI> memoryMap) {
        if (args.size() != 4 || args.stream().anyMatch(java.util.Objects::isNull)) {
            QuestContext.report("confirm", ruleId, dialog, "usage: nskr_quest confirm <optionId> \"<text>\" \"<yes>\" \"<no>\"");
            return false;
        }
        if (dialog == null) {
            QuestContext.report("confirm", ruleId, dialog, "confirm needs a dialog");
            return false;
        }
        String optionId = args.get(0);
        if (!dialog.getOptionPanel().hasOption(optionId)) {
            QuestContext.report("confirm", ruleId, dialog, "option " + optionId + " does not exist yet; confirm it in the row that adds it or later");
            return false;
        }
        String text = Global.getSector().getRules().performTokenReplacement(ruleId, args.get(1), dialog.getInteractionTarget(), memoryMap);
        dialog.getOptionPanel().addOptionConfirmation(optionId, text, args.get(2), args.get(3));
        return true;
    }

    private static <S extends Enum<S> & QuestStage> S stage(QuestContext<S, ?> ctx, String name) {
        for (S stage : ctx.quest().stages().getEnumConstants()) {
            if (stage.name().equals(name)) return stage;
        }
        ctx.error("unknown stage " + name);
        return null;
    }

    private static Enum<?> flag(QuestContext<?, ?> ctx, String name) {
        for (Enum<?> flag : ctx.quest().flags().getEnumConstants()) {
            if (flag.name().equals(name)) return flag;
        }
        ctx.error("unknown flag " + name);
        return null;
    }

    private static boolean unknown(QuestContext<?, ?> ctx, String kind, String name) {
        ctx.error("unknown " + kind + " " + name);
        return false;
    }

    private static boolean usage(QuestContext<?, ?> ctx, String usage) {
        ctx.error("usage: nskr_quest " + ctx.quest().id() + " " + usage);
        return false;
    }
}
