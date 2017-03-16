package camp.computer.construct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import camp.computer.util.console.Color;
import camp.computer.workspace.Manager;

public class Construct extends Identifier {

    // In Redis, primitive types has types and content; non-primitive has no content.
    // TODO: Use "features" object as a HashMap for non-primitive to reference features;
    // TODO:      ArrayList for primitive "list" types;
    // TODO:      String for primitive "text" types;
    // TODO:      Double for primitive "number" types;
    // TODO:      null for primitive "none" types

    // <CONCEPT>
    public Type type = null;

    public Concept concept = null; // The {@code Construct} used to create this Construct.

//    public HashMap<String, Feature> features = new HashMap<>(); // TODO: Remove? Remove setupConfiguration?
    // TODO: (Replace ^ with this, based on TODO block above:) Bytes storing actual object and object types

    // null for "none"
    // List for "list" (allocates ArrayList<Object>)
    // String for "text"
    // Double for "number"
    // [DELETE] Construct for non-primitive types
    // Map for non-primitive construct (allocates HashMap or TreeMap)
    public Class objectType = null;
    public Object object = null;
    // </CONCEPT>

    // This is only present for non-primitive types (that instantiate a Map)
    // TODO: Remove this after removing the State class.
    public HashMap<String, Construct> states = new HashMap<>(); // TODO: Remove? Remove setupConfiguration?

    // TODO: configuration(s) : assign state to multiple features <-- do this for _Container_ not Concept


    // TODO:
    // 1. Use types, features, and states for non-console (structure) constructs (custom non-primitive constructs)
    // 2. For console states (i.e., to replace State), don't use features or states hashes. Store actual data in objectType and object (from State).

    private Construct(Concept concept) {

        this.type = concept.type;
        this.concept = concept;

        // Allocate default object based on specified classType
        if (type == Type.get("none")) {
            this.objectType = null;
            this.object = null;
        } else if (type == Type.get("number")) {
            this.objectType = Double.class;
            this.object = 0; // TODO: Default to null?
        } else if (type == Type.get("text")) {
            this.objectType = String.class;
            this.object = ""; // TODO: Default to null?
        } else if (type == Type.get("list")) {
            this.objectType = List.class;
            this.object = new ArrayList<>();
        } else if (type != null) {
            this.objectType = Map.class;
            this.object = new HashMap<String, Feature>();

            // Create Content for each Feature
            HashMap<String, Feature> features = (HashMap<String, Feature>) this.object;
            for (Feature feature : concept.features.values()) {
                features.put(feature.identifier, feature);
                states.put(feature.identifier, Construct.create(Type.get("none"))); // Initialize with only available types if there's only one available
//                if (feature.types != null) { // if (feature.types.size() == 1) {
//                    // Get default feature construct state
//                    // states.put(feature.identifier, Construct.get(feature.types.get(0).identifier)); // Initialize with only available types if there's only one available
//                    states.put(feature.identifier, Construct.create(Type.get("none"))); // Initialize with only available types if there's only one available
//                } else {
//                    states.put(feature.identifier, null); // Default to "any" types by setting null
//                }
            }
        }
    }

//    /**
//     * Returns {@code true} if {@code construct} is configured to store one or more
//     * <em>primitive</em> constructs. This configuration is determined to be present if the
//     * {@code construct} references a {@code Map}.
//     * @param construct
//     * @return
//     */
//    public static boolean isComposite(Construct construct) {
////        if (!Construct.isPrimitive(construct) && construct.objectType == Map.class && construct.object != null) {
//        if (construct.objectType == Map.class && construct.object != null) {
//            return true;
//        }
//        return false;
//    }

    public static Construct create(Type type) {
        Concept concept = Concept.request(type);
        if (concept != null) {
            Construct construct = Manager.getPersistentConstruct(type);
            if (construct == null) {
                // TODO: Check if default construct for classType already exists!
                construct = new Construct(concept);
                long uid = Manager.add(construct);
                return construct;
            }
            return construct;
        }
        return null;
    }

