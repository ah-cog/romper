package camp.computer.construct;

import java.util.UUID;

public class Address {

    public long uid = -1L; // Manager_v1.elementCounter++; // manager/cache UID

    public UUID uuid = UUID.randomUUID(); // universal address (unique among all in central repo)

    public static boolean isType(Address address) {
        return (address != null && address.getClass() == Type.class);
    }

    public static boolean isStructure(Address address) {
        return (address != null && address.getClass() == Reference.class && ((Reference) address).object.getClass() == Structure.class);
    }

    public static Type getType(Address address) {
        if (Address.isType(address)) {
            return (Type) address;
        }
        return null;
    }

    public static Structure getStructure(Address address) {
        if (Address.isStructure(address)) {
            return (Structure) ((Reference) address).object;
        }
        return null;
    }

}
