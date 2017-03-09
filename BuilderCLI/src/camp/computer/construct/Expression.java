package camp.computer.construct;

public class Expression {

    public String expression = null;

    private Expression(String expression) {
        this.expression = expression;
    }

    public static Expression create(String expression) {
        return new Expression(expression);
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
