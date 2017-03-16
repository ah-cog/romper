package camp.computer.construct;

import java.util.ArrayList;
import java.util.List;

import camp.computer.Context;

public class Expression {

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
            if (i == 0) {
                continue;
            }
            if (context.references.containsKey(tokens[i])) {
                // TODO: Check for type error!
                Construct construct = (Construct) context.references.get(tokens[i]).object;
                tokens[i] = construct.toString();
            }
        }
        expression = String.join(" ", tokens);

        // TODO: validate expression after sanitization

        return new Expression(expression);
    }

    public static List<String> tokenize(String expression) {

        // type port
        // has mode text
        // !has mode
        // has mode text : 'digital', 'analog', ...
        // let ...
        //
        // port
        // port (id: 34)
        //
        // port source-port
        // source-port
        // set mode 'digital'
        // set mode text (id: 34)
        //
        // port.id.34
        // port.id.34.text 'digital'
        //
        // source-port.text 'digital'
        // source-port.text none
        //
        // !source-port

        boolean hasUndo = false; // i.e., expression starts with '!'

        List<String> tokens = new ArrayList<>();

        int i = -1, j = -1;
        for (i = 0; i < expression.length(); i++) {

            // Extract first token if it hasn't already been extracted.
            if (i == -1 && j == -1) {

            }
        }

        return tokens;

    }

    /**
     * Matches expressions identifying constructs such as "port(id: 34)" and reasonable
     * equivalent expressions.
     *
     * @param expression
     * @return
     */
    public static boolean isConstruct(String expression) {
        return expression.matches("([a-z]+)[ ]*\\([ ]*(id|uid|uuid)[ ]*:[ ]*[0-9]+[ ]*\\)");
    }

//    public static boolean isText(String featureContent) {
//        if (!featureContent.startsWith("'") || !featureContent.endsWith("'")) {
//            return false;
//        }
//        return true;
//    }

}
