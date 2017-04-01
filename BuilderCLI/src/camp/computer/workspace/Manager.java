package camp.computer.workspace;

import java.util.HashMap;
//import java.util.List;
import java.util.Map;

import camp.computer.construct.Handle;
import camp.computer.construct.Type;
import camp.computer.construct.Structure;
import camp.computer.construct.Feature;
import camp.computer.util.List;

public class Manager {

    public static long elementCounter = 0L;

//    private static HashMap<Long, Type> elements = new HashMap<>();
    private static HashMap<Long, Handle> elements = new HashMap<>();

    public static long add(Handle handle) {
        long uid = Manager.elementCounter++;
        handle.uid = uid;
        Manager.elements.put(uid, handle);
        return uid;
    }

    public static List<Handle> get() {
        return new List<>(elements.values());
    }

    public static <T extends Handle> List<T> get(Class classType) {
        List<T> constructList = new List<>();
        for (Handle handle : elements.values()) {
            if (handle.getClass() == classType) {
                constructList.add((T) handle);
            }
        }
        return constructList;
    }

//    public static Type request(long uid) {
    public static Handle get(long uid) {
        return elements.get(uid);
    }

//    // Retrieves State from persistent store if it exists! Also caches it!
//    // If the State does not exist (in cache or persistent store), then returns null.
//    public static State getPersistentState(String expression) {
//
//        TypeId stateType = TypeId.request(expression);
//        if (stateType != null) {
//
//            if (stateType == TypeId.request("none")) {
//                // Look for existing (persistent) state for the given expression
//                List<Handle> identiferList = Manager.request();
//                for (int i = 0; i < identiferList.size(); i++) {
//                    if (identiferList.request(i).getClass() == State.class) {
//                        State state = (State) identiferList.request(i);
//                        if (state.classType == TypeId.request("none") && state.objectType == null && state.object == null) {
//                            return state;
//                        }
//                    }
//                }
//            } else if (stateType == TypeId.request("text")) {
//                // e.g.,
//                // [ ] 'foo'
//                // [ ] text('foo')
//                // [ ] text(id:234)
//
//                // Look for existing (persistent) state for the given expression
//                List<Handle> identiferList = Manager.request();
//                for (int i = 0; i < identiferList.size(); i++) {
//                    if (identiferList.request(i).getClass() == State.class) {
//                        State state = (State) identiferList.request(i);
//                        String textContent = expression.substring(1, expression.length() - 1);
//                        if (state.classType == TypeId.request("text") && state.objectType == String.class && textContent.equals(state.object)) {
//                            return state;
//                        }
//                    }
//                }
//            } else if (stateType == TypeId.request("list")) {
//
//                // TODO: Look for permutation of a list?
//
//            } else {
//
//                if (Expression.isAddress(expression)) {
//
//                    String typeIdentifierToken = expression.substring(0, expression.indexOf("(")).trim(); // text before '('
//                    String addressTypeToken = expression.substring(expression.indexOf("(") + 1, expression.indexOf(":")).trim(); // text between '(' and ':'
//                    String addressToken = expression.substring(expression.indexOf(":") + 1, expression.indexOf(")")).trim(); // text between ':' and ')'
//
//                    long uid = Long.parseLong(addressToken.trim());
//
//                    Handle handle = Manager.request(uid);
////                    if (handle != null) {
////                        if (handle.getClass() == Structure.class) {
////                            State state = State.getState(stateType);
////                            state.object = handle;
////                            return state;
////                        }
////                    }
//
//
//                    // Look for existing (persistent) state for the given expression
//                    if (handle != null) {
//                        List<Handle> identiferList = Manager.request();
//                        for (int i = 0; i < identiferList.size(); i++) {
//                            if (identiferList.request(i).getClass() == State.class) {
//                                State state = (State) identiferList.request(i);
////                            String textContent = expression.substring(1, expression.length() - 1);
//                                // TODO: Also check TypeId?
//                                if (state.objectType == Structure.class && state.object != null
//                                        && state.object == handle) {
//                                    return state;
//                                }
//                            }
//                        }
//                    }
//
//                }
//            }
//        }
//
//        return null;
//    }

//    public static Structure getPersistentConstruct(TypeId type) {
//
//        if (type != null) {
//
//            if (type == TypeId.request("none")) {
//                // Look for existing (persistent) state for the given expression
//                List<Handle> identiferList = Manager.get();
//                for (int i = 0; i < identiferList.size(); i++) {
//                    if (identiferList.get(i).getClass() == Structure.class) {
//                        Structure structure = (Structure) identiferList.get(i);
//                        if (structure.type == TypeId.request("none") && structure.objectType == null && structure.object == null) {
//                            return structure;
//                        }
//                    }
//                }
//            } else if (type == TypeId.request("text")) {
//                // e.g.,
//                // [ ] 'foo'
//                // [ ] text('foo')
//                // [ ] text(id:234)
//
//                // Search for persistent "empty text" object (i.e., the default text).
//                List<Handle> identiferList = Manager.get();
//                for (int i = 0; i < identiferList.size(); i++) {
//                    if (identiferList.get(i).getClass() == Structure.class) {
//                        Structure structure = (Structure) identiferList.get(i);
//                        String textContentDefault = "";
//                        if (structure.type == TypeId.request("text") && structure.objectType == String.class && textContentDefault.equals(structure.object)) {
////                        if (structure.classType == TypeId.request("text") && structure.objectType == String.class && ((textContent == null && structure.object == null) || textContent.equals(structure.object))) {
//                            return structure;
//                        }
//                    }
//                }
//            } else if (type == TypeId.request("list")) {
//
//                // TODO: Same existence-checking procedure as for construct? (i.e., look up "list(id:34)")
//                // TODO: Also support looking up by construct permutation contained in list?
//
//                // Look for persistent "empty list" object (i.e., the default list).
//                List<Handle> identiferList = Manager.get();
//                for (int i = 0; i < identiferList.size(); i++) {
//                    if (identiferList.get(i).getClass() == Structure.class) {
//                        Structure structure = (Structure) identiferList.get(i);
//                        if (structure.type == TypeId.request("list") && structure.objectType == List.class && structure.object != null && ((List) structure.object).size() == 0) {
//                            // TODO: Look for permutation of a list (matching list of constructs)?
//                            return structure;
//                        }
//                    }
//                }
//
//            } else {
//
//                Type type = Type.request(type.handle);
//
//                // Look for persistent "empty list" object (i.e., the default list).
//                List<Handle> identiferList = Manager.get();
//                for (int i = 0; i < identiferList.size(); i++) {
//                    if (identiferList.get(i).getClass() == Structure.class) {
//                        Structure structure = (Structure) identiferList.get(i);
//                        if (structure.type == type && structure.objectType == Map.class && structure.object != null) {
//
//                            // Check (1) if structure is based on the specified type, and
//                            //       (2) same set of features as type and assignments to default constructs.
//                            HashMap<String, Feature> constructFeatures = (HashMap<String, Feature>) structure.object;
//
//                            // Compare identifer, types, domain, listTypes
//                            // TODO: Move comparison into Type.hasConstruct(type, structure);
//                            boolean isConstructMatch = true;
//                            if (constructFeatures.size() != type.features.size()) {
//                                isConstructMatch = false;
//                            } else {
//
//                                for (String featureIdentifier : type.features.keySet()) {
//                                    if (!constructFeatures.containsKey(featureIdentifier)
//                                            || !constructFeatures.containsValue(type.features.get(featureIdentifier))) {
//                                        isConstructMatch = false;
//                                    }
//                                }
//
//                                // TODO: Additional checks...
//
//                            }
//
//                            if (isConstructMatch) {
//                                return structure;
//                            }
//
//
//                            // TODO: Look for permutation of a list (matching list of constructs)?
//                            return structure;
//                        }
//                    }
//                }
//
//                // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Type used to create it).
//
//            }
//        }
//
//        return null;
//    }

