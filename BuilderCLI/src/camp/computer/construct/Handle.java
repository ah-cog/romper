package camp.computer.construct;

import java.util.UUID;

public class Handle {

    public long uid = -1L; // Manager_v1.elementCounter++; // manager/cache UID

    public UUID uuid = UUID.randomUUID(); // universal handle (unique among all in central repo)

    public static boolean isType(Handle handle) {
        return (handle != null && handle.getClass() == Type.class);
    }

    public static boolean isStructure(Handle handle) {
        return (handle != null && handle.getClass() == Reference.class && ((Reference) handle).object.getClass() == Structure.class);
    }

    public static Type getType(Handle handle) {
        if (Handle.isType(handle)) {
            return (Type) handle;
        }
        return null;
    }

    public static Structure getStructure(Handle handle) {
        if (Handle.isStructure(handle)) {
            return (Structure) ((Reference) handle).object;
        }
        return null;
    }

}
