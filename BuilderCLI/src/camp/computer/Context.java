package camp.computer;

import camp.computer.construct.Concept;
import camp.computer.construct.Construct;
import camp.computer.construct.Identifier;
import camp.computer.construct.Reference;

public class Context {

    public String inputLine = null;

    public Identifier currentIdentifier = null;

    public static boolean isConcept(Context context) {
        return (context.currentIdentifier != null && context.currentIdentifier.getClass() == Concept.class);
    }

    public static boolean isConstruct(Context context) {
        return (context.currentIdentifier != null && context.currentIdentifier.getClass() == Reference.class && ((Reference) context.currentIdentifier).object.getClass() == Construct.class);
    }

    public static Concept getConcept(Context context) {
        if (Context.isConcept(context)) {
            return (Concept) context.currentIdentifier;
        }
        return null;
    }

    public static Construct getConstruct(Context context) {
        if (Context.isConstruct(context)) {
            return (Construct) context.currentIdentifier;
        }
        return null;
    }
}
