package camp.computer.construct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import camp.computer.util.terminal.Color;
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

    public HashMap<String, Feature> features = new HashMap<>(); // TODO: Remove? Remove setupConfiguration?
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
    // 1. Use types, features, and states for non-terminal (structure) constructs (custom non-primitive constructs)
    // 2. For terminal states (i.e., to replace State), don't use features or states hashes. Store actual data in objectType and object (from State).

    private Construct(Concept concept) {

        this.type = concept.type;

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
                if (feature.types.size() == 1) {
                    // TODO: get default construct...
//                    states.put(feature.identifier, Construct.get(feature.types.get(0).identifier)); // Initialize with only available types if there's only one available
                    states.put(feature.identifier, Construct.create(Type.get("none"))); // Initialize with only available types if there's only one available
                } else {
                    states.put(feature.identifier, null); // Default to "any" types by setting null
                }
            }
        }

//        // Create Content for each Feature
//        for (Feature feature : concept.features.values()) {
//            features.put(feature.identifier, feature);
//            if (feature.types.size() == 1) {
//                states.put(feature.identifier, Construct.get(feature.types.get(0))); // Initialize with only available types if there's only one available
//            } else {
//                states.put(feature.identifier, null); // Default to "any" types by setting null
//            }
//        }

    }

//    public static boolean isPrimitive(Construct construct) {
//        return construct.type != Type.get("none")
//                && construct.type != Type.get("number")
//                && construct.type != Type.get("text")
//                && construct.type != Type.get("list");
//    }

    /**
     * Returns {@code true} if {@code construct} is configured to store one or more
     * <em>primitive</em> constructs. This configuration is determined to be present if the
     * {@code construct} references a {@code Map}.
     * @param construct
     * @return
     */
    public static boolean isComposite(Construct construct) {
//        if (!Construct.isPrimitive(construct) && construct.objectType == Map.class && construct.object != null) {
        if (construct.objectType == Map.class && construct.object != null) {
            return true;
        }
        return false;
    }

    public static Construct create(Type type) {
        Concept concept = Concept.get(type);
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

    // Encapsulate state
    // TODO: Query database for the state corresponding to stateExpression.
    // e.g.,
    // none
    // 3 --or-- 3.4 --or-- 341
    // text(id:23) --or-- 'foo' --or-- text('foo')
    // list(id:44) --or-- port, path, project --or-- 'foo', text('bar'), port(uid:3) --or-- port, path, project
    // port(uid:3)
    public static Construct get(String expression) {
        Type constructType = Type.get(expression);
        if (constructType != null) {

            if (constructType == Type.get("none")) {
                Construct construct = Manager.getPersistentConstruct(expression);
                if (construct == null) {
                    // State wasn't found, so create a new one and return it
                    // TODO: Store in the database
                    construct = Construct.create(constructType);
                }
                return construct;
            } else if (constructType == Type.get("text")) {
                Construct construct = Manager.getPersistentConstruct(expression);
                if (construct == null) {
                    // State wasn't found, so create a new one and return it
                    // TODO: Store in the database
                    if (expression.startsWith("'") && expression.endsWith("'")) {
                        Concept conceptType = Concept.get(constructType);
                        construct = new Construct(conceptType);
                        long uid = Manager.add(construct);
                        construct.object = expression.substring(1, expression.length() - 1);
                    } else {
                        construct = Construct.create(constructType);
                        construct.object = "";
                    }
                }
                return construct;
            } else if (constructType == Type.get("list")) {

            } else {

                Construct construct = Manager.getPersistentConstruct(expression);
                if (construct == null) {

                    // Create new State
                    // TODO: Add new state to persistent store

                    Concept conceptType = Concept.get(constructType);
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

//    public static Construct REFACTOR_getList(Construct currentConstruct, String featureToReplace, Construct featureConstructReplacement) {
    public static Construct REFACTOR_getList(List requestedConstructList) {

        Concept concept = Concept.get(Type.get("list"));
        Construct newListConstruct = new Construct(concept);

        // Copy elements into construct list.
        List constructList = (List) newListConstruct.object;
        for (int i = 0; i < requestedConstructList.size(); i++) {
            constructList.add(requestedConstructList.get(i));
        }

        long uid = Manager.add(newListConstruct);
        return newListConstruct;

    }

    public static Construct REFACTOR_getRevise(Construct currentConstruct, String featureToReplace, Construct featureConstructReplacement) {

        Concept concept = Concept.get(currentConstruct.type);
        Construct newContruct = new Construct(concept);

        // Copy states from source Construct.
        for (String featureIdentifier : currentConstruct.states.keySet()) {
            if (featureIdentifier.equals(featureToReplace)) {
                newContruct.states.put(featureToReplace, featureConstructReplacement);
            } else {
                newContruct.states.put(featureIdentifier, currentConstruct.states.get(featureIdentifier));
            }
        }

        long uid = Manager.add(newContruct);
        return newContruct;

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
    public void set(String featureIdentifier, String expression) {

        // TODO: Check if classType can use "set"

        HashMap<String, Feature> features = (HashMap<String, Feature>) this.object;

        if (features.containsKey(featureIdentifier)) {

            Type constructType = Type.get(expression);
            Feature feature = features.get(featureIdentifier);
//            if (feature.types == null || feature.types.contains(constructType)) {
            if (feature.types.size() == 0 || feature.types.contains(constructType)) {

                /*
                // Get feature's current state
                State state = states.get(featureIdentifier);

                if (stateType == Type.get("none")) {

                    // Remove the types of the stored object
                    if (state == null) {
                        state = State.getState(stateType);
                    } else if (state.types != stateType) {
//                        featureContent.objectType = null;
//                        featureContent.object = null;
                        state = State.getState(stateType);
                    }

                } else if (stateType == Type.get("list")) {

                    // Change the types of the stored object if it is not a list
                    if (state == null) {
                        state = State.getState(stateType);
                    } else if (state.types != stateType) {
//                        featureContent.objectType = List.class;
//                        featureContent.object = new ArrayList<>();
                        state = State.getState(stateType);
                    }

                    // Update the object

                } else if (stateType == Type.get("text")) {

                    // Change the types of the stored object if it is not a string (for text)
//                    if (state == null) {
//                        state = State.getPersistentState(stateType);
//                    } else if (state.types != stateType) {
////                        featureContent.objectType = String.class;
////                        featureContent.object = null;
//                        state = State.getPersistentState(stateType);
//                    }

//                    if (Content.isText((String) object)) {
                   state = State.getState((String) expression);
//                    if (feature.domain == null || feature.domain.contains(stateExpression)) {
                    if (feature.domain == null || feature.domain.contains(state)) { // TODO: Make sure 'contains' works!
//                        state.object = (String) stateExpression;
                        states.put(featureIdentifier, state);
                    } else {
                        System.out.println(Application.ANSI_RED + "Error: Specified text is not in the feature's domain." + Application.ANSI_RESET);
                    }
//                    } else {
//                        System.out.println("Error: Cannot assign non-text to text feature.");
//                    }

                } else {


                }
                */

                if (constructType == Type.get("none")) {

                    Construct construct = Construct.get(expression);

                    if (feature.domain == null || feature.domain.contains(construct)) { // TODO: Make sure 'contains' works!
                        states.put(featureIdentifier, construct);
                        // TODO: Update Construct in database
                    } else {
                        System.out.println(Color.ANSI_RED + "Error: Specified text is not in the feature's domain." + Color.ANSI_RESET);
                    }

//                } else if (stateType == Type.get("text")) {
//
//                    State state = State.getState(expression);
//
//                    if (state != null) {
//                        if (feature.domain == null || feature.domain.contains(state)) { // TODO: Make sure 'contains' works!
//                            states.put(featureIdentifier, state);
//                            // TODO: Update Construct in database
//                        } else {
//                            System.out.println(Application.ANSI_RED + "Error: Specified text is not in the feature's domain." + Application.ANSI_RESET);
//                        }
//                    } else {
//                        System.out.println(Application.ANSI_RED + "Error: Interpreter error. State is null." + Application.ANSI_RESET);
//                    }

                } else if (constructType == Type.get("list")) {

                    // TODO: Allow lists to be assigned? Yes!
                    System.out.println(Color.ANSI_RED + "Error: Cannot SET on a list. (This might change!)." + Color.ANSI_RESET);

                } else {

//                    State state = State.getState(expression);
//
//                    // Add to the list in memory
//                    // TODO: if (state != null && state != states.get(featureIdentifier)) {
//                    if (state != null) {
//                        if (feature.domain == null || feature.domain.contains(state)) { // TODO: Update domain to contain State objects so it can contain port and other Constructs
//                            State featureState = states.get(featureIdentifier);
//                            Construct featureConstruct = (Construct) featureState.object;
////                        contents.get(tag).state.object = (String) object;
//                            featureConstruct.get(state);
//                        } else {
//                            System.out.println(Application.ANSI_RED + "Error: Specified " + stateType + " is not in the feature's domain." + Application.ANSI_RESET);
//                        }
//                    }

                    Construct construct = Construct.get(expression);

                    if (construct != null) {
                        if (feature.domain == null || feature.domain.contains(construct)) { // TODO: Make sure 'contains' works!
                            states.put(featureIdentifier, construct);
                            // TODO: Update Construct in database
                        } else {
                            System.out.println(Color.ANSI_RED + "Error: Specified text is not in the feature's domain." + Color.ANSI_RESET);
                        }
                    } else {
                        System.out.println(Color.ANSI_RED + "Error: Interpreter error. State is null." + Color.ANSI_RESET);
                    }

//                    System.out.println(Application.ANSI_RED + "Error: Feature types mismatches object types." + Application.ANSI_RESET);

                }
            }


            /*
            if (contents.get(tag).types == Type.get("none")) {
                // TODO: Can't assign anything to the feature object
                System.out.println("Error: Cannot assign feature with types 'none'.");
            } else if (contents.get(tag).types == Type.get("any")) {
                // TODO: Verify that this is correct!
                contents.get(tag).object = object;
            } else if (contents.get(tag).types == Type.get("list")) {
                List contentList = (List) contents.get(tag).object;

                // TODO: Check types of list contents and restrict to the types (or any if "any")
                // TODO: If specific text tokens are allowed AS WELL AS text construct, text construct subsumes the tokens and the tokens are not included in the domain

//                if (contents.get(identifier).listType == Type.get("text")) {
                if (contents.get(tag).listTypes.contains(Type.get("text"))) {
                    if (Content.isText((String) object)) {
                        contentList.get(object);
                    } else {
                        System.out.println("Error: Cannot get non-text to list (only can contain text).");
                    }
//                } else if (contents.get(identifier).listType == Type.get("construct")) {
//                } else if (contents.get(identifier).listTypes.contains(Type.get("construct"))) {
                } else {
                    // TODO: Determine if the construct object is allowed into the list based on the specific types!
                    contentList.get(object);
                }
            } else if (contents.get(tag).types == Type.get("text")) {
                if (Content.isText((String) object)) {
                    contents.get(tag).object = (String) object;
                } else {
                    System.out.println("Error: Cannot assign non-text to text feature.");
                }
            } else {
                contents.get(tag).object = object;
            }
            */
        }
    }

//    public Feature get(String tag) {
//        if (features.containsKey(tag)) {
//            return features.get(tag);
//        }
//        return null;
//    }

    // TODO: get <list-feature-identifier> : <object>

    /**
     * Adds a {@code State} to a <em>list</em> {@code Construct}, which is a {@code Construct} with
     * a {@code Type} uniquely identified by its {@code identifier} equal to {@code "list"}.
     *
     * {@code expression} is a <em>state expression</em>.
     *
     * @param featureIdentifier
     * @param expression
     */
    public void insert(String featureIdentifier, String expression) {
        if (features.containsKey(featureIdentifier)) {
            Feature feature = features.get(featureIdentifier);
            Construct featureState = states.get(featureIdentifier);

            // Check if feature can be a list
            if (!feature.types.contains(Type.get("list"))) {
                System.out.println(Color.ANSI_RED + "Error: Cannot add to a non-list." + Color.ANSI_RESET);
                return;
            }

            // Check if feature is currently configured as a list
            if (featureState.type != Type.get("list")) {
                // Change the types of the stored object if it is not a list
                if (featureState == null) {
                    featureState = Construct.create(Type.get("list"));
                } else if (featureState.type != Type.get("list")) {
                    featureState = Construct.create(Type.get("list"));
                }
            }

            // Add the object to the list
            Type stateType = Type.get((String) expression);
            if (stateType != null
                    && (feature.listTypes == null || feature.listTypes.contains(stateType))) {

                if (stateType == Type.get("none")) {

//                    // Remove the types of the stored object
//                    if (featureContent.state == null) {
//                        featureContent.state = new State(objectType);
//                    } else if (featureContent.state.types != objectType) {
////                        featureContent.objectType = null;
////                        featureContent.object = null;
//                        featureContent.state = new State(objectType);
//                    }

                } else if (stateType == Type.get("list")) {

                    // Change the types of the stored object if it is not a list
                    if (featureState == null) {
                        featureState = Construct.create(stateType);
                    } else if (featureState.type != stateType) {
//                        featureContent.objectType = List.class;
//                        featureContent.object = new ArrayList<>();
                        featureState = Construct.create(stateType);
                    }

                    // Update the object

                } else if (stateType == Type.get("text")) {

                    // Change the types of the stored object if it is not a string (for text)
//                    if (featureContent.state == null) {
//                        featureContent.state = new State(objectType);
//                    } else if (featureContent.state.types != objectType) {
////                        featureContent.objectType = String.class;
////                        featureContent.object = null;
//                        featureContent.state = new State(objectType);
//                    }


//                    // Encapsulate text state
//                    State state = State.getState(stateType);
//                    state.object = expression;

                    // Encapsulate text state
                    Construct construct = Construct.get(expression);


//                    if (Content.isText((String) object)) {
//                    if (feature.domain == null || feature.domain.contains((String) expression)) {
                    if (feature.domain == null || feature.domain.contains(construct)) {
                    // TODO: if (feature.domain == null || feature.domain.contains(state)) {
                            List list = (List) featureState.object;
//                        contents.get(tag).state.object = (String) object;
//                            list.get(expression);
                            list.add(construct);
                        } else {
                            System.out.println(Color.ANSI_RED + "Error: Specified " + stateType + " is not in the feature's domain." + Color.ANSI_RESET);
                        }
//                    }
//                    } else {
//                        System.out.println("Error: Cannot assign non-text to text feature.");
//                    }

                } else {

//                    // Change the types of the stored object if it is not a list
//                    if (state == null) {
//                        state = State.getPersistentState(contentType);
//                    } else if (state.types != contentType) {
////                        featureContent.objectType = List.class;
////                        featureContent.object = new ArrayList<>();
//                        state = State.getPersistentState(contentType);
//                    }

                    // Encapsulate text state
                    Construct construct = Construct.get(expression);

                    // Add to the list in memory
//                    if (Content.isText((String) object)) {
                    if (construct != null) {
//                        if (feature.domain == null || feature.domain.contains((String) expression)) { // TODO: Update domain to contain State objects so it can contain port and other Constructs
                        if (feature.domain == null || feature.domain.contains(construct)) { // TODO: Update domain to contain State objects so it can contain port and other Constructs
                            List list = (List) featureState.object;
//                        contents.get(tag).state.object = (String) object;
                            list.add(construct);
                        } else {
                            System.out.println(Color.ANSI_RED + "Error: Specified " + stateType + " is not in the feature's domain." + Color.ANSI_RESET);
                        }
                    }

                }
            } else {
                System.out.println(Color.ANSI_RED + "Error: Feature types mismatches object types." + Color.ANSI_RESET);
            }


            /*
            if (contents.get(tag).types == Type.get("none")) {
                // TODO: Can't assign anything to the feature object
                System.out.println("Error: Cannot assign feature with types 'none'.");
            } else if (contents.get(tag).types == Type.get("any")) {
                // TODO: Verify that this is correct!
                contents.get(tag).object = object;
            } else if (contents.get(tag).types == Type.get("list")) {
                List contentList = (List) contents.get(tag).object;

                // TODO: Check types of list contents and restrict to the types (or any if "any")
                // TODO: If specific text tokens are allowed AS WELL AS text construct, text construct subsumes the tokens and the tokens are not included in the domain

//                if (contents.get(identifier).listType == Type.get("text")) {
                if (contents.get(tag).listTypes.contains(Type.get("text"))) {
                    if (Content.isText((String) object)) {
                        contentList.get(object);
                    } else {
                        System.out.println("Error: Cannot get non-text to list (only can contain text).");
                    }
//                } else if (contents.get(identifier).listType == Type.get("construct")) {
//                } else if (contents.get(identifier).listTypes.contains(Type.get("construct"))) {
                } else {
                    // TODO: Determine if the construct object is allowed into the list based on the specific types!
                    contentList.get(object);
                }
            } else if (contents.get(tag).types == Type.get("text")) {
                if (Content.isText((String) object)) {
                    contents.get(tag).object = (String) object;
                } else {
                    System.out.println("Error: Cannot assign non-text to text feature.");
                }
            } else {
                contents.get(tag).object = object;
            }
            */
        }
    }

    @Override
    public String toString() {
        if (type == Type.get("text")) {
            String content = (String) this.object;
            return type + " '" + content + "' (id:" + uid + ")";
        } else {
            return type + " (id:" + uid + ")";
        }
    }

    public String toColorString() {
        if (type == Type.get("text")) {
            String content = (String) this.object;
            return Color.ANSI_BLUE + type + Color.ANSI_RESET + " '" + content + "' (id:" + uid + ")";
        } else {
            return Color.ANSI_BLUE + type + Color.ANSI_RESET + " (id:" + uid + ")";
        }
    }
}
