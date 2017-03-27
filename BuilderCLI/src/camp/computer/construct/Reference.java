package camp.computer.construct;

import camp.computer.util.console.Color;
import camp.computer.workspace.Manager;

public class Reference extends Address {

    // TODO: OWNER/USER (has a timeline of these?)

    // This is the reference ROOT construct
    public Class classType = null; // Type.class or Structure.class
    public Object object = null; // Type or Structure

    // TODO?: address string?
    // TODO: type list (restricts types that can be referenced)
    // TODO: domain list (restricts constructs that can be referenced)

    private Reference() {
    }

    public static Reference create(Object object) {
        if (object.getClass() != Type.class && object.getClass() != Structure.class) {
            System.err.println(Error.get("Can't create reference for " + object.getClass()));
            return null;
        } else {
            Reference reference = new Reference();
            // TODO: Load (most recent revision) of default type or structure for the specified type.
            reference.classType = object.getClass();
            reference.object = object;
            long uid = Manager.add(reference);
            return reference;
        }
    }

    public String toString() {
        if (this.object != null) {
            if (this.object.getClass() == Type.class) {
                Type type = (Type) this.object;
                return "reference " + type.identifier + ".id=" + uid + " -> structure " + type.identifier + ".id=" + type.uid;
            } else if (this.object.getClass() == Structure.class) {
                Structure structure = (Structure) this.object;
                return "reference " + structure.type.identifier + ".id." + uid + " -> structure " + structure.type.identifier + ".id." + structure.uid;
            }
        }
        return null; // Reference points to "any"
    }

    public String toColorString() {
        if (this.object != null) {
            if (this.object.getClass() == Type.class) {
                Type type = (Type) this.object;
                return "" + type.identifier + ".id=" + uid + " -> " + type.identifier + ".id=" + type.uid;
            } else if (this.object.getClass() == Structure.class) {
                Structure structure = (Structure) this.object;
                // return structure.type.toColorString() + ".id." + uid + " -> " + structure.type.toColorString() + ".id." + structure.uid;
                return Color.ANSI_YELLOW + structure.type.identifier + Color.ANSI_RESET + ".id=" + uid + " -> " + Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET + ".id=" + structure.uid;
            }
        }
        return null; // Reference points to "any"
    }

}