    public static Construct create(Concept concept) {
        if (concept != null) {
//            Construct construct = Manager.getPersistentConstruct(type);
            Construct construct = Manager.getPersistentConstruct(concept);
            if (construct == null) {
                // TODO: Check if default construct for classType already exists!
                construct = new Construct(concept);
                long uid = Manager.add(construct);
                return construct;
            }
            return construct;
        }
        return null;
    }

//    // TODO: public static Construct create(Double number)
//    public static Construct create(String text) {
//        Construct construct = null;
//       // TODO:
//        return construct;
//    }

    //    public static Construct REFACTOR_getList(Construct currentConstruct, String featureToReplace, Construct featureConstructReplacement) {
    public static Construct create(List list) { // previously, REFACTOR_getList(...)

        Concept concept = Concept.request(Type.get("list"));
        Construct newListConstruct = new Construct(concept);

        // Copy elements into construct list.
        List constructList = (List) newListConstruct.object;
        for (int i = 0; i < list.size(); i++) {
            constructList.add(list.get(i));
        }

        long uid = Manager.add(newListConstruct);
        return newListConstruct;

    }

    /**
     * Creates a {@code Construct} by specified feature change. Creates {@code Construct} if it
     * doesn't exist in the persistent store.
     *
     * @param baseConstruct The reference {@code Construct} for the feature replacement.
     * @param targetFeature The feature to replace in {@code baseConstruct} with {@code replacementConstruct}.
     * @param replacementConstruct The {@code Construct} to assign to the feature identified by {@code targetFeature}.
     * @return
     */
    public static Construct create(Construct baseConstruct, String targetFeature, Construct replacementConstruct) {

//        Concept concept = Concept.request(baseConstruct.type);
//        Concept concept = Concept.request(baseConstruct.concept);
//        Construct newContruct = new Construct(concept);
        Construct newContruct = new Construct(baseConstruct.concept);

        // Copy states from source Construct.
        for (String featureIdentifier : baseConstruct.states.keySet()) {
            if (featureIdentifier.equals(targetFeature)) {
                newContruct.states.put(targetFeature, replacementConstruct);
            } else {
                newContruct.states.put(featureIdentifier, baseConstruct.states.get(featureIdentifier));
            }
        }

        long uid = Manager.add(newContruct);
        return newContruct;

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
    public static Construct request(String expression) { // previously, getPersistentConstruct
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
                // State wasn't found, so create a new one and return it
                return Construct.create(constructType);
                /*
                if (construct == null) {
                    // TODO: Store in the database
                    construct = Construct.create(constructType);
                }
                return construct;
                */
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
                // State wasn't found, so create a new one and return it
                // TODO: Store in the database
                Construct construct = null;
                if (expression.startsWith("'") && expression.endsWith("'")) {
                    Concept conceptType = Concept.request(constructType);
                    construct = new Construct(conceptType);
                    long uid = Manager.add(construct);
                    construct.object = expression.substring(1, expression.length() - 1);
                } else {
                    construct = Construct.create(constructType);
                    construct.object = "";
                }
                return construct;
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

//                    String typeIdentifierToken = expression.substring(0, expression.indexOf("(")).trim(); // text before '('
//                    String addressTypeToken = expression.substring(expression.indexOf("(") + 1, expression.indexOf(":")).trim(); // text between '(' and ':'
//                    String addressToken = expression.substring(expression.indexOf(":") + 1, expression.indexOf(")")).trim(); // text between ':' and ')'
                    String[] tokens = expression.split("\\.");
                    String typeIdentifierToken = tokens[0];
                    String addressTypeToken = tokens[1];
                    String addressToken = tokens[2];

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

                // Create new construct since a persistent one wasn't found for the expression
                Construct construct = null;
                if (construct == null) {

                    // Create new State
                    // TODO: Add new state to persistent store

                    Concept conceptType = Concept.request(constructType);
                    construct = new Construct(conceptType);
                    long uid = Manager.add(construct);
//                    construct.object = expression.substring(1, expression.length() - 1);

//                    String typeIdentifierToken = expression.substring(0, expression.indexOf("(")).trim(); // text before '('
//                    String addressTypeToken = expression.substring(expression.indexOf("(") + 1, expression.indexOf(":")).trim(); // text between '(' and ':'
//                    String addressToken = expression.substring(expression.indexOf(":") + 1, expression.indexOf(")")).trim(); // text between ':' and ')'
//
//                    long uid = Long.parseLong(addressToken.trim());
//                    Identifier identifier = Manager.get(uid);
//                    if (identifier != null) {
//                        construct = Construct.get(constructType);
//                        construct.object = identifier;
//                        return construct;
//                    } else {
//                        System.out.println(Error.get("Error: " + expression + " does not exist."));
//                    }
                }
                return construct;
            }
        }

