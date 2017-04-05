package camp.computer.construct;

import java.util.UUID;

public class Resource {

    public long uid = -1L; // Manager_v1.elementCounter++; // manager/cache UID

    public UUID uuid = UUID.randomUUID(); // universal resource (unique among all in central repo)

    // TODO: Replace with .type() != null
    public static boolean isType(Resource resource) {
        return (resource != null && resource.getClass() == Type.class);
    }

    public static boolean isStructure(Resource resource) {
        return (resource != null && resource.getClass() == Reference.class && ((Reference) resource).object.getClass() == Structure.class);
    }

    /**
     * Returns this {@code Resource} as a {@Type}.
     * @param resource
     * @return
     */
    public static Type type(Resource resource) {
        if (resource != null && resource.getClass() == Type.class) {
            return (Type) resource;
        }
        return null;
    }

    /**
     * Returns a {@code Structure} reference to {@code resource} if it is of type
     * (genesis moment: "ifitisof") {@code Structure}.
     * @param resource
     * @return
     */
    public static Structure structure(Resource resource) {
        if (resource != null && resource.getClass() == Reference.class && ((Reference) resource).object.getClass() == Structure.class) {
            return (Structure) ((Reference) resource).object;
        }
        return null;
    }

}
