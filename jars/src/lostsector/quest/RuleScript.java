package lostsector.quest;

import com.fs.starfarer.api.campaign.rules.ExpressionAPI;
import com.fs.starfarer.api.campaign.rules.RuleAPI;
import com.fs.starfarer.api.util.Misc.Token;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;

// Reads a rule's Script commands without running them. ExpressionAPI exposes neither the command nor its arguments;
// the engine's expression class (obfuscated name, 0.98a-RC8 com.fs.starfarer.campaign.rules) has the public methods
// getCommandClass(), the resolved class name or null for a line that is not a command, and getCommandParams(), the
// Misc.Token arguments. They are called by name through MethodHandles, because the script class loader rejects
// java.lang.reflect and the class name changes between builds.
final class RuleScript {

    // One Script command line.
    static final class Command {

        final String className;
        final List<Token> params;

        Command(String className, List<Token> params) {
            this.className = className;
            this.params = params;
        }
    }

    private static Class<?> expressionClass;
    private static MethodHandle commandClass;
    private static MethodHandle commandParams;
    private static boolean failed;

    private RuleScript() {
    }

    // Empty when the rule has no commands or the accessors cannot be found; the latter is logged once per game run.
    @SuppressWarnings("unchecked")
    static List<Command> commands(RuleAPI rule) {
        List<ExpressionAPI> script = rule.getScriptCopy();
        List<Command> commands = new ArrayList<>(script.size());
        for (ExpressionAPI expression : script) {
            if (!bind(expression.getClass())) return List.of();
            try {
                String name = (String) commandClass.invoke(expression);
                if (name == null) continue;
                List<Token> params = (List<Token>) commandParams.invoke(expression);
                commands.add(new Command(name, params == null ? List.of() : params));
            } catch (Throwable e) {
                fail("reading the Script of " + rule.getId() + " failed: " + e);
                return List.of();
            }
        }
        return commands;
    }

    private static boolean bind(Class<?> type) {
        if (type == expressionClass) return true;
        if (failed) return false;
        try {
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            commandClass = lookup.findVirtual(type, "getCommandClass", MethodType.methodType(String.class));
            commandParams = lookup.findVirtual(type, "getCommandParams", MethodType.methodType(List.class));
            expressionClass = type;
            return true;
        } catch (ReflectiveOperationException e) {
            fail("the rule expression class " + type.getName() + " has no getCommandClass() or getCommandParams(): " + e);
            return false;
        }
    }

    private static void fail(String message) {
        if (failed) return;
        failed = true;
        QuestManager.logError("rules", message + "; intel rows show token highlights only");
    }
}