        return null;
    }

    /**
     * Request the <em>list</em> {@code Construct} that contains the same sequence of
     * {@code Construct}s as specified in {@code list}.
     *
     * @param list
     * @return
     */
    public static Construct request(List list) { // previously, getPersistentListConstruct

        Type type = Type.get("list");

        // Look for persistent "empty list" object (i.e., the default list).
        List<Identifier> identiferList = Manager.get();
        for (int i = 0; i < identiferList.size(); i++) {
            if (identiferList.get(i).getClass() == Construct.class) {
                Construct candidateConstruct = (Construct) identiferList.get(i);

                if (candidateConstruct.type == type && candidateConstruct.objectType == List.class && candidateConstruct.object != null) {
                    // LIST


                    // Check (1) if constructs are based on the same specified concept version, and
                    //       (2) same list of constructs.
                    List candidateConstructList = (List) candidateConstruct.object;
//                    List currentConstructList = (List) currentConstruct.object;
                    List currentConstructList = list;

                    // Compare identifer, types, domain, listTypes
                    // TODO: Move comparison into Concept.hasConstruct(concept, construct);
                    boolean isConstructMatch = true;
                    if (candidateConstructList.size() != currentConstructList.size()) {
                        isConstructMatch = false;
                    } else {

                        // Compare candidate list (from repository) with the requested list.
                        for (int j = 0; j < currentConstructList.size(); j++) {
                            if (!candidateConstructList.contains(currentConstructList.get(j))) {
                                isConstructMatch = false;
                            }
                        }

//                        // Compare candidate construct (from repository) with the current construct being updated.
//                        for (String featureIdentifier : currentConstructFeatures.keySet()) {
//                            if (featureIdentifier.equals(featureToReplace)) {
//                                if (!candidateConstructFeatures.containsKey(featureIdentifier)
//                                        || !candidateConstruct.states.containsKey(featureIdentifier)
//                                        || candidateConstruct.states.get(featureIdentifier) != featureConstructReplacement) {
////                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
//                                    isConstructMatch = false;
//                                }
//                            } else {
//                                if (!candidateConstructFeatures.containsKey(featureIdentifier)
//                                        || !candidateConstruct.states.containsKey(featureIdentifier)
//                                        || candidateConstruct.states.get(featureIdentifier) != currentConstruct.states.get(featureIdentifier)) {
////                                        || !candidateConstructFeatures.containsValue(concept.features.get(featureIdentifier))) {
//                                    isConstructMatch = false;
//                                }
//                            }
//                        }
//
//                        // TODO: Additional checks...

                    }

                    if (isConstructMatch) {
                        return candidateConstruct;
                    }


                    // TODO: Look for permutation of a list (matching list of constructs)?
//                    return construct;

                }
            }
        }

        // Create new Construct if got to this point because an existing one was not found
//        Construct newReplacementConstruct = Construct.create(currentConstruct, featureToReplace, featureConstructReplacement);
        Construct newReplacementConstruct = Construct.create(list);
        if (newReplacementConstruct != null) {
            return newReplacementConstruct;
        }

        // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Concept used to create it).
        return null;

    }


    /**
     * Requests a {@code Construct} by feature change. Creates {@code Construct} if it doesn't
     * exist in the persistent store.
     *
     * Returns the persistent {@code Construct}, if exists, that would result from applying
     * {@code expression} to the specified {@code construct}.
     *
     * If no such {@code Construct} exists, returns {@code null}.
     */
