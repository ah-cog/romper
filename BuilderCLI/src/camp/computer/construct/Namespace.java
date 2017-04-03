package camp.computer.construct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * TODO: Rename to Workspace and provide namespace implicitly, in addition to history, etc.?
 */
public class Namespace {

    private static HashMap<String, Namespace> contextList = new HashMap<>();

    public String identifer = null;

    /**
     * The sequence of expressions that have been issued from this {@code Namespace}.
     *
     * Included on the timeline is the sequence of expressions previously issued from the context.
     */
    public List<String> expressionTimeline = new ArrayList<>();

    /**
     * Current list of references that exist in the context.
     */
    public HashMap<String, Reference> references = new HashMap<>();

    public String expression = null;

    public Resource resource = null;

    private Namespace() {
    }

    public static Namespace request(String identifier) {
        if (Namespace.contextList.containsKey(identifier)) {
            return Namespace.contextList.get(identifier);
        } else {
            Namespace namespace = new Namespace();
            namespace.identifer = identifier;
            Namespace.contextList.put(identifier, namespace);
            return namespace;
        }
    }

    public static Expression setExpression(Namespace namespace, String expressionText) {

        Expression expression = Expression.create(expressionText, namespace);
//        System.out.println(">>> NEW EXPRESSION: " + expression.expression);

        // Store expression
        namespace.expression = expression.expression;

        // Save line in history
        namespace.expressionTimeline.add(expressionText);

        return expression;

    }
}
