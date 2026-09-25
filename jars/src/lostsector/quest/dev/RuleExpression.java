package lostsector.quest.dev;

import java.util.ArrayList;
import java.util.List;

// One Conditions or Script line, parsed as the 0.98a-RC8 loader parses it: Misc.tokenize (sources-api/util.java)
// and the rule expression constructor (sources-obf/campaign.rules.java). Misc cannot be called here because its
// static initializer calls Global.getSettings(), so the tokenizer is ported.
final class RuleExpression {

    enum TokenType {
        VARIABLE,
        LITERAL,
        OPERATOR
    }

    enum Operator {
        ASSIGN("="),
        NOT("!"),
        NOT_EQUAL("!="),
        EQUAL("=="),
        GREATER_OR_EQUAL(">="),
        LESS("<"),
        LESS_OR_EQUAL("<="),
        GREATER(">"),
        INCREMENT("++"),
        DECREMENT("--");

        private final String symbol;

        Operator(String symbol) {
            this.symbol = symbol;
        }

        // Operator strings outside this list are ignored by the engine, which is why "+=" reduces a line to "$x".
        static Operator of(String text) {
            for (Operator operator : values()) {
                if (operator.symbol.equals(text)) return operator;
            }
            return null;
        }
    }

    record Token(String text, TokenType type) {

        boolean isVariable() {
            return type == TokenType.VARIABLE;
        }

        // The key without its scope prefix, as Misc.Token splits "$scope.key" at the first dot.
        String key() {
            int dot = text.indexOf('.');
            return dot > 0 && dot < text.length() - 1 ? "$" + text.substring(dot + 1) : text;
        }
    }

    final String source;
    String error;
    Operator operator;
    String command;
    List<Token> params = List.of();
    Token first;
    Token second;

    private RuleExpression(String source) {
        this.source = source;
    }

    boolean isCommand(String name) {
        return error == null && name.equals(command);
    }

    // A parse error is a load error: the game stops loading rules.csv.
    static RuleExpression parse(String line) {
        RuleExpression expression = new RuleExpression(line.trim());
        try {
            expression.build();
        } catch (RuntimeException e) {
            expression.error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            expression.operator = null;
            expression.command = null;
            expression.params = List.of();
            expression.first = null;
            expression.second = null;
        }
        return expression;
    }

    private void build() {
        List<Token> tokens = tokenize(source);
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("no tokens; a line that holds only spaces stops the file loading");
        }
        Token last = tokens.get(tokens.size() - 1);
        if (last.type == TokenType.LITERAL && last.text.startsWith("score:")) {
            Integer.parseInt(last.text.substring(6));
            tokens.remove(tokens.size() - 1);
        }
        for (Token token : tokens) {
            if (token.type != TokenType.OPERATOR) continue;
            Operator found = Operator.of(token.text);
            if (found != null && operator == null) {
                operator = found;
            } else if (found != null) {
                throw new IllegalArgumentException("multiple operators");
            }
        }
        if (operator == Operator.NOT) {
            if (!tokens.get(1).isVariable()) {
                command = tokens.get(1).text;
                params = new ArrayList<>(tokens.subList(2, tokens.size()));
            } else {
                first = tokens.get(1);
            }
        } else if (!tokens.get(0).isVariable()) {
            if (operator != null) {
                throw new IllegalArgumentException("operators other than ! are not allowed in a command line");
            }
            command = tokens.get(0).text;
            params = new ArrayList<>(tokens.subList(1, tokens.size()));
        } else if (operator != null) {
            first = tokens.get(0);
            if (operator != Operator.INCREMENT && operator != Operator.DECREMENT) {
                second = tokens.get(2);
            }
        } else {
            first = tokens.get(0);
        }
    }

    // Port of Misc.tokenize: a token ends at a space or tab outside quotes, at an unescaped quote, and where
    // operator characters start or end; a backslash escapes the next character and "\n" becomes a line break.
    static List<Token> tokenize(String string) {
        List<Token> result = new ArrayList<>();
        boolean inQuote = false;
        boolean inOperator = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            char next = i + 1 < string.length() ? string.charAt(i + 1) : 0;
            boolean escaped = false;
            if (c == '\\') {
                i++;
                if (i >= string.length()) {
                    throw new IllegalArgumentException("escape character at end of line");
                }
                c = string.charAt(i);
                next = i + 1 < string.length() ? string.charAt(i + 1) : 0;
                escaped = true;
            }
            if (c == '"' && !escaped) {
                inQuote = !inQuote;
                if (!inQuote && current.length() == 0) {
                    result.add(new Token("", TokenType.LITERAL));
                } else if (current.length() > 0) {
                    result.add(inQuote ? classify(current.toString(), inOperator) : new Token(current.toString(), TokenType.LITERAL));
                }
                inOperator = false;
                current.setLength(0);
                continue;
            }
            if (!inQuote && (c == ' ' || c == '\t')) {
                if (current.length() > 0) {
                    result.add(classify(current.toString(), inOperator));
                }
                inOperator = false;
                current.setLength(0);
                continue;
            }
            if (!inQuote && !inOperator && isOperatorChar(c) && (c != '-' || !isDigit(next))) {
                if (current.length() > 0) {
                    result.add(classify(current.toString(), false));
                }
                current.setLength(0);
                inOperator = true;
                current.append(escaped && c == 'n' ? '\n' : c);
                continue;
            }
            if (!inQuote && inOperator && !isOperatorChar(c)) {
                if (current.length() > 0) {
                    result.add(new Token(current.toString(), TokenType.OPERATOR));
                }
                current.setLength(0);
                inOperator = false;
                current.append(escaped && c == 'n' ? '\n' : c);
                continue;
            }
            current.append(escaped && c == 'n' ? '\n' : c);
        }
        if (inQuote) {
            throw new IllegalArgumentException("unmatched quotes");
        }
        if (current.length() > 0) {
            result.add(classify(current.toString(), inOperator));
        }
        return result;
    }

    private static Token classify(String text, boolean inOperator) {
        if (text.startsWith("$")) return new Token(text, TokenType.VARIABLE);
        return new Token(text, inOperator ? TokenType.OPERATOR : TokenType.LITERAL);
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private static boolean isOperatorChar(char c) {
        return "=<>!+-".indexOf(c) >= 0;
    }
}
