package camp.computer.construct;

import java.util.List;

import camp.computer.util.console.Color;
import camp.computer.workspace.Manager;

public class Type extends Identifier {

    public String identifier = null; // types identifier of construct

    private Type(String identifier) {
        this.identifier = identifier;
    }

    public static Type add(String identifier) {
        // Check if <em>type</em> already exists. If so, return it.
        List<Type> typeList = Manager.get(Type.class);
        for (int i = 0; i < typeList.size(); i++) {
            if (typeList.get(i).identifier.equals(identifier)) {
                return typeList.get(i);
            }
        }
        // If <em>type</em> doesn't exist, create and return it.
        Type type = new Type(identifier);
        long uid = Manager.add(type);
        return type;
    }

    public static boolean exists(String identifier) {
        List<Type> typeList = Manager.get(Type.class);
        for (int i = 0; i < typeList.size(); i++) {
            if (typeList.get(i).identifier.equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    public static List<Type> get() {
//        return new ArrayList<>(Type.identifiers.values());
        List<Type> typeList = Manager.get(Type.class);
        return typeList;
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
        if (Type.exists(expression)) {
            List<Type> typeList = Manager.get(Type.class);
            for (int i = 0; i < typeList.size(); i++) {
                if (typeList.get(i).identifier.equals(expression)) {
                    return typeList.get(i);
                }
            }
        } else if (expression.startsWith("'") && expression.endsWith("'")) { // TODO: Update with regex match
            return Type.get("text");
        } else if (expression.contains(",")) { // TODO: Update with regex match
            return Type.get("list");
//        } else if (expression.contains("(") && expression.contains(")")) { // TODO: update with regex match
        } else if (Expression.isConstruct(expression)) {
            String typeTag = expression.substring(0, expression.indexOf("(")).trim();
            List<Type> typeList = Manager.get(Type.class);
            for (int i = 0; i < typeList.size(); i++) {
                if (typeList.get(i).identifier.equals(typeTag)) {
                    return typeList.get(i);
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
//        return identifier + " (id: " + uid + ")";
        return identifier;
    }

    public String toColorString() {
//        return Color.ANSI_BLUE + identifier + Color.ANSI_RESET + " (id: " + this.uid + ")";
        return Color.ANSI_BLUE + identifier + Color.ANSI_RESET;
    }
}
