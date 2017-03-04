package camp.computer.workspace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import camp.computer.construct.Concept;
import camp.computer.construct.Construct;
import camp.computer.construct.Expression;
import camp.computer.construct.Feature;
import camp.computer.construct.Identifier;
import camp.computer.construct.Type;

public class Manager {

    public static long elementCounter = 0L;

//    private static HashMap<Long, Concept> elements = new HashMap<>();
    private static HashMap<Long, Identifier> elements = new HashMap<>();

    public static long add(Identifier identifier) {
        long uid = Manager.elementCounter++;
        identifier.uid = uid;
        Manager.elements.put(uid, identifier);
        return uid;
    }

    public static List<Identifier> get() {
        return new ArrayList<>(elements.values());
    }

    public static <T extends Identifier> List<T> get(Class classType) {
        List<T> constructList = new ArrayList<>();
        for (Identifier identifier : elements.values()) {
            if (identifier.getClass() == classType) {
                constructList.add((T) identifier);
            }
        }
        return constructList;
    }

//    public static Concept get(long uid) {
    public static Identifier get(long uid) {
        return elements.get(uid);
    }

//    // Retrieves State from persistent store if it exists! Also caches it!
//    // If the State does not exist (in cache or persistent store), then returns null.
//    public static State getPersistentState(String expression) {
//
//        Type stateType = Type.get(expression);
//        if (stateType != null) {
//
//            if (stateType == Type.get("none")) {
//                // Look for existing (persistent) state for the given expression
//                List<Identifier> identiferList = Manager.get();
//                for (int i = 0; i < identiferList.size(); i++) {
//                    if (identiferList.get(i).getClass() == State.class) {
//                        State state = (State) identiferList.get(i);
//                        if (state.classType == Type.get("none") && state.objectType == null && state.object == null) {
//                            return state;
//                        }
//                    }
//                }
//            } else if (stateType == Type.get("text")) {
//                // e.g.,
//                // [ ] 'foo'
//                // [ ] text('foo')
//                // [ ] text(id:234)
//
//                // Look for existing (persistent) state for the given expression
//                List<Identifier> identiferList = Manager.get();
//                for (int i = 0; i < identiferList.size(); i++) {
//                    if (identiferList.get(i).getClass() == State.class) {
//                        State state = (State) identiferList.get(i);
//                        String textContent = expression.substring(1, expression.length() - 1);
//                        if (state.classType == Type.get("text") && state.objectType == String.class && textContent.equals(state.object)) {
//                            return state;
//                        }
//                    }
//                }
//            } else if (stateType == Type.get("list")) {
//
//                // TODO: Look for permutation of a list?
//
//            } else {
//
//                if (Expression.isConstruct(expression)) {
//
//                    String typeIdentifierToken = expression.substring(0, expression.indexOf("(")).trim(); // text before '('
//                    String addressTypeToken = expression.substring(expression.indexOf("(") + 1, expression.indexOf(":")).trim(); // text between '(' and ':'
//                    String addressToken = expression.substring(expression.indexOf(":") + 1, expression.indexOf(")")).trim(); // text between ':' and ')'
//
//                    long uid = Long.parseLong(addressToken.trim());
//
//                    Identifier identifier = Manager.get(uid);
////                    if (identifier != null) {
////                        if (identifier.getClass() == Construct.class) {
////                            State state = State.getState(stateType);
////                            state.object = identifier;
////                            return state;
////                        }
////                    }
//
//
//                    // Look for existing (persistent) state for the given expression
//                    if (identifier != null) {
//                        List<Identifier> identiferList = Manager.get();
//                        for (int i = 0; i < identiferList.size(); i++) {
//                            if (identiferList.get(i).getClass() == State.class) {
//                                State state = (State) identiferList.get(i);
////                            String textContent = expression.substring(1, expression.length() - 1);
//                                // TODO: Also check Type?
//                                if (state.objectType == Construct.class && state.object != null
//                                        && state.object == identifier) {
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

