package camp.computer.construct;

import camp.computer.Context;

public class Expression {

    public static String REGEX_ADDRESS_EXPRESSION = "[A-Za-z]+\\.(id|uuid)=[0-9]+";
    public static String REGEX_TEXT_EXPRESSION = "^'[A-Za-z0-9 -_]*'$";

    public String expression = null;

    private Expression(String expression) {
        this.expression = expression;
    }

    /**
     * Creates {@code Expression} in normalized format.
     * @param expression
     * @return
     */
    public static Expression create(String expression, Context context) {

        // Sanitize expression
        expression = expression.trim();
        expression = expression.replaceAll("[ ]{2,}", " ");
        expression = expression.replaceAll("[ ]+\\(", "(");
        expression = expression.replaceAll("[ ]+:[ ]+", ":");

        // Replace label parameter tokens with address tokens (not first token)
        String[] tokens = expression.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            if (i == 0 || Type.exists(tokens[0])) {
                continue;
            }
//            if (context.references.containsKey(tokens[i])) {
//                // TODO: Check for type error!
//                Structure structure = (Structure) context.references.get(tokens[i]).object;
//                tokens[i] = structure.toString();
//            }
        }
        expression = String.join(" ", tokens);

        // TODO: validate expression after sanitization

        return new Expression(expression);
    }

    /**
     * Matches expressions identifying constructs such as "port.id=34" and reasonable
     * equivalent expressions.
     *
     * @param expression
     * @return
     */
    public static boolean isAddress(String expression) {
        // return expression.matches("([a-z]+)[ ]*\\([ ]*(id|uid|uuid)[ ]*:[ ]*[0-9]+[ ]*\\)");
        // return expression.matches("[A-Za-z]+\\.(id|uuid)\\.[0-9]+");
        return expression.matches(REGEX_ADDRESS_EXPRESSION);
    }

    public static boolean isText(String expression) {
        return expression.matches(REGEX_TEXT_EXPRESSION);
    }
}