    public static Structure getPersistentConstruct(Type type) {

//        Type type = Type.request(concept.identifier);

        if (type != null) {

            if (type == Type.request("none")) {
                // Look for existing (persistent) state for the given expression
                List<Handle> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Structure.class) {
                        Structure structure = (Structure) identiferList.get(i);
                        if (structure.type == Type.request("none") && structure.objectType == null && structure.object == null) {
                            return structure;
                        }
                    }
                }
            } else if (type == Type.request("text")) {
                // e.g.,
                // [ ] 'foo'
                // [ ] text('foo')
                // [ ] text(id:234)

                // Search for persistent "empty text" object (i.e., the default text).
                List<Handle> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Structure.class) {
                        Structure structure = (Structure) identiferList.get(i);
                        String textContentDefault = "";
                        if (structure.type == Type.request("text") && structure.objectType == String.class && textContentDefault.equals(structure.object)) {
//                        if (structure.classType == TypeId.request("text") && structure.objectType == String.class && ((textContent == null && structure.object == null) || textContent.equals(structure.object))) {
                            return structure;
                        }
                    }
                }
            } else if (type == Type.request("list")) {

                // TODO: Same existence-checking procedure as for construct? (i.e., look up "list(id:34)")
                // TODO: Also support looking up by construct permutation contained in list?

                // Look for persistent "empty list" object (i.e., the default list).
                List<Handle> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Structure.class) {
                        Structure structure = (Structure) identiferList.get(i);
                        if (structure.type == Type.request("list") && structure.objectType == List.class && structure.object != null && ((List) structure.object).size() == 0) {
                            // TODO: Look for permutation of a list (matching list of constructs)?
                            return structure;
                        }
                    }
                }

            } else {

//                Type type = Type.request(type);

                // Look for persistent "empty list" object (i.e., the default list).
                List<Handle> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Structure.class) {
                        Structure structure = (Structure) identiferList.get(i);

                        // TODO: Update type check to also check the Type?
                        if (structure.type == type && structure.objectType == Map.class && structure.object != null) {

                            // Check (1) if structure is based on the specified type, and
                            //       (2) same set of features as type and assignments to default constructs.
                            HashMap<String, Feature> constructFeatures = (HashMap<String, Feature>) structure.object;

                            // Compare identifer, types, domain, listTypes
                            // TODO: Move comparison into Type.hasConstruct(type, structure);
                            boolean isConstructMatch = true;
                            if (constructFeatures.size() != type.features.size()) {
                                isConstructMatch = false;
                            } else {

                                for (String featureIdentifier : type.features.keySet()) {
                                    if (!constructFeatures.containsKey(featureIdentifier)
                                            || !constructFeatures.containsValue(type.features.get(featureIdentifier))) {
                                        isConstructMatch = false;
                                    }
                                }

                                // TODO: Additional checks...

                            }

                            if (isConstructMatch) {
                                return structure;
                            }


                            // TODO: Look for permutation of a list (matching list of constructs)?
                            return structure;
                        }
                    }
                }

                // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Type used to create it).

            }
        }

        return null;
    }