    public static Construct getPersistentConstruct(Type type) {

        if (type != null) {

            if (type == Type.get("none")) {
                // Look for existing (persistent) state for the given expression
                List<Identifier> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Construct.class) {
                        Construct construct = (Construct) identiferList.get(i);
                        if (construct.type == Type.get("none") && construct.objectType == null && construct.object == null) {
                            return construct;
                        }
                    }
                }
            } else if (type == Type.get("text")) {
                // e.g.,
                // [ ] 'foo'
                // [ ] text('foo')
                // [ ] text(id:234)

                // Search for persistent "empty text" object (i.e., the default text).
                List<Identifier> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Construct.class) {
                        Construct construct = (Construct) identiferList.get(i);
                        String textContentDefault = "";
                        if (construct.type == Type.get("text") && construct.objectType == String.class && textContentDefault.equals(construct.object)) {
//                        if (construct.classType == Type.get("text") && construct.objectType == String.class && ((textContent == null && construct.object == null) || textContent.equals(construct.object))) {
                            return construct;
                        }
                    }
                }
            } else if (type == Type.get("list")) {

                // TODO: Same existence-checking procedure as for construct? (i.e., look up "list(id:34)")
                // TODO: Also support looking up by construct permutation contained in list?

                // Look for persistent "empty list" object (i.e., the default list).
                List<Identifier> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Construct.class) {
                        Construct construct = (Construct) identiferList.get(i);
                        if (construct.type == Type.get("list") && construct.objectType == List.class && construct.object != null && ((List) construct.object).size() == 0) {
                            // TODO: Look for permutation of a list (matching list of constructs)?
                            return construct;
                        }
                    }
                }

            } else {

//                if (Expression.isConstruct(expression)) {
//
//                    String typeIdentifierToken = expression.substring(0, expression.indexOf("(")).trim(); // text before '('
//                    String addressTypeToken = expression.substring(expression.indexOf("(") + 1, expression.indexOf(":")).trim(); // text between '(' and ':'
//                    String addressToken = expression.substring(expression.indexOf(":") + 1, expression.indexOf(")")).trim(); // text between ':' and ')'
//
//                    long uid = Long.parseLong(addressToken.trim());
//
//                    Identifier identifier = Manager.get(uid);
////                    if (identifier != null) {
////                        if (identifier.getClass() == Construct.class) {
////                            State state = State.getState(stateType);
////                            state.object = identifier;
////                            return state;
////                        }
////                    }
//
//
//                    // Look for existing (persistent) state for the given expression
//                    if (identifier != null) {
//                        List<Identifier> identiferList = Manager.get();
//                        for (int i = 0; i < identiferList.size(); i++) {
//                            if (identiferList.get(i).getClass() == Construct.class) {
//                                Construct construct = (Construct) identiferList.get(i);
////                            String textContent = expression.substring(1, expression.length() - 1);
//                                // TODO: Also check Type?
//                                if (construct.objectType == Map.class && construct.object != null
//                                        && construct.object == identifier) {
//                                    return construct;
//                                }
//                            }
//                        }
//                    }
//
//                }

//                System.out.println("WHAT SHOULD HAPPEN HERE?");
                // (answer!) Look for persistent "empty list" object (i.e., the default list).

                Concept concept = Concept.get(type);

                // Look for persistent "empty list" object (i.e., the default list).
                List<Identifier> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Construct.class) {
                        Construct construct = (Construct) identiferList.get(i);
                        if (construct.type == type && construct.objectType == Map.class && construct.object != null) {

                            // Check (1) if construct is based on the specified concept, and
                            //       (2) same set of features as concept and assignments to default constructs.
                            HashMap<String, Feature> constructFeatures = (HashMap<String, Feature>) construct.object;

                            // Compare identifer, types, domain, listTypes
                            // TODO: Move comparison into Concept.hasConstruct(concept, construct);
                            boolean isConstructMatch = true;
                            if (constructFeatures.size() != concept.features.size()) {
                                isConstructMatch = false;
                            } else {

                                for (String featureIdentifier : concept.features.keySet()) {
                                    if (!constructFeatures.containsKey(featureIdentifier)
                                            || !constructFeatures.containsValue(concept.features.get(featureIdentifier))) {
                                        isConstructMatch = false;
                                    }
                                }

                                // TODO: Additional checks...

                            }

                            if (isConstructMatch) {
                                return construct;
                            }


                            // TODO: Look for permutation of a list (matching list of constructs)?
                            return construct;
                        }
                    }
                }

                // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Concept used to create it).

            }
        }

        return null;
    }

    /**
     * Returns the persistent {@code Construct}, if exists, that would result from applying
     * {@code expression} to the specified {@code construct}.
     *
     * If no such {@code Construct} exists, returns {@code null}.
     */
