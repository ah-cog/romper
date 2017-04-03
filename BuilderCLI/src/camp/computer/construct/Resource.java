package camp.computer.construct;

import java.util.UUID;

public class Resource {

    public long uid = -1L; // Manager_v1.elementCounter++; // manager/cache UID

    public UUID uuid = UUID.randomUUID(); // universal resource (unique among all in central repo)

    public static boolean isType(Resource resource) {
        return (resource != null && resource.getClass() == Type.class);
    }

    public static boolean isStructure(Resource resource) {
        return (resource != null && resource.getClass() == Reference.class && ((Reference) resource).object.getClass() == Structure.class);
    }

    public static Type getType(Resource resource) {
        if (Resource.isType(resource)) {
            return (Type) resource;
        }
        return null;
    }

    public static Structure getStructure(Resource resource) {
        if (Resource.isStructure(resource)) {
            return (Structure) ((Reference) resource).object;
        }
        return null;
    }

}