//    public static Construct getPersistentConstruct(Construct construct, String expression) {
//    public static Construct getPersistentConstruct(Construct construct, Feature feature, Construct featureConstructReplacement) {
    public static Construct request(Construct currentConstruct, String featureToReplace, Construct featureConstructReplacement) {

        Type type = currentConstruct.type; // Construct type

        // Look for persistent "empty list" object (i.e., the default list).
        List<Identifier> identiferList = Manager.get();
        for (int i = 0; i < identiferList.size(); i++) {
            if (identiferList.get(i).getClass() == Construct.class) {
                Construct candidateConstruct = (Construct) identiferList.get(i);

                if (candidateConstruct.type == type && candidateConstruct.objectType == List.class && candidateConstruct.object != null) {
                    // LIST


                    // Check (1) if constructs are based on the same specified concept version, and
                    //       (2) same list of constructs.
                    List candidateConstructList = (List) candidateConstruct.object;
                    List currentConstructList = (List) currentConstruct.object;

                } else if (candidateConstruct.type == type && candidateConstruct.objectType == Map.class && candidateConstruct.object != null) {
//                } else if (Construct.isComposite(construct)) {
                    // HASHMAP

                    // Check (1) if constructs are based on the same specified concept version, and
                    //       (2) same set of features and assignments to constructs except the specified feature to change.
                    HashMap<String, Feature> candidateConstructFeatures = (HashMap<String, Feature>) candidateConstruct.object;
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
                                        || !candidateConstruct.states.containsKey(featureIdentifier)
                                        || candidateConstruct.states.get(featureIdentifier) != featureConstructReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
                                    isConstructMatch = false;
                                }
                            } else {
                                if (!candidateConstructFeatures.containsKey(featureIdentifier)
                                        || !candidateConstruct.states.containsKey(featureIdentifier)
                                        || candidateConstruct.states.get(featureIdentifier) != currentConstruct.states.get(featureIdentifier)) {
//                                        || !candidateConstructFeatures.containsValue(concept.features.get(featureIdentifier))) {
                                    isConstructMatch = false;
                                }
                            }
                        }

                        // TODO: Additional checks...

                    }

                    if (isConstructMatch) {
                        return candidateConstruct;
                    }


                    // TODO: Look for permutation of a list (matching list of constructs)?
//                    return construct;
                }
            }
        }

        // Create new Construct if got to this point because an existing one was not found
        Construct newReplacementConstruct = Construct.create(currentConstruct, featureToReplace, featureConstructReplacement);
        if (newReplacementConstruct != null) {
            return newReplacementConstruct;
        }

        // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Concept used to create it).
        return null;

    }

    public static Feature getFeature(Construct construct, String featureIdentifier) {
        HashMap<String, Feature> features = (HashMap<String, Feature>) construct.object;
        if (features.containsKey(featureIdentifier)) {
            return features.get(featureIdentifier);
        }
        return null;
    }

    // If listType is "any", allow anything to go in the list
    // if listType is "text", only allow text to be placed in the list
    // if listType is specific "text" values, only allow those values in the list