//    public static Construct getPersistentConstruct(Construct construct, String expression) {
//    public static Construct getPersistentConstruct(Construct construct, Feature feature, Construct featureConstructReplacement) {
    public static Construct getPersistentConstruct(Construct currentConstruct, String featureToReplace, Construct featureConstructReplacement) {

        Type type = currentConstruct.type; // Construct type

//        Concept concept = Concept.get(type);

        // Look for persistent "empty list" object (i.e., the default list).
        List<Identifier> identiferList = Manager.get();
        for (int i = 0; i < identiferList.size(); i++) {
            if (identiferList.get(i).getClass() == Construct.class) {
                Construct construct = (Construct) identiferList.get(i);
                if (construct.type == type && construct.objectType == Map.class && construct.object != null) {

                    // Check (1) if constructs are based on the same specified concept version, and
                    //       (2) same set of features and assignments to constructs except the specified feature to change.
                    HashMap<String, Feature> candidateConstructFeatures = (HashMap<String, Feature>) construct.object;
                    HashMap<String, Feature> currentConstructFeatures = (HashMap<String, Feature>) currentConstruct.object;

                    // Compare identifer, types, domain, listTypes
                    // TODO: Move comparison into Concept.hasConstruct(concept, construct);
                    boolean isConstructMatch = true;
                    if (candidateConstructFeatures.size() != currentConstructFeatures.size()) {
                        isConstructMatch = false;
                    } else {

                        // Compare candidate construct (from repository) with the current construct being updated.
                        for (String featureIdentifier : currentConstructFeatures.keySet()) {
                            if (featureIdentifier.equals(featureToReplace)) {
                                if (!candidateConstructFeatures.containsKey(featureIdentifier)
                                        || !construct.states.containsKey(featureIdentifier)
                                        || construct.states.get(featureIdentifier) != featureConstructReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
                                    isConstructMatch = false;
                                }
                            } else {
                                if (!candidateConstructFeatures.containsKey(featureIdentifier)
                                        || !construct.states.containsKey(featureIdentifier)
                                        || construct.states.get(featureIdentifier) != currentConstruct.states.get(featureIdentifier)) {
//                                        || !candidateConstructFeatures.containsValue(concept.features.get(featureIdentifier))) {
                                    isConstructMatch = false;
                                }
                            }
                        }

                        // TODO: Additional checks...

                    }

                    if (isConstructMatch) {
                        return construct;
                    }


                    // TODO: Look for permutation of a list (matching list of constructs)?
//                    return construct;
                }
            }
        }

        // Create new Construct if got to this point because an existing one was not found
        Construct newReplacementConstruct = Construct.REFACTOR_getRevise(currentConstruct, featureToReplace, featureConstructReplacement);
        if (newReplacementConstruct != null) {
            return newReplacementConstruct;
        }

        // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Concept used to create it).
        return null;

    }

    /**
     * If the State does not exist (in cache or persistent store), then returns null.
     *
     * Retrieves State from persistent store if it exists! Also caches it!
     *
     * <strong>Examples of {@code Expression}:</strong>
     *
     * none
     *
     * 'foo'
     * text('foo')
     * text(id:34)
     *
     * 66
     * number(66)
     * number(id:12)
     *
     * text(id:34), 'foo', 'bar'
     * list(text(id:34), 'foo', 'bar')
     * list(id:44)
     *
     * port(id:99)
     */
    public static Construct getPersistentConstruct(String expression) {

        Type constructType = Type.get(expression);
        if (constructType != null) {

            if (constructType == Type.get("none")) {
                // Look for existing (persistent) state for the given expression
                List<Identifier> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Construct.class) {
                        Construct construct = (Construct) identiferList.get(i);
                        if (construct.type == Type.get("none") && construct.objectType == null && construct.object == null) {
                            return construct;
                        }
                    }
                }
            } else if (constructType == Type.get("text")) {
                // e.g.,
                // [ ] 'foo'
                // [ ] text('foo')
                // [ ] text(id:234)

                // Look for existing (persistent) state for the given expression
                List<Identifier> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Construct.class) {
                        Construct construct = (Construct) identiferList.get(i);
                        String textContent = "";
                        if (expression.startsWith("'") && expression.endsWith("'")) {
                            textContent = expression.substring(1, expression.length() - 1);
                        }
                        if (construct.type == Type.get("text") && construct.objectType == String.class && textContent.equals(construct.object)) {
//                        if (construct.classType == Type.get("text") && construct.objectType == String.class && ((textContent == null && construct.object == null) || textContent.equals(construct.object))) {
                            return construct;
                        }
                    }
                }
            } else if (constructType == Type.get("list")) {

                // TODO: Same existence-checking procedure as for construct? (i.e., look up "list(id:34)")
                // TODO: Also support looking up by construct permutation contained in list?

                // Look for existing (persistent) state for the given expression
                List<Identifier> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Construct.class) {
                        Construct construct = (Construct) identiferList.get(i);
                        if (construct.type == Type.get("list") && construct.objectType == List.class && construct.object != null) {
                            // TODO: Look for permutation of a list (matching list of constructs)?
                            return construct;
                        }
                    }
                }

            } else {

                if (Expression.isConstruct(expression)) {

                    String typeIdentifierToken = expression.substring(0, expression.indexOf("(")).trim(); // text before '('
                    String addressTypeToken = expression.substring(expression.indexOf("(") + 1, expression.indexOf(":")).trim(); // text between '(' and ':'
                    String addressToken = expression.substring(expression.indexOf(":") + 1, expression.indexOf(")")).trim(); // text between ':' and ')'

                    long uid = Long.parseLong(addressToken.trim());

                    Identifier identifier = Manager.get(uid);
//                    if (identifier != null) {
//                        if (identifier.getClass() == Construct.class) {
//                            State state = State.getState(stateType);
//                            state.object = identifier;
//                            return state;
//                        }
//                    }

                    if (identifier != null) {
                        return (Construct) identifier;
                    }


//                    // Look for existing (persistent) state for the given expression
//                    if (identifier != null) {
//                        List<Identifier> identiferList = Manager.get();
//                        for (int i = 0; i < identiferList.size(); i++) {
//                            if (identiferList.get(i).getClass() == Construct.class) {
//                                Construct construct = (Construct) identiferList.get(i);
////                            String textContent = expression.substring(1, expression.length() - 1);
//                                // TODO: Also check Type?
//                                if (construct.objectType == Map.class && construct.object != null) {
////                                        && construct.object == identifier) {
////                                        && construct.object == identifier) {
//                                    for (Construct featureConstruct : construct.states.values()) {
//                                        if (features.containsValue(identifier)) { // TODO: iterate through features to see if contains feature...
//                                            return construct;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }

                }
            }
        }

        return null;
    }

    public static List<Construct> getConstructList(Type type) {
        List<Construct> constructList = new ArrayList<>();
        for (Identifier identifier : elements.values()) {
            if (identifier.getClass() == Construct.class) {
                if (((Construct) identifier).type == type) {
                    constructList.add((Construct) identifier);
                }
            }
        }
        return constructList;
    }