//    public static Structure getPersistentListConstruct(List constructList) {
//
//        TypeId type = TypeId.request("list");
//
//        // Look for persistent "empty list" object (i.e., the default list).
//        List<Handle> identiferList = Manager.request();
//        for (int i = 0; i < identiferList.size(); i++) {
//            if (identiferList.request(i).getClass() == Structure.class) {
//                Structure candidateConstruct = (Structure) identiferList.request(i);
//
//                if (candidateConstruct.type == type && candidateConstruct.objectType == List.class && candidateConstruct.object != null) {
//                    // LIST
//
//
//                    // Check (1) if constructs are based on the same specified type version, and
//                    //       (2) same list of constructs.
//                    List candidateConstructList = (List) candidateConstruct.object;
////                    List currentConstructList = (List) currentConstruct.object;
//                    List currentConstructList = constructList;
//
//                    // Compare identifer, types, domain, listTypes
//                    // TODO: Move comparison into Type.hasConstruct(type, construct);
//                    boolean isConstructMatch = true;
//                    if (candidateConstructList.size() != currentConstructList.size()) {
//                        isConstructMatch = false;
//                    } else {
//
//                        // Compare candidate list (from repository) with the requested list.
//                        for (int j = 0; j < currentConstructList.size(); j++) {
//                            if (!candidateConstructList.contains(currentConstructList.request(j))) {
//                                isConstructMatch = false;
//                            }
//                        }
//
////                        // Compare candidate construct (from repository) with the current construct being updated.
////                        for (String featureIdentifier : currentConstructFeatures.keySet()) {
////                            if (featureIdentifier.equals(featureToReplace)) {
////                                if (!candidateConstructFeatures.containsKey(featureIdentifier)
////                                        || !candidateConstruct.states.containsKey(featureIdentifier)
////                                        || candidateConstruct.states.request(featureIdentifier) != featureConstructReplacement) {
//////                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
////                                    isConstructMatch = false;
////                                }
////                            } else {
////                                if (!candidateConstructFeatures.containsKey(featureIdentifier)
////                                        || !candidateConstruct.states.containsKey(featureIdentifier)
////                                        || candidateConstruct.states.request(featureIdentifier) != currentConstruct.states.request(featureIdentifier)) {
//////                                        || !candidateConstructFeatures.containsValue(type.features.request(featureIdentifier))) {
////                                    isConstructMatch = false;
////                                }
////                            }
////                        }
////
////                        // TODO: Additional checks...
//
//                    }
//
//                    if (isConstructMatch) {
//                        return candidateConstruct;
//                    }
//
//
//                    // TODO: Look for permutation of a list (matching list of constructs)?
////                    return construct;
//
//                }
//            }
//        }
//
//        // Create new Structure if got to this point because an existing one was not found
////        Structure newReplacementConstruct = Structure.create(currentConstruct, featureToReplace, featureConstructReplacement);
//        Structure newReplacementConstruct = Structure.REFACTOR_getList(constructList);
//        if (newReplacementConstruct != null) {
//            return newReplacementConstruct;
//        }
//
//        // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Type used to create it).
//        return null;
//
//    }


    // TODO: Move to Structure.list(Type)
    public static List<Structure> getStructureList(Type type) {

//        Type type2 = Type.request(type.handle);

        List<Structure> structureList = new List<>();
        for (Handle handle : elements.values()) {
            if (handle.getClass() == Structure.class) {
                if (((Structure) handle).type == type) {
                    structureList.add((Structure) handle);
                }
            }
        }
        return structureList;
    }