//    public void set(String featureIdentifier, String expression) {
//
//        // TODO: Check if classType can use "set"
//
//        HashMap<String, Feature> features = (HashMap<String, Feature>) this.object;
//
//        if (features.containsKey(featureIdentifier)) {
//
//            Type constructType = Type.get(expression);
//            Feature feature = features.get(featureIdentifier);
////            if (feature.types == null || feature.types.contains(constructType)) {
//            if (feature.types.size() == 0 || feature.types.contains(constructType)) {
//
//                /*
//                // Get feature's current state
//                State state = states.get(featureIdentifier);
//
//                if (stateType == Type.get("none")) {
//
//                    // Remove the types of the stored object
//                    if (state == null) {
//                        state = State.getState(stateType);
//                    } else if (state.types != stateType) {
////                        featureContent.objectType = null;
////                        featureContent.object = null;
//                        state = State.getState(stateType);
//                    }
//
//                } else if (stateType == Type.get("list")) {
//
//                    // Change the types of the stored object if it is not a list
//                    if (state == null) {
//                        state = State.getState(stateType);
//                    } else if (state.types != stateType) {
////                        featureContent.objectType = List.class;
////                        featureContent.object = new ArrayList<>();
//                        state = State.getState(stateType);
//                    }
//
//                    // Update the object
//
//                } else if (stateType == Type.get("text")) {
//
//                    // Change the types of the stored object if it is not a string (for text)
////                    if (state == null) {
////                        state = State.getPersistentState(stateType);
////                    } else if (state.types != stateType) {
//////                        featureContent.objectType = String.class;
//////                        featureContent.object = null;
////                        state = State.getPersistentState(stateType);
////                    }
//
////                    if (Content.isText((String) object)) {
//                   state = State.getState((String) expression);
////                    if (feature.domain == null || feature.domain.contains(stateExpression)) {
//                    if (feature.domain == null || feature.domain.contains(state)) { // TODO: Make sure 'contains' works!
////                        state.object = (String) stateExpression;
//                        states.put(featureIdentifier, state);
//                    } else {
//                        System.out.println(Application.ANSI_RED + "Error: Specified text is not in the feature's domain." + Application.ANSI_RESET);
//                    }
////                    } else {
////                        System.out.println("Error: Cannot assign non-text to text feature.");
////                    }
//
//                } else {
//
//
//                }
//                */
//
//                if (constructType == Type.get("none")) {
//
//                    Construct construct = Construct.get(expression);
//
//                    if (feature.domain == null || feature.domain.contains(construct)) { // TODO: Make sure 'contains' works!
//                        states.put(featureIdentifier, construct);
//                        // TODO: Update Construct in database
//                    } else {
//                        System.out.println(Color.ANSI_RED + "Error: Specified text is not in the feature's domain." + Color.ANSI_RESET);
//                    }
//
////                } else if (stateType == Type.get("text")) {
////
////                    State state = State.getState(expression);
////
////                    if (state != null) {
////                        if (feature.domain == null || feature.domain.contains(state)) { // TODO: Make sure 'contains' works!
////                            states.put(featureIdentifier, state);
////                            // TODO: Update Construct in database
////                        } else {
////                            System.out.println(Application.ANSI_RED + "Error: Specified text is not in the feature's domain." + Application.ANSI_RESET);
////                        }
////                    } else {
////                        System.out.println(Application.ANSI_RED + "Error: Interpreter error. State is null." + Application.ANSI_RESET);
////                    }
//
//                } else if (constructType == Type.get("list")) {
//
//                    // TODO: Allow lists to be assigned? Yes!
//                    System.out.println(Color.ANSI_RED + "Error: Cannot SET on a list. (This might change!)." + Color.ANSI_RESET);
//
//                } else {
//
////                    State state = State.getState(expression);
////
////                    // Add to the list in memory
////                    // TODO: if (state != null && state != states.get(featureIdentifier)) {
////                    if (state != null) {
////                        if (feature.domain == null || feature.domain.contains(state)) { // TODO: Update domain to contain State objects so it can contain port and other Constructs
////                            State featureState = states.get(featureIdentifier);
////                            Construct featureConstruct = (Construct) featureState.object;
//////                        contents.get(tag).state.object = (String) object;
////                            featureConstruct.get(state);
////                        } else {
////                            System.out.println(Application.ANSI_RED + "Error: Specified " + stateType + " is not in the feature's domain." + Application.ANSI_RESET);
////                        }
////                    }
//
//                    Construct construct = Construct.get(expression);
//
//                    if (construct != null) {
//                        if (feature.domain == null || feature.domain.contains(construct)) { // TODO: Make sure 'contains' works!
//                            states.put(featureIdentifier, construct);
//                            // TODO: Update Construct in database
//                        } else {
//                            System.out.println(Color.ANSI_RED + "Error: Specified text is not in the feature's domain." + Color.ANSI_RESET);
//                        }
//                    } else {
//                        System.out.println(Color.ANSI_RED + "Error: Interpreter error. State is null." + Color.ANSI_RESET);
//                    }
//
////                    System.out.println(Application.ANSI_RED + "Error: Feature types mismatches object types." + Application.ANSI_RESET);
//
//                }
//            }
//
//
//            /*
//            if (contents.get(tag).types == Type.get("none")) {
//                // TODO: Can't assign anything to the feature object
//                System.out.println("Error: Cannot assign feature with types 'none'.");
//            } else if (contents.get(tag).types == Type.get("any")) {
//                // TODO: Verify that this is correct!
//                contents.get(tag).object = object;
//            } else if (contents.get(tag).types == Type.get("list")) {
//                List contentList = (List) contents.get(tag).object;
//
//                // TODO: Check types of list contents and restrict to the types (or any if "any")
//                // TODO: If specific text tokens are allowed AS WELL AS text construct, text construct subsumes the tokens and the tokens are not included in the domain
//
////                if (contents.get(identifier).listType == Type.get("text")) {
//                if (contents.get(tag).listTypes.contains(Type.get("text"))) {
//                    if (Content.isText((String) object)) {
//                        contentList.get(object);
//                    } else {
//                        System.out.println("Error: Cannot get non-text to list (only can contain text).");
//                    }
////                } else if (contents.get(identifier).listType == Type.get("construct")) {
////                } else if (contents.get(identifier).listTypes.contains(Type.get("construct"))) {
//                } else {
//                    // TODO: Determine if the construct object is allowed into the list based on the specific types!
//                    contentList.get(object);
//                }
//            } else if (contents.get(tag).types == Type.get("text")) {
//                if (Content.isText((String) object)) {
//                    contents.get(tag).object = (String) object;
//                } else {
//                    System.out.println("Error: Cannot assign non-text to text feature.");
//                }
//            } else {
//                contents.get(tag).object = object;
//            }
//            */
//        }
//    }

