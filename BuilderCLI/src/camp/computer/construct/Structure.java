package camp.computer.construct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import camp.computer.util.console.Color;
import camp.computer.workspace.Manager;

public class Structure extends Identifier {

    // In Redis, primitive types has types and content; non-primitive has no content.
    // TODO: Use "features" object as a HashMap for non-primitive to reference features;
    // TODO:      ArrayList for primitive "list" types;
    // TODO:      String for primitive "text" types;
    // TODO:      Double for primitive "number" types;
    // TODO:      null for primitive "none" types

    // <CONCEPT>
//    public TypeId type = null; // TODO: Remove!
    public Type type2 = null; // TODO: Remove!
    public Type type = null; // The {@code Structure} used to create this Structure.

//    public HashMap<String, Feature> features = new HashMap<>(); // TODO: Remove? Remove setupConfiguration?
    // TODO: (Replace ^ with this, based on TODO block above:) Bytes storing actual object and object types

    // null for "none"
    // List for "list" (allocates ArrayList<Object>)
    // String for "text"
    // Double for "number"
    // [DELETE] Structure for non-primitive types
    // Map for non-primitive construct (allocates HashMap or TreeMap)
    public Class objectType = null;
    public Object object = null;
    // </CONCEPT>

    // This is only present for non-primitive types (that instantiate a Map)
    // TODO: Remove this after removing the State class.
    public HashMap<String, Structure> states = new HashMap<>(); // TODO: Remove? Remove setupConfiguration?

    // TODO: configuration(s) : assign state to multiple features <-- do this for _Container_ not Type


    // TODO:
    // 1. Use types, features, and states for non-console (structure) constructs (custom non-primitive constructs)
    // 2. For console states (i.e., to replace State), don't use features or states hashes. Store actual data in objectType and object (from State).