//    public static List<Handle> request(Class classType) {
//        List<Handle> elements = new ArrayList<>();
//        for (Handle element : Manager.elements.values()) {
//            if (element.getClass() == classType) {
//                elements.add(element);
//            }
//        }
//        return elements;
//    }

//    public static Type request(String constructUri) {
//    public static Handle get(String constructUri) {
//
//        // Parse:
//        // 3
//        // "foo"
//        // uid(44)
//        // uuid(a716a27b-8489-4bae-b099-2bc73e963876)
//
//        // edit port(uid:25)         # _global_ lookup by UID
//        // edit port(uuid:<uuid>)    # _global_ lookup by UUID
//        // edit port(1)              # _relative_ lookup list item by index
//        // edit my-OLD_construct-handle     # _global?_ lookup by handle
//        // edit :device(1):port(1)   # explicit "full path" lookup prefixed by ":" indicating "from workspace..."
//        //
//        // edit port(my-handle)              # _relative_ lookup list item by list handle and element handle?
//        // edit port                # lookup by property label
//
////        if (handle.startsWith("port")) {
////
////        }
//
//        if (constructUri.startsWith("project")
//                || constructUri.startsWith("device")
//                || constructUri.startsWith("port")
//                || constructUri.startsWith("path")
//                || constructUri.startsWith("task")
//                || constructUri.startsWith("script")) {
//
////        if (constructUri.startsWith("\"") && constructUri.endsWith("\"")) {
////
////
////
////        } else {
//
//            String type = constructUri.substring(0, constructUri.indexOf("("));
//
//            String identifierDeclaration = constructUri.substring(constructUri.indexOf("(") + 1, constructUri.indexOf(")"));
//
//            String identifierType = identifierDeclaration.split(":")[0];
//            String identifier = identifierDeclaration.split(":")[1];
//
//            if (identifierType.equals("uid")) {
//
//                long inputTaskUid = Long.valueOf(identifier);
//
//                if (Manager.elements.containsKey(inputTaskUid)) {
//                    return Manager.elements.get(inputTaskUid);
//                }
//
//            } else if (identifierType.equals("uuid")) {
//
//                UUID inputTaskUuid = UUID.fromString(identifier);
//
//                for (int i = 0; i < Manager.elements.size(); i++) {
//                    if (Manager.elements.get(i).uuid.equals(inputTaskUuid)) {
//                        return Manager.elements.get(i);
//                    }
//                }
//
//            } else {
//
//                // TODO: Lookup by index.
//
//            }
//
//        } else {
//
////            String handle = constructUri.substring(1, constructUri.length() - 1);
//            String title = String.valueOf(constructUri);
//
//            List<Handle> addresses = new ArrayList<>(elements.values());
//
////            for (long uid : elements.keySet()) {
////                Type OLD_construct = elements.clone(uid);
////                if (OLD_construct.handle != null && OLD_construct.handle.equals(handle)) {
////                    return OLD_construct;
////                }
////            }
//
//            for (int i = 0; i < addresses.size(); i++) {
//                Handle handle = addresses.get(i);
//                if (handle.tag != null && handle.tag.equals(title)) {
//                    return handle;
//                }
//            }
//
//        }
//
//        return null;
//
//    }

    public static Handle remove(long uid) {

        if (elements.containsKey(uid)) {
            return elements.remove(uid);
        }

        return null;

    }

}
