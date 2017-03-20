package camp.computer.construct;

import camp.computer.workspace.Manager;

public class Reference extends Identifier {

    // TODO: OWNER/USER (has a timeline of these?)

    // This is the reference ROOT construct
    public Class classType = null; // Type.class or Structure.class
    public Object object = null; // Type or Structure

    // TODO?: identifier string?
    // TODO: type list (restricts types that can be referenced)
    // TODO: domain list (restricts constructs that can be referenced)

    private Reference() {
    }

    public static Reference get(long id, long revisionUid) {

        Reference reference = new Reference();

        // TODO: Loads (and instantiates immediately?) the specified reference from the persistent store.

        return reference;
    }

//    public static Reference getReference(TypeId type) {
//
//        Reference reference = new Reference();
//
//        // TODO: Load (most recent revision) of default type or construct for the specified type.
//
//        return reference;
//
//    }

    public static Reference create(Structure structure) {

        Reference reference = new Reference();

        // TODO: Load (most recent revision) of default type or structure for the specified type.
        reference.classType = Structure.class;
        reference.object = structure;

        long uid = Manager.add(reference);

        return reference;

    }

//    public static Reference getReference(TypeId type, long id) {
//
//        Reference reference = new Reference();
//
//        // TODO: Load the specified version of the type or construct for the specified type.
//
//        return reference;
//
//    }

//    public static Reference getReference(TypeId type, long id, long revisionUid) {
//
//        Reference reference = new Reference();
//
//        // TODO: Load specified type or construct from cache (or persistent store).
//
//        return reference;
//    }

//    public static Reference updateChild(Reference reference, ) {
//
//    }

    public String toString() {
        if (this.object != null) {
            if (this.object.getClass() == Structure.class) {
                Structure structure = (Structure) this.object;
                return "reference " + structure.type2.identifier + ".id." + uid + " -> structure " + structure.type2.identifier + ".id." + structure.uid;
            }
        }
        return null; // Reference points to "any"
    }

    public String toColorString() {
        if (this.object != null) {
            if (this.object.getClass() == Structure.class) {
                Structure structure = (Structure) this.object;
                return structure.type2.toColorString() + ".id." + uid + " -> " + structure.type2.toColorString() + ".id." + structure.uid;
            }
        }
        return null; // Reference points to "any"
    }

}
