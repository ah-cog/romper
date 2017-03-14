package camp.computer.construct;

import java.util.UUID;

public class Identifier {

    public UUID uuid = UUID.randomUUID(); // universal identifier (unique among all in central repo)
//    public UUID versionUuid = UUID.randomUUID();

    public long uid = -1L; // Manager_v1.elementCounter++; // manager/cache UID
//    public long versionUid = -1L;

    public String tag = null; // label/identifier(s)

    public static boolean isConcept(Identifier identifier) {
        return (identifier != null && identifier.getClass() == Concept.class);
    }

    public static boolean isConstruct(Identifier identifier) {
        return (identifier != null && identifier.getClass() == Reference.class && ((Reference) identifier).object.getClass() == Construct.class);
    }

    public static Concept getConcept(Identifier identifier) {
        if (Identifier.isConcept(identifier)) {
            return (Concept) identifier;
        }
        return null;
    }

    public static Construct getConstruct(Identifier identifier) {
        if (Identifier.isConstruct(identifier)) {
            return (Construct) ((Reference) identifier).object;
        }
        return null;
    }

}