//    public Feature get(String tag) {
//        if (features.containsKey(tag)) {
//            return features.get(tag);
//        }
//        return null;
//    }

    // TODO: get <list-feature-identifier> : <object>

//    /**
//     * Adds a {@code State} to a <em>list</em> {@code Construct}, which is a {@code Construct} with
//     * a {@code Type} uniquely identified by its {@code identifier} equal to {@code "list"}.
//     *
//     * {@code expression} is a <em>state expression</em>.
//     *
//     * @param featureIdentifier
//     * @param expression
//     */
//    public void insert(String featureIdentifier, String expression) {
//        if (features.containsKey(featureIdentifier)) {
//            Feature feature = features.get(featureIdentifier);
//            Construct featureState = states.get(featureIdentifier);
//
//            // Check if feature can be a list
//            if (!feature.types.contains(Type.get("list"))) {
//                System.out.println(Color.ANSI_RED + "Error: Cannot add to a non-list." + Color.ANSI_RESET);
//                return;
//            }
//
//            // Check if feature is currently configured as a list
//            if (featureState.type != Type.get("list")) {
//                // Change the types of the stored object if it is not a list
//                if (featureState == null) {
//                    featureState = Construct.create(Type.get("list"));
//                } else if (featureState.type != Type.get("list")) {
//                    featureState = Construct.create(Type.get("list"));
//                }
//            }
//
//            // Add the object to the list
//            Type stateType = Type.get((String) expression);
//            if (stateType != null
//                    && (feature.listTypes == null || feature.listTypes.contains(stateType))) {
//
//                if (stateType == Type.get("none")) {
//
////                    // Remove the types of the stored object
////                    if (featureContent.state == null) {
////                        featureContent.state = new State(objectType);
////                    } else if (featureContent.state.types != objectType) {
//////                        featureContent.objectType = null;
//////                        featureContent.object = null;
////                        featureContent.state = new State(objectType);
////                    }
//
//                } else if (stateType == Type.get("list")) {
//
//                    // Change the types of the stored object if it is not a list
//                    if (featureState == null) {
//                        featureState = Construct.create(stateType);
//                    } else if (featureState.type != stateType) {
////                        featureContent.objectType = List.class;
////                        featureContent.object = new ArrayList<>();
//                        featureState = Construct.create(stateType);
//                    }
//
//                    // Update the object
//
//                } else if (stateType == Type.get("text")) {
//
//                    // Change the types of the stored object if it is not a string (for text)
////                    if (featureContent.state == null) {
////                        featureContent.state = new State(objectType);
////                    } else if (featureContent.state.types != objectType) {
//////                        featureContent.objectType = String.class;
//////                        featureContent.object = null;
////                        featureContent.state = new State(objectType);
////                    }
//
//
////                    // Encapsulate text state
////                    State state = State.getState(stateType);
////                    state.object = expression;
//
//                    // Encapsulate text state
//                    Construct construct = Construct.get(expression);
//
//
////                    if (Content.isText((String) object)) {
////                    if (feature.domain == null || feature.domain.contains((String) expression)) {
//                    if (feature.domain == null || feature.domain.contains(construct)) {
//                    // TODO: if (feature.domain == null || feature.domain.contains(state)) {
//                            List list = (List) featureState.object;
////                        contents.get(tag).state.object = (String) object;
////                            list.get(expression);
//                            list.add(construct);
//                        } else {
//                            System.out.println(Color.ANSI_RED + "Error: Specified " + stateType + " is not in the feature's domain." + Color.ANSI_RESET);
//                        }
////                    }
////                    } else {
////                        System.out.println("Error: Cannot assign non-text to text feature.");
////                    }
//
//                } else {
//
////                    // Change the types of the stored object if it is not a list
////                    if (state == null) {
////                        state = State.getPersistentState(contentType);
////                    } else if (state.types != contentType) {
//////                        featureContent.objectType = List.class;
//////                        featureContent.object = new ArrayList<>();
////                        state = State.getPersistentState(contentType);
////                    }
//
//                    // Encapsulate text state
//                    Construct construct = Construct.get(expression);
//
//                    // Add to the list in memory
////                    if (Content.isText((String) object)) {
//                    if (construct != null) {
////                        if (feature.domain == null || feature.domain.contains((String) expression)) { // TODO: Update domain to contain State objects so it can contain port and other Constructs
//                        if (feature.domain == null || feature.domain.contains(construct)) { // TODO: Update domain to contain State objects so it can contain port and other Constructs
//                            List list = (List) featureState.object;
////                        contents.get(tag).state.object = (String) object;
//                            list.add(construct);
//                        } else {
//                            System.out.println(Color.ANSI_RED + "Error: Specified " + stateType + " is not in the feature's domain." + Color.ANSI_RESET);
//                        }
//                    }
//
//                }
//            } else {
//                System.out.println(Color.ANSI_RED + "Error: Feature types mismatches object types." + Color.ANSI_RESET);
//            }
//
//
//            /*
//            if (contents.get(tag).types == Type.get("none")) {
//                // TODO: Can't assign anything to the feature object
//                System.out.println("Error: Cannot assign feature with types 'none'.");
//            } else if (contents.get(tag).types == Type.get("any")) {
//                // TODO: Verify that this is correct!
//                contents.get(tag).object = object;
//            } else if (contents.get(tag).types == Type.get("list")) {
//                List contentList = (List) contents.get(tag).object;
//
//                // TODO: Check types of list contents and restrict to the types (or any if "any")
//                // TODO: If specific text tokens are allowed AS WELL AS text construct, text construct subsumes the tokens and the tokens are not included in the domain
//
////                if (contents.get(identifier).listType == Type.get("text")) {
//                if (contents.get(tag).listTypes.contains(Type.get("text"))) {
//                    if (Content.isText((String) object)) {
//                        contentList.get(object);
//                    } else {
//                        System.out.println("Error: Cannot get non-text to list (only can contain text).");
//                    }
////                } else if (contents.get(identifier).listType == Type.get("construct")) {
////                } else if (contents.get(identifier).listTypes.contains(Type.get("construct"))) {
//                } else {
//                    // TODO: Determine if the construct object is allowed into the list based on the specific types!
//                    contentList.get(object);
//                }
//            } else if (contents.get(tag).types == Type.get("text")) {
//                if (Content.isText((String) object)) {
//                    contents.get(tag).object = (String) object;
//                } else {
//                    System.out.println("Error: Cannot assign non-text to text feature.");
//                }
//            } else {
//                contents.get(tag).object = object;
//            }
//            */
//        }
//    }

    @Override
    public String toString() {
        if (type == Type.get("text")) {
            String content = (String) this.object;
            return "'" + content + "' " + type + ".id." + uid + "";
        } else if (type == Type.get("list")) {
            String content = "";
            List list = (List) this.object;
            for (int i = 0; i < list.size(); i++) {
                content += list.get(i);
                if ((i + 1) < list.size()) {
                    content += ", ";
                }
            }
            return type + ".id." + uid + " : " + content;
        } else {
            return type + ".id." + uid;
        }
    }

    public String toColorString() {
        if (type == Type.get("text")) {
            String content = (String) this.object;
            // return Color.ANSI_BLUE + type + Color.ANSI_RESET + " '" + content + "' (id: " + uid + ")" + " (uuid: " + uuid + ")";
            return  "'" + content + "' " + Color.ANSI_BLUE + type + Color.ANSI_RESET + ".id." + uid;
        } else {
            return Color.ANSI_BLUE + type + Color.ANSI_RESET + ".id." + uid;
            // return Color.ANSI_BLUE + type + Color.ANSI_RESET + " (id: " + uid + ")" + " (uuid: " + uuid + ")";
        }
    }
}
