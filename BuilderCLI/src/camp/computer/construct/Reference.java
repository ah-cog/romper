package camp.computer.construct;

import camp.computer.workspace.Manager;

public class Reference extends Identifier {

    // TODO: OWNER/USER (has a timeline of these?)

    // This is the reference ROOT construct
    public Class classType = null; // Concept.class or Construct.class
    public Object object = null; // Concept or Construct

    private Reference() {
    }

    public static Reference get(long id, long revisionUid) {

        Reference reference = new Reference();

        // TODO: Loads (and instantiates immediately?) the specified reference from the persistent store.

        return reference;
    }

    public static Reference getReference(Type type) {

        Reference reference = new Reference();

        // TODO: Load (most recent revision) of default concept or construct for the specified type.

        return reference;

    }

    public static Reference create(Construct construct) {

        Reference reference = new Reference();

        // TODO: Load (most recent revision) of default concept or construct for the specified type.
        reference.classType = Construct.class;
        reference.object = construct;

        long uid = Manager.add(reference);

        return reference;

    }

    public static Reference getReference(Type type, long id) {

        Reference reference = new Reference();

        // TODO: Load the specified version of the concept or construct for the specified type.

        return reference;

    }

    public static Reference getReference(Type type, long id, long revisionUid) {

        Reference reference = new Reference();

        // TODO: Load specified concept or construct from cache (or persistent store).

        return reference;
    }

//    public static Reference updateChild(Reference reference, ) {
//
//    }

}
