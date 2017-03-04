package camp.computer.workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import camp.computer.OLD_construct.Construct_v1;

public class Manager_v1 {

    public static long elementCounter = 0L;

    public static HashMap<Long, Construct_v1> elements = new HashMap<>();

    public static Construct_v1 get(String constructUri) {

        // Parse:
        // 3
        // "foo"
        // uid(44)
        // uuid(a716a27b-8489-4bae-b099-2bc73e963876)

        // edit port(uid:25)         # _global_ lookup by UID
        // edit port(uuid:<uuid>)    # _global_ lookup by UUID
        // edit port(1)              # _relative_ lookup list item by index
        // edit my-OLD_construct-identifier     # _global?_ lookup by identifier
        // edit :device(1):port(1)   # explicit "full path" lookup prefixed by ":" indicating "from workspace..."
        //
        // edit port(my-identifier)              # _relative_ lookup list item by list identifier and element identifier?
        // edit port                # lookup by property label

//        if (address.startsWith("port")) {
//
//        }

        if (constructUri.startsWith("project")
                || constructUri.startsWith("device")
                || constructUri.startsWith("port")
                || constructUri.startsWith("path")
                || constructUri.startsWith("task")
                || constructUri.startsWith("script")) {

//        if (constructUri.startsWith("\"") && constructUri.endsWith("\"")) {
//
//
//
//        } else {

            String type = constructUri.substring(0, constructUri.indexOf("("));

            String identifierDeclaration = constructUri.substring(constructUri.indexOf("(") + 1, constructUri.indexOf(")"));

            String identifierType = identifierDeclaration.split(":")[0];
            String identifier = identifierDeclaration.split(":")[1];

            if (identifierType.equals("uid")) {

                long inputTaskUid = Long.valueOf(identifier);

                if (Manager_v1.elements.containsKey(inputTaskUid)) {
                    return Manager_v1.elements.get(inputTaskUid);
                }

            } else if (identifierType.equals("uuid")) {

                UUID inputTaskUuid = UUID.fromString(identifier);

                for (int i = 0; i < Manager_v1.elements.size(); i++) {
                    if (Manager_v1.elements.get(i).uuid.equals(inputTaskUuid)) {
                        return Manager_v1.elements.get(i);
                    }
                }

            } else {

                // TODO: Lookup by index.

            }

        } else {

//            String identifier = constructUri.substring(1, constructUri.length() - 1);
            String title = String.valueOf(constructUri);

            List<Construct_v1> constructs = new ArrayList<>(elements.values());

//            for (long uid : elements.keySet()) {
//                Concept OLD_construct = elements.clone(uid);
//                if (OLD_construct.identifier != null && OLD_construct.identifier.equals(identifier)) {
//                    return OLD_construct;
//                }
//            }

            for (int i = 0; i < constructs.size(); i++) {
                Construct_v1 construct = constructs.get(i);
                if (construct.title != null && construct.title.equals(title)) {
                    return construct;
                }
            }

        }

        return null;

    }

    public static Construct_v1 remove(long uid) {

        if (elements.containsKey(uid)) {
            return elements.remove(uid);
        }

        return null;

    }

}