//    public static Concept get(String constructUri) {
    public static Identifier get(String constructUri) {

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

                if (Manager.elements.containsKey(inputTaskUid)) {
                    return Manager.elements.get(inputTaskUid);
                }

            } else if (identifierType.equals("uuid")) {

                UUID inputTaskUuid = UUID.fromString(identifier);

                for (int i = 0; i < Manager.elements.size(); i++) {
                    if (Manager.elements.get(i).uuid.equals(inputTaskUuid)) {
                        return Manager.elements.get(i);
                    }
                }

            } else {

                // TODO: Lookup by index.

            }

        } else {

//            String identifier = constructUri.substring(1, constructUri.length() - 1);
            String title = String.valueOf(constructUri);

            List<Identifier> identifiers = new ArrayList<>(elements.values());

//            for (long uid : elements.keySet()) {
//                Concept OLD_construct = elements.clone(uid);
//                if (OLD_construct.identifier != null && OLD_construct.identifier.equals(identifier)) {
//                    return OLD_construct;
//                }
//            }

            for (int i = 0; i < identifiers.size(); i++) {
                Identifier identifier = identifiers.get(i);
                if (identifier.tag != null && identifier.tag.equals(title)) {
                    return identifier;
                }
            }

        }

        return null;

    }

    public static Identifier remove(long uid) {

        if (elements.containsKey(uid)) {
            return elements.remove(uid);
        }

        return null;

    }

}
