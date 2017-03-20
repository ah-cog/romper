package camp.computer.construct;

import java.util.UUID;

public class Identifier {

    public UUID uuid = UUID.randomUUID(); // universal identifier (unique among all in central repo)
//    public UUID versionUuid = UUID.randomUUID();

    public long uid = -1L; // Manager_v1.elementCounter++; // manager/cache UID
//    public long versionUid = -1L;

    public String tag = null; // label/identifier(s)

    public static boolean isConcept(Identifier identifier) {
        return (identifier != null && identifier.getClass() == Type.class);
    }

    public static boolean isConstruct(Identifier identifier) {
        return (identifier != null && identifier.getClass() == Reference.class && ((Reference) identifier).object.getClass() == Structure.class);
    }

    public static Type getConcept(Identifier identifier) {
        if (Identifier.isConcept(identifier)) {
            return (Type) identifier;
        }
        return null;
    }

    public static Structure getConstruct(Identifier identifier) {
        if (Identifier.isConstruct(identifier)) {
            return (Structure) ((Reference) identifier).object;
        }
        return null;
    }

}
