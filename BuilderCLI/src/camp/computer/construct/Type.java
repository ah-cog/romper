package camp.computer.construct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import camp.computer.util.console.Color;
import camp.computer.workspace.Manager;

public class Type extends Identifier {

    // TODO: Store Type identifiers in Manager_v1
    // TODO: Consider using static Type interface to wrap interface to Manager_v1 for Type-specific operations
    private static HashMap<String, Type> identifiers = new HashMap<>();

    public String identifier = null; // types identifier of construct

    // <TODO?>
    // public List<Domain> domain;
    // </TODO?>

    private Type(String identifier) {
        this.identifier = identifier;
    }

    public static Type add(String identifier) {
        if (Type.identifiers.containsKey(identifier)) {
            return Type.identifiers.get(identifier);
        } else {
            Type type = new Type(identifier);
            Type.identifiers.put(identifier, type);
            long uid = Manager.add(type);
            return type;
        }
    }

    public static boolean has(String identifier) {
        return Type.identifiers.containsKey(identifier);
    }

    public static List<Type> get() {
        return new ArrayList<>(Type.identifiers.values());
    }

    // Type identifiers:
    // text
    // port
    //
    // State expressions:
    // 'my text content'
    // text('my text content')
    // port(id:<uid>)
    // port(uuid:<uuid>)
    // device(id:<uid>)
    // device(uuid:<uuid>)
    public static Type get(String expression) {
        if (Type.identifiers.containsKey(expression)) {
            return Type.identifiers.get(expression);
        } else if (expression.startsWith("'") && expression.endsWith("'")) { // TODO: Update with regex match
            return Type.get("text");
        } else if (expression.contains(",")) { // TODO: Update with regex match
            return Type.get("list");
        } else if (expression.contains("(") && expression.contains(")")) { // TODO: update with regex match
            String typeTag = expression.substring(0, expression.indexOf("("));
            if (Type.identifiers.containsKey(typeTag)) {
                return Type.identifiers.get(typeTag);
            }
//            if (Type.has(typeTag)) {
//                // TODO: Check if specified construct exists
//                return Type.get(typeTag);
//            }
        }
        return null;
    }

    @Override
    public String toString() {
        return identifier;
    }

    public String toColorString() {
        return Color.ANSI_BLUE + identifier + Color.ANSI_RESET + " (id: " + this.uid + ")";
    }
}
