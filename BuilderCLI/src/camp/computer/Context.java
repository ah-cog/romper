package camp.computer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import camp.computer.construct.Address;
import camp.computer.construct.Type;
import camp.computer.construct.Expression;
import camp.computer.construct.Reference;

public class Context {

    private static HashMap<String, Context> contextList = new HashMap<>();

    public String identifer = null;

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

    public String expression = null;

    public Address address = null;

    private Context() {
    }

    public static Context request(String identifier) {
        if (Context.contextList.containsKey(identifier)) {
            return Context.contextList.get(identifier);
        } else {
            Context context = new Context();
            context.identifer = identifier;
            Context.contextList.put(identifier, context);
            return context;
        }
    }

    public static Expression setExpression(Context context, String expressionText) {

        Expression expression = Expression.create(expressionText, context);
//        System.out.println(">>> NEW EXPRESSION: " + expression.expression);

        // Store expression
        context.expression = expression.expression;

        return expression;

    }
}