    private Structure(Type type) {

        this.type2 = Type.request(type.identifier);

        this.type = type;

        // Allocate default object based on specified classType
        if (this.type.identifier.equals("none")) {
            this.objectType = null;
            this.object = null;
        } else if (this.type.identifier.equals("number")) {
            this.objectType = Double.class;
            this.object = 0; // TODO: Default to null?
        } else if (this.type.identifier.equals("text")) {
            this.objectType = String.class;
            this.object = ""; // TODO: Default to null?
        } else if (this.type.identifier.equals("list")) {
            this.objectType = List.class;
            this.object = new ArrayList<>();
        } else if (this.type.identifier != null) {
            this.objectType = Map.class;
            this.object = new HashMap<String, Feature>();

            // Create Content for each Feature
            HashMap<String, Feature> features = (HashMap<String, Feature>) this.object;
            for (Feature feature : type.features.values()) {
                features.put(feature.identifier, feature);
                Type type2 = Type.request("none");
                Structure structure = Structure.create(type2);
                states.put(feature.identifier, structure); // Initialize with only available types if there's only one available
//                states.put(feature.identifier, Structure.create(Type.request("none"))); // Initialize with only available types if there's only one available
//                if (feature.types != null) { // if (feature.types.size() == 1) {
//                    // Get default feature structure state
//                    // states.put(feature.identifier, Structure.request(feature.types.request(0).identifier)); // Initialize with only available types if there's only one available
//                    states.put(feature.identifier, Structure.create(Type.request("none"))); // Initialize with only available types if there's only one available
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
//    public static boolean isComposite(Structure construct) {
////        if (!Structure.isPrimitive(construct) && construct.objectType == Map.class && construct.object != null) {
//        if (construct.objectType == Map.class && construct.object != null) {
//            return true;
//        }
//        return false;
//    }

//    public static Structure create(TypeId type) {
//        Type type = Type.request(type);
//        if (type != null) {
//            Structure construct = Manager.getPersistentConstruct(type);
//            if (construct == null) {
//                // TODO: Check if default construct for classType already exists!
//                construct = new Structure(type);
//                long uid = Manager.add(construct);
//                return construct;
//            }
//            return construct;
//        }
//        return null;
//    }

    public static Structure create(Type type) {
        if (type != null) {
//            Structure structure = Manager.getPersistentConstruct(type);
            Structure structure = Manager.getPersistentConstruct(type);
            if (structure == null) {
                // TODO: Check if default structure for classType already exists!
                structure = new Structure(type);
                long uid = Manager.add(structure);
                return structure;
            }
            return structure;
        }
        return null;
    }

//    // TODO: public static Structure create(Double number)
//    public static Structure create(String text) {
//        Structure construct = null;
//       // TODO:
//        return construct;
//    }

    //    public static Structure REFACTOR_getList(Structure currentConstruct, String featureToReplace, Structure featureConstructReplacement) {
    public static Structure create(List list) { // previously, REFACTOR_getList(...)

        Type type = Type.request("list");
        Structure newListStructure = new Structure(type);

        // Copy elements into construct list.
        List constructList = (List) newListStructure.object;
        for (int i = 0; i < list.size(); i++) {
            constructList.add(list.get(i));
        }

        long uid = Manager.add(newListStructure);
        return newListStructure;

    }

    /**
     * Creates a {@code Structure} by specified feature change. Creates {@code Structure} if it
     * doesn't exist in the persistent store.
     *
     * @param baseStructure The reference {@code Structure} for the feature replacement.
     * @param targetFeature The feature to replace in {@code baseStructure} with {@code replacementStructure}.
     * @param replacementStructure The {@code Structure} to assign to the feature identified by {@code targetFeature}.
     * @return
     */
    public static Structure create(Structure baseStructure, String targetFeature, Structure replacementStructure) {

//        Type type = Type.request(baseStructure.type);
//        Type type = Type.request(baseStructure.type);
//        Structure newContruct = new Structure(type);
        Structure newContruct = new Structure(baseStructure.type);

        // Copy states from source Structure.
        for (String featureIdentifier : baseStructure.states.keySet()) {
            if (featureIdentifier.equals(targetFeature)) {
                newContruct.states.put(targetFeature, replacementStructure);
            } else {
                newContruct.states.put(featureIdentifier, baseStructure.states.get(featureIdentifier));
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
    public static Structure request(String expression) { // previously, getPersistentConstruct
        Type constructTypeId = Type.request(expression);
        if (constructTypeId != null) {

            if (constructTypeId == Type.request("none")) {
                // Look for existing (persistent) state for the given expression
                List<Identifier> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Structure.class) {
                        Structure structure = (Structure) identiferList.get(i);
                        if (structure.type2 == Type.request("none") && structure.objectType == null && structure.object == null) {
                            return structure;
                        }
                    }
                }
                // State wasn't found, so create a new one and return it
                Type type = Type.request(constructTypeId.identifier);
                return Structure.create(type);
//                return Structure.create(constructTypeId);
                /*
                if (construct == null) {
                    // TODO: Store in the database
                    construct = Structure.create(constructTypeId);
                }
                return construct;
                */
            } else if (constructTypeId == Type.request("text")) {
                // e.g.,
                // [ ] 'foo'
                // [ ] text('foo')
                // [ ] text(id:234)

                // Look for existing (persistent) state for the given expression
                List<Identifier> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Structure.class) {
                        Structure structure = (Structure) identiferList.get(i);
                        String textContent = "";
                        if (expression.startsWith("'") && expression.endsWith("'")) {
                            textContent = expression.substring(1, expression.length() - 1);
                        }
                        if (structure.type2 == Type.request("text") && structure.objectType == String.class && textContent.equals(structure.object)) {
//                        if (structure.classType == Type.request("text") && structure.objectType == String.class && ((textContent == null && structure.object == null) || textContent.equals(structure.object))) {
                            return structure;
                        }
                    }
                }
                // State wasn't found, so create a new one and return it
                // TODO: Store in the database
                Structure structure = null;
                if (expression.startsWith("'") && expression.endsWith("'")) {
                    Type typeType = Type.request(constructTypeId.identifier);
                    structure = new Structure(typeType);
                    long uid = Manager.add(structure);
                    structure.object = expression.substring(1, expression.length() - 1);
                } else {
                    Type type = Type.request(constructTypeId.identifier);
                    structure = Structure.create(type);
                    structure.object = "";
                }
                return structure;
            } else if (constructTypeId == Type.request("list")) {

                // TODO: Same existence-checking procedure as for construct? (i.e., look up "list(id:34)")
                // TODO: Also support looking up by construct permutation contained in list?

                // Look for existing (persistent) state for the given expression
                List<Identifier> identiferList = Manager.get();
                for (int i = 0; i < identiferList.size(); i++) {
                    if (identiferList.get(i).getClass() == Structure.class) {
                        Structure structure = (Structure) identiferList.get(i);
                        if (structure.type2 == Type.request("list") && structure.objectType == List.class && structure.object != null) {
                            // TODO: Look for permutation of a list (matching list of constructs)?
                            return structure;
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
//                        if (identifier.getClass() == Structure.class) {
//                            State state = State.getState(stateType);
//                            state.object = identifier;
//                            return state;
//                        }
//                    }

                    if (identifier != null) {
                        return (Structure) identifier;
                    }


//                    // Look for existing (persistent) state for the given expression
//                    if (identifier != null) {
//                        List<Identifier> identiferList = Manager.request();
//                        for (int i = 0; i < identiferList.size(); i++) {
//                            if (identiferList.request(i).getClass() == Structure.class) {
//                                Structure structure = (Structure) identiferList.request(i);
////                            String textContent = expression.substring(1, expression.length() - 1);
//                                // TODO: Also check TypeId?
//                                if (structure.objectType == Map.class && structure.object != null) {
////                                        && structure.object == identifier) {
////                                        && structure.object == identifier) {
//                                    for (Structure featureConstruct : structure.states.values()) {
//                                        if (features.containsValue(identifier)) { // TODO: iterate through features to see if contains feature...
//                                            return structure;
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }

                }

                // Create new structure since a persistent one wasn't found for the expression
                Structure structure = null;
                if (structure == null) {

                    // Create new State
                    // TODO: Add new state to persistent store

                    Type typeType = Type.request(constructTypeId.identifier);
                    structure = new Structure(typeType);
                    long uid = Manager.add(structure);
//                    structure.object = expression.substring(1, expression.length() - 1);

//                    String typeIdentifierToken = expression.substring(0, expression.indexOf("(")).trim(); // text before '('
//                    String addressTypeToken = expression.substring(expression.indexOf("(") + 1, expression.indexOf(":")).trim(); // text between '(' and ':'
//                    String addressToken = expression.substring(expression.indexOf(":") + 1, expression.indexOf(")")).trim(); // text between ':' and ')'
//
//                    long uid = Long.parseLong(addressToken.trim());
//                    Identifier identifier = Manager.request(uid);
//                    if (identifier != null) {
//                        structure = Structure.request(constructTypeId);
//                        structure.object = identifier;
//                        return structure;
//                    } else {
//                        System.out.println(Error.request("Error: " + expression + " does not exist."));
//                    }
                }
                return structure;
            }
        }

        return null;
    }

    /**
     * Request the <em>list</em> {@code Structure} that contains the same sequence of
     * {@code Structure}s as specified in {@code list}.
     *
     * @param list
     * @return
     */
    public static Structure request(List list) { // previously, getPersistentListConstruct

//        TypeId type = Type.request("list");
        Type type = Type.request("list");

        // Look for persistent "empty list" object (i.e., the default list).
        List<Identifier> identiferList = Manager.get();
        for (int i = 0; i < identiferList.size(); i++) {
            if (identiferList.get(i).getClass() == Structure.class) {
                Structure candidateStructure = (Structure) identiferList.get(i);

                if (candidateStructure.type2 == type && candidateStructure.objectType == List.class && candidateStructure.object != null) {
                    // LIST


                    // Check (1) if constructs are based on the same specified type version, and
                    //       (2) same list of constructs.
                    List candidateConstructList = (List) candidateStructure.object;
//                    List currentConstructList = (List) currentConstruct.object;
                    List currentConstructList = list;

                    // Compare identifer, types, domain, listTypes
                    // TODO: Move comparison into Type.hasConstruct(type, construct);
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
//                                        || !candidateStructure.states.containsKey(featureIdentifier)
//                                        || candidateStructure.states.request(featureIdentifier) != featureConstructReplacement) {
////                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
//                                    isConstructMatch = false;
//                                }
//                            } else {
//                                if (!candidateConstructFeatures.containsKey(featureIdentifier)
//                                        || !candidateStructure.states.containsKey(featureIdentifier)
//                                        || candidateStructure.states.request(featureIdentifier) != currentConstruct.states.request(featureIdentifier)) {
////                                        || !candidateConstructFeatures.containsValue(type.features.request(featureIdentifier))) {
//                                    isConstructMatch = false;
//                                }
//                            }
//                        }
//
//                        // TODO: Additional checks...

                    }

                    if (isConstructMatch) {
                        return candidateStructure;
                    }


                    // TODO: Look for permutation of a list (matching list of constructs)?
//                    return construct;

                }
            }
        }

        // Create new Structure if got to this point because an existing one was not found
//        Structure newReplacementStructure = Structure.create(currentConstruct, featureToReplace, featureConstructReplacement);
        Structure newReplacementStructure = Structure.create(list);
        if (newReplacementStructure != null) {
            return newReplacementStructure;
        }

        // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Type used to create it).
        return null;

    }


    /**
     * Requests a {@code Structure} by feature change. Creates {@code Structure} if it doesn't
     * exist in the persistent store.
     *
     * Returns the persistent {@code Structure}, if exists, that would result from applying
     * {@code expression} to the specified {@code construct}.
     *
     * If no such {@code Structure} exists, returns {@code null}.
     */
//    public static Structure getPersistentConstruct(Structure construct, String expression) {
//    public static Structure getPersistentConstruct(Structure construct, Feature feature, Structure featureStructureReplacement) {
    public static Structure request(Structure currentStructure, String featureToReplace, Structure featureStructureReplacement) {

//        TypeId type = currentStructure.type; // Structure type
        Type type2 = currentStructure.type2; // Structure type

        // Look for persistent "empty list" object (i.e., the default list).
        List<Identifier> identiferList = Manager.get();
        for (int i = 0; i < identiferList.size(); i++) {
            if (identiferList.get(i).getClass() == Structure.class) {
                Structure candidateStructure = (Structure) identiferList.get(i);

                if (candidateStructure.type2 == type2 && candidateStructure.objectType == List.class && candidateStructure.object != null) {
                    // LIST


                    // Check (1) if constructs are based on the same specified type version, and
                    //       (2) same list of constructs.
                    List candidateConstructList = (List) candidateStructure.object;
                    List currentConstructList = (List) currentStructure.object;

                } else if (candidateStructure.type2 == type2 && candidateStructure.objectType == Map.class && candidateStructure.object != null) {
//                } else if (Structure.isComposite(construct)) {
                    // HASHMAP

                    // Check (1) if constructs are based on the same specified type version, and
                    //       (2) same set of features and assignments to constructs except the specified feature to change.
                    HashMap<String, Feature> candidateConstructFeatures = (HashMap<String, Feature>) candidateStructure.object;
                    HashMap<String, Feature> currentConstructFeatures = (HashMap<String, Feature>) currentStructure.object;

                    // Compare identifer, types, domain, listTypes
                    // TODO: Move comparison into Type.hasConstruct(type, construct);
                    boolean isConstructMatch = true;
                    if (candidateConstructFeatures.size() != currentConstructFeatures.size()) {
                        isConstructMatch = false;
                    } else {

                        // Compare candidate construct (from repository) with the current construct being updated.
                        for (String featureIdentifier : currentConstructFeatures.keySet()) {
                            if (featureIdentifier.equals(featureToReplace)) {
                                if (!candidateConstructFeatures.containsKey(featureIdentifier)
                                        || !candidateStructure.states.containsKey(featureIdentifier)
                                        || candidateStructure.states.get(featureIdentifier) != featureStructureReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureStructureReplacement)) {
                                    isConstructMatch = false;
                                }
                            } else {
                                if (!candidateConstructFeatures.containsKey(featureIdentifier)
                                        || !candidateStructure.states.containsKey(featureIdentifier)
                                        || candidateStructure.states.get(featureIdentifier) != currentStructure.states.get(featureIdentifier)) {
//                                        || !candidateConstructFeatures.containsValue(type.features.request(featureIdentifier))) {
                                    isConstructMatch = false;
                                }
                            }
                        }

                        // TODO: Additional checks...

                    }

                    if (isConstructMatch) {
                        return candidateStructure;
                    }


                    // TODO: Look for permutation of a list (matching list of constructs)?
//                    return construct;
                }
            }
        }

        // Create new Structure if got to this point because an existing one was not found
        Structure newReplacementStructure = Structure.create(currentStructure, featureToReplace, featureStructureReplacement);
        if (newReplacementStructure != null) {
            return newReplacementStructure;
        }

        // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Type used to create it).
        return null;

    }

    public static Feature getFeature(Structure structure, String featureIdentifier) {
        HashMap<String, Feature> features = (HashMap<String, Feature>) structure.object;
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
//            TypeId constructType = Type.request(expression);
//            Feature feature = features.request(featureIdentifier);
////            if (feature.types == null || feature.types.contains(constructType)) {
//            if (feature.types.size() == 0 || feature.types.contains(constructType)) {
//
//                /*
//                // Get feature's current state
//                State state = states.request(featureIdentifier);
//
//                if (stateType == Type.request("none")) {
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
//                } else if (stateType == Type.request("list")) {
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
//                } else if (stateType == Type.request("text")) {
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
//                if (constructType == Type.request("none")) {
//
//                    Structure construct = Structure.request(expression);
//
//                    if (feature.domain == null || feature.domain.contains(construct)) { // TODO: Make sure 'contains' works!
//                        states.put(featureIdentifier, construct);
//                        // TODO: Update Structure in database
//                    } else {
//                        System.out.println(Color.ANSI_RED + "Error: Specified text is not in the feature's domain." + Color.ANSI_RESET);
//                    }
//
////                } else if (stateType == Type.request("text")) {
////
////                    State state = State.getState(expression);
////
////                    if (state != null) {
////                        if (feature.domain == null || feature.domain.contains(state)) { // TODO: Make sure 'contains' works!
////                            states.put(featureIdentifier, state);
////                            // TODO: Update Structure in database
////                        } else {
////                            System.out.println(Application.ANSI_RED + "Error: Specified text is not in the feature's domain." + Application.ANSI_RESET);
////                        }
////                    } else {
////                        System.out.println(Application.ANSI_RED + "Error: Interpreter error. State is null." + Application.ANSI_RESET);
////                    }
//
//                } else if (constructType == Type.request("list")) {
//
//                    // TODO: Allow lists to be assigned? Yes!
//                    System.out.println(Color.ANSI_RED + "Error: Cannot SET on a list. (This might change!)." + Color.ANSI_RESET);
//
//                } else {
//
////                    State state = State.getState(expression);
////
////                    // Add to the list in memory
////                    // TODO: if (state != null && state != states.request(featureIdentifier)) {
////                    if (state != null) {
////                        if (feature.domain == null || feature.domain.contains(state)) { // TODO: Update domain to contain State objects so it can contain port and other Constructs
////                            State featureState = states.request(featureIdentifier);
////                            Structure featureConstruct = (Structure) featureState.object;
//////                        contents.request(tag).state.object = (String) object;
////                            featureConstruct.request(state);
////                        } else {
////                            System.out.println(Application.ANSI_RED + "Error: Specified " + stateType + " is not in the feature's domain." + Application.ANSI_RESET);
////                        }
////                    }
//
//                    Structure construct = Structure.request(expression);
//
//                    if (construct != null) {
//                        if (feature.domain == null || feature.domain.contains(construct)) { // TODO: Make sure 'contains' works!
//                            states.put(featureIdentifier, construct);
//                            // TODO: Update Structure in database
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
//            if (contents.request(tag).types == Type.request("none")) {
//                // TODO: Can't assign anything to the feature object
//                System.out.println("Error: Cannot assign feature with types 'none'.");
//            } else if (contents.request(tag).types == Type.request("any")) {
//                // TODO: Verify that this is correct!
//                contents.request(tag).object = object;
//            } else if (contents.request(tag).types == Type.request("list")) {
//                List contentList = (List) contents.request(tag).object;
//
//                // TODO: Check types of list contents and restrict to the types (or any if "any")
//                // TODO: If specific text tokens are allowed AS WELL AS text construct, text construct subsumes the tokens and the tokens are not included in the domain
//
////                if (contents.request(identifier).listType == Type.request("text")) {
//                if (contents.request(tag).listTypes.contains(Type.request("text"))) {
//                    if (Content.isText((String) object)) {
//                        contentList.request(object);
//                    } else {
//                        System.out.println("Error: Cannot request non-text to list (only can contain text).");
//                    }
////                } else if (contents.request(identifier).listType == Type.request("construct")) {
////                } else if (contents.request(identifier).listTypes.contains(Type.request("construct"))) {
//                } else {
//                    // TODO: Determine if the construct object is allowed into the list based on the specific types!
//                    contentList.request(object);
//                }
//            } else if (contents.request(tag).types == Type.request("text")) {
//                if (Content.isText((String) object)) {
//                    contents.request(tag).object = (String) object;
//                } else {
//                    System.out.println("Error: Cannot assign non-text to text feature.");
//                }
//            } else {
//                contents.request(tag).object = object;
//            }
//            */
//        }
//    }

//    public Feature request(String tag) {
//        if (features.containsKey(tag)) {
//            return features.request(tag);
//        }
//        return null;
//    }

    // TODO: request <list-feature-identifier> : <object>

//    /**
//     * Adds a {@code State} to a <em>list</em> {@code Structure}, which is a {@code Structure} with
//     * a {@code TypeId} uniquely identified by its {@code identifier} equal to {@code "list"}.
//     *
//     * {@code expression} is a <em>state expression</em>.
//     *
//     * @param featureIdentifier
//     * @param expression
//     */
//    public void insert(String featureIdentifier, String expression) {
//        if (features.containsKey(featureIdentifier)) {
//            Feature feature = features.request(featureIdentifier);
//            Structure featureState = states.request(featureIdentifier);
//
//            // Check if feature can be a list
//            if (!feature.types.contains(Type.request("list"))) {
//                System.out.println(Color.ANSI_RED + "Error: Cannot add to a non-list." + Color.ANSI_RESET);
//                return;
//            }
//
//            // Check if feature is currently configured as a list
//            if (featureState.type != Type.request("list")) {
//                // Change the types of the stored object if it is not a list
//                if (featureState == null) {
//                    featureState = Structure.create(Type.request("list"));
//                } else if (featureState.type != Type.request("list")) {
//                    featureState = Structure.create(Type.request("list"));
//                }
//            }
//
//            // Add the object to the list
//            TypeId stateType = Type.request((String) expression);
//            if (stateType != null
//                    && (feature.listTypes == null || feature.listTypes.contains(stateType))) {
//
//                if (stateType == Type.request("none")) {
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
//                } else if (stateType == Type.request("list")) {
//
//                    // Change the types of the stored object if it is not a list
//                    if (featureState == null) {
//                        featureState = Structure.create(stateType);
//                    } else if (featureState.type != stateType) {
////                        featureContent.objectType = List.class;
////                        featureContent.object = new ArrayList<>();
//                        featureState = Structure.create(stateType);
//                    }
//
//                    // Update the object
//
//                } else if (stateType == Type.request("text")) {
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
//                    Structure construct = Structure.request(expression);
//
//
////                    if (Content.isText((String) object)) {
////                    if (feature.domain == null || feature.domain.contains((String) expression)) {
//                    if (feature.domain == null || feature.domain.contains(construct)) {
//                    // TODO: if (feature.domain == null || feature.domain.contains(state)) {
//                            List list = (List) featureState.object;
////                        contents.request(tag).state.object = (String) object;
////                            list.request(expression);
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
//                    Structure construct = Structure.request(expression);
//
//                    // Add to the list in memory
////                    if (Content.isText((String) object)) {
//                    if (construct != null) {
////                        if (feature.domain == null || feature.domain.contains((String) expression)) { // TODO: Update domain to contain State objects so it can contain port and other Constructs
//                        if (feature.domain == null || feature.domain.contains(construct)) { // TODO: Update domain to contain State objects so it can contain port and other Constructs
//                            List list = (List) featureState.object;
////                        contents.request(tag).state.object = (String) object;
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
//            if (contents.request(tag).types == Type.request("none")) {
//                // TODO: Can't assign anything to the feature object
//                System.out.println("Error: Cannot assign feature with types 'none'.");
//            } else if (contents.request(tag).types == Type.request("any")) {
//                // TODO: Verify that this is correct!
//                contents.request(tag).object = object;
//            } else if (contents.request(tag).types == Type.request("list")) {
//                List contentList = (List) contents.request(tag).object;
//
//                // TODO: Check types of list contents and restrict to the types (or any if "any")
//                // TODO: If specific text tokens are allowed AS WELL AS text construct, text construct subsumes the tokens and the tokens are not included in the domain
//
////                if (contents.request(identifier).listType == Type.request("text")) {
//                if (contents.request(tag).listTypes.contains(Type.request("text"))) {
//                    if (Content.isText((String) object)) {
//                        contentList.request(object);
//                    } else {
//                        System.out.println("Error: Cannot request non-text to list (only can contain text).");
//                    }
////                } else if (contents.request(identifier).listType == Type.request("construct")) {
////                } else if (contents.request(identifier).listTypes.contains(Type.request("construct"))) {
//                } else {
//                    // TODO: Determine if the construct object is allowed into the list based on the specific types!
//                    contentList.request(object);
//                }
//            } else if (contents.request(tag).types == Type.request("text")) {
//                if (Content.isText((String) object)) {
//                    contents.request(tag).object = (String) object;
//                } else {
//                    System.out.println("Error: Cannot assign non-text to text feature.");
//                }
//            } else {
//                contents.request(tag).object = object;
//            }
//            */
//        }
//    }

    @Override
    public String toString() {
        if (type2 == Type.request("text")) {
            String content = (String) this.object;
            return "'" + content + "' " + type2 + ".id." + uid + "";
        } else if (type2 == Type.request("list")) {
            String content = "";
            List list = (List) this.object;
            for (int i = 0; i < list.size(); i++) {
                content += list.get(i);
                if ((i + 1) < list.size()) {
                    content += ", ";
                }
            }
            return type2 + ".id." + uid + " : " + content;
        } else {
            return type2 + ".id." + uid;
        }
    }

    public String toColorString() {
        if (type2 == Type.request("text")) {
            String content = (String) this.object;
            // return Color.ANSI_BLUE + type + Color.ANSI_RESET + " '" + content + "' (id: " + uid + ")" + " (uuid: " + uuid + ")";
            return  "'" + content + "' " + Color.ANSI_BLUE + type2 + Color.ANSI_RESET + ".id." + uid;
        } else {
            return Color.ANSI_BLUE + type2 + Color.ANSI_RESET + ".id." + uid;
            // return Color.ANSI_BLUE + type + Color.ANSI_RESET + " (id: " + uid + ")" + " (uuid: " + uuid + ")";
        }
    }
}
