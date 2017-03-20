package camp.computer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import camp.computer.construct.Address;
import camp.computer.construct.Type;
import camp.computer.construct.Expression;
import camp.computer.construct.Reference;

public class Context {

    /**
     * The sequence of expressions that have been issued from this {@code Context}.
     *
     * Included on the timeline is the sequence of expressions previously issued from the context.
     */
    public List<String> expressionTimeline = new ArrayList<>();

    /**
     * Current list of references that exist in the context.
     */
    public HashMap<String, Reference> references = new HashMap<>();

    public HashMap<String, Type> conceptReferences = new HashMap<>(); // current concepts to use

    public String expression = null;

    public Address address = null;

    public static Expression setExpression(Context context, String expressionText) {

        Expression expression = Expression.create(expressionText, context);
//        System.out.println(">>> NEW EXPRESSION: " + expression.expression);

        // Store expression
        context.expression = expression.expression;

        return expression;

    }
}
