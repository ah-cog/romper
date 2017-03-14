package camp.computer;

import java.util.HashMap;

import camp.computer.construct.Concept;
import camp.computer.construct.Construct;
import camp.computer.construct.Expression;
import camp.computer.construct.Identifier;
import camp.computer.construct.Reference;

public class Context {

    public HashMap<String, Reference> references = new HashMap<>();

    public String expression = null;

    public Identifier identifier = null;

    public static Expression setExpression(Context context, String expressionText) {

        Expression expression = Expression.create(expressionText, context);
//        System.out.println("NEW EXPRESSION: " + expression.expression);

        // Store expression
        context.expression = expression.expression;

        return expression;

    }
}
