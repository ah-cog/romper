package camp.computer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import camp.computer.network.Repository;
import camp.computer.OLD_construct.Construct_v1;
import camp.computer.OLD_construct.ControllerConstruct;
import camp.computer.OLD_construct.DeviceConstruct;
import camp.computer.OLD_construct.OperationConstruct;
import camp.computer.OLD_construct.PathConstruct;
import camp.computer.OLD_construct.PortConstruct;
import camp.computer.OLD_construct.ProjectConstruct;
import camp.computer.OLD_construct.ScriptConstruct;
import camp.computer.OLD_construct.TaskConstruct;
import camp.computer.construct.Concept;
import camp.computer.construct.Construct;
import camp.computer.construct.Error;
import camp.computer.construct.Expression;
import camp.computer.construct.Feature;
import camp.computer.construct.Identifier;
import camp.computer.construct.Reference;
import camp.computer.construct.Type;
import camp.computer.data.format.configuration.Configuration;
import camp.computer.data.format.configuration.Variable;
import camp.computer.platform_infrastructure.LoadBuildFileTask;
import camp.computer.util.Pair;
import camp.computer.util.Tuple;
import camp.computer.util.console.Color;
import camp.computer.workspace.Manager_v1;
import camp.computer.workspace.Manager;

public class Interpreter {

    private List<String> inputLines = new ArrayList<>();

    // TODO: Move list of Processes into Workspace
    private List<OperationConstruct> operationConstructs = new ArrayList<>(); // TODO: Define namespaces (or just use construct and set types from default "container" to "namespace", then get an interpreter for that types)!

    // <SETTINGS>
    public static boolean ENABLE_VERBOSE_OUTPUT = false;
    // </SETTINGS>

    private static Interpreter instance = null;

    private Context context = null;

    private Workspace workspace;

    private Interpreter() {
        Interpreter.instance = this;
        workspace = new Workspace();

        // Instantiate primitive types
        Type.add("none");
        // Type.get("number");
        Type.add("text");
        Type.add("list");

        // Instantiate primitive concepts
        if (!Concept.exists(Type.get("none"))) {
            Concept noneConcept = Concept.request(Type.get("none"));
        }

        /*
        if (!Concept.exists(Type.get("number"))) {
            Concept numberConcept = Concept.request(Type.get("number"));
        }
        */

        if (!Concept.exists(Type.get("text"))) {
            Concept textConcept = Concept.request(Type.get("text"));
        }

        if (!Concept.exists(Type.get("list"))) {
            Concept listConcept = Concept.request(Type.get("list"));
        }
    }

    public static Interpreter getInstance() {
        if (Interpreter.instance == null) {
            Interpreter interpreter = new Interpreter();
            return interpreter;
        } else {
            return Interpreter.instance;
        }
    }

    public void start() {

        Scanner scanner = new Scanner(System.in);
        String inputLine = null;

        while (true) {
            System.out.print("~ ");
            inputLine = scanner.nextLine();
            interpretLine(inputLine);
        }

    }

    public void interpretLine(String inputLine) {

        // <SANITIZE_INPUT>
        if (inputLine.contains("#")) {
            inputLine = inputLine.substring(0, inputLine.indexOf("#"));
        }

        inputLine = inputLine.trim();
        // </SANITIZE_INPUT>

        // <VALIDATE_INPUT>
        if (inputLine.length() == 0) {
            return;
        }
        // </VALIDATE_INPUT>

        if (workspace.operationConstruct != null && !inputLine.startsWith("stop")) {
            workspace.operationConstruct.operations.add(inputLine);
        } else {

            // Save line in history
            this.inputLines.add(inputLine);

            // Store context in history
            // Context context = new Context();
            if (context == null) {
                context = new Context();
            }
            context.inputLine = inputLine.trim();

            // These tokens are chosen based on the idea that they narrate the work that you do when
            // using the language or narrate the thoughts that you're having when you use the
            // language. In other words, the grammar is designed so using it can feel like thinking
            // a stream of consciousness.

            if (context.inputLine.startsWith("import file")) {
                importFileTask(context);
            } else if (context.inputLine.startsWith("define") || context.inputLine.startsWith("def")) { // "new", "describe"
                defineTask(context);
            } else if (context.inputLine.startsWith("tag")) {
                // TODO: tag or alias given identifier (construct) with given text tag
            } else if (context.inputLine.startsWith("has")) {
                hasTask(context);
            } else if (context.inputLine.startsWith("let")) {
                letTask(context);
            } else if (context.inputLine.startsWith("set")) {
                setTask(context);
            } else if (context.inputLine.startsWith("add")) {
                addTask(context);
            } else if (context.inputLine.startsWith("remove") || context.inputLine.startsWith("rem") || context.inputLine.startsWith("rm")) {
                removeTask(context);
            } else if (context.inputLine.equals("what")) {
                whatTask(context);
            } else if (context.inputLine.equals("which")) {
                whichTask(context);
            } else if (context.inputLine.startsWith("describe") || context.inputLine.startsWith("ds")) { // previously: list, index, inspect, view, ls, cite, db, browse
                // TODO: full command "list"
                describeTask(context);
            } else if (context.inputLine.startsWith("locate")) { // previously: ws, show, ls
                searchTask(context);
            } else if (context.inputLine.startsWith("next")) {
                // TODO: Allow if previous command was "search" and more items exist (or say no more exist)
            } else if (context.inputLine.startsWith("previous")) {
                // TODO: Allow if previous command was "search" and previous items exist (or say no more exist)
//            } else if (context.inputLine.startsWith("print")) {
//                printTask(context);
            } else if (context.inputLine.equals("exit")) {
                exitTask(context);
            } else {
                // TODO: Validate string as valid construct instance identifier.

                if (Type.has(context.inputLine)) {

                    addressTask(context); // "address" here is in the sense of "Hereafter, until otherwise specified, expressions address concept or construct <X>."

                } else if (Expression.isConstruct(inputLine)) {

                    String typeIdentifierToken = context.inputLine.substring(0, context.inputLine.indexOf("(")).trim(); // text before '('
                    String addressTypeToken = context.inputLine.substring(context.inputLine.indexOf("(") + 1, context.inputLine.indexOf(":")).trim(); // text between '(' and ':'
                    String addressToken = context.inputLine.substring(context.inputLine.indexOf(":") + 1, context.inputLine.indexOf(")")).trim(); // text between ':' and ')'

                    if (Type.has(typeIdentifierToken)) {
                        if (addressTypeToken.equals("id")) {
                            long uid = Long.parseLong(addressToken);
                            Identifier identifier = Manager.get(uid);
                            if (identifier == null) {
                                System.out.println(Color.ANSI_RED + "Error: No concept with UID " + uid + Color.ANSI_RESET);
                            } else if (identifier.getClass() == Reference.class) {
                                Reference reference = (Reference) identifier;
                                Construct construct = (Construct) reference.object;
//                                System.out.println("[FOUND] reference " + reference.type + " construct (UID: " + uid + ")");
                                System.out.println("[FOUND] reference " + construct.type+ " (id: " + reference.uid + ") -> construct " + construct.type + " (id: " + construct.uid + ")" + " (uuid: " + construct.uuid + ")");

                                // Update object
//                                currentContextType = ContextType.CONSTRUCT;
                                context.currentIdentifier = reference;
                            } else if (identifier.getClass() == Construct.class) {
                                Construct construct = (Construct) identifier;
                                System.out.println("Found " + construct.type + " construct (UID: " + uid + ")");

                                // Update object
//                                currentContextType = ContextType.CONSTRUCT;
                                context.currentIdentifier = construct;

                            } else if (identifier.getClass() == Concept.class) {
                                System.out.println(Color.ANSI_RED + "Error: The UID is for a concept." + Color.ANSI_RESET);
//                                Concept concept = (Concept) identifier;
//                                System.out.println("Found " + concept.types + " with UID " + uid);
                            }
                        } else if (context.inputLine.contains("uuid:")) {

                        }
                    }
                } else {
                    System.out.println(Color.ANSI_RED + "Error: Unsupported expression." + Color.ANSI_RESET);
                }
            }
        }
    }

    public void defineTask(Context context) {

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            System.out.println("Usage: describe <concept-name>");

        } else if (inputLineTokens.length == 2) {

            String typeToken = inputLineTokens[1];

            if (!Type.has(typeToken)) {
                Type.add(typeToken);
            }

            Type type = Type.get(typeToken);

            if (type == Type.get("none")
                    || type == Type.get("text")
                    || type == Type.get("list")) {
                System.out.println(Error.get("Cannot change description of primitive classType."));
                return;
            }

            if (!Concept.exists(type)) {
                Concept.request(type);
            }

            // System.out.println("Error: Construct already exists.");

            Concept concept = Concept.request(type);

            System.out.println("concept " + concept + " (uuid: " + concept.uuid + ")");

            // Update object
            context.currentIdentifier = concept;

        }
    }

    // Examples:
    // has voltage list : 'none', 'cmos', 'ttl'
    public void hasTask(Context context) {

        String[] inputLineSegments = context.inputLine.split("[ ]*:[ ]*");

        // Determine interpreter's object. Concept or instance?
        if (Context.isConcept(context)) {

            // Defaults
            String featureIdentifier = null;
            List<Type> featureTypes = null; // new ArrayList<>(); // if size == 0, then unconstrained!
            List<Type> listTypes = null; // new ArrayList<>(); // if size == 0, then unconstrained!

            boolean hasError = false;

            // Determine identifier and types
            if (inputLineSegments.length >= 1) {

                String[] inputLineTokens = inputLineSegments[0].split("[ ]+");

                // Determine identifier
                featureIdentifier = inputLineTokens[1];

                // <REFACTOR>
                // Check if the feature already exists in the current object
                if (Context.getConcept(context).features.containsKey(featureIdentifier)) {
                    System.out.println(Color.ANSI_RED + "Warning: Context already contains feature '" + featureIdentifier + "'. A new construct revision will be generated." + Color.ANSI_RESET);
                }
                // </REFACTOR>

                // Determine types
                if (inputLineTokens.length >= 3) {
                    String featureTypeToken = inputLineTokens[2];

                    if (featureTypes == null) {
                        featureTypes = new ArrayList<>();
                    }

//                    if (featureTypeToken.equals("text")) {
//                        featureTypes.get(Type.get(featureTypeToken));
//                    } else
                    if (featureTypeToken.equals("list")) {
                        featureTypes.add(Type.get(featureTypeToken));
                        if (Type.has(featureIdentifier)) {
                            if (listTypes == null) {
                                listTypes = new ArrayList<>();
                            }
                            listTypes.add(Type.get(featureIdentifier)); // If identifier is a construct types, then constraint list to that types by default
                        } else {
//                            listTypes.get(Type.get("any")); // If identifier is non-construct types then default list types is "any"
                            listTypes = null;
                        }
                    } else {
                        // TODO: Refactor. There's some weird redundancy here with 'has' and 'Type.get'.
                        if (Type.has(featureTypeToken)) {
                            featureTypes.add(Type.get(featureTypeToken));
                        }
                    }
                } else {
                    if (Type.has(featureIdentifier)) {
//                    if (camp.computer.construct.Concept.has(featureTagToken)) {
//                            // TODO: Replace with ConstructType for reserved construct types
//            System.out.println("(id: " + concept.uid + ") " + Application.ANSI_BLUE + typeToken + Application.ANSI_RESET + " (uuid: " + concept.uuid + ")");
                        if (featureTypes == null) {
                            featureTypes = new ArrayList<>();
                        }
                        featureTypes.add(Type.get(featureIdentifier));
                    } else {
                        // Can contain any types (no types is specified)
//                        featureTypes.get(Type.get("any")); // Default types
                    }
                }
            }

            // Determine constraints
            // TODO: Replace with counters for each of these possibilities!
            boolean hasContentConstraint = false;
            boolean isSingletonList = false;
            boolean isTextContent = true;
            boolean hasTextContent = false;
            boolean isConstructContent = true;
            boolean hasConstructContent = false;
            boolean hasDomainList = false;
            boolean hasInvalidConstruct = false;

            List<Construct> featureDomain = new ArrayList<>();
            if (inputLineSegments.length >= 2) {
                hasContentConstraint = true;

                String[] constraintTokens = inputLineSegments[1].split("[ ]*,[ ]*");

                // Determine types
                // Note: Start with ANY, but constrain with increasing generality based on any examples present.
                if (constraintTokens.length == 1) {
                    isSingletonList = true;
                }

                // Determine elements of object list
                for (int i = 0; i < constraintTokens.length; i++) {
                    String constraintToken = constraintTokens[i].trim();
                    if (!constraintToken.startsWith("'") || !constraintToken.endsWith("'")) {
                        hasConstructContent = true;
                        isTextContent = false;
                    } else if (constraintToken.startsWith("'") && constraintToken.endsWith("'")) {
                        hasTextContent = true;
                        isConstructContent = false;
                    } else if ((constraintToken.startsWith("'") && !constraintToken.endsWith("'"))
                            || (!constraintToken.startsWith("'") && constraintToken.endsWith("'"))) {
                        hasInvalidConstruct = true;
                    }
                }

                // Set feature types or check for conflict with current feature types
                if (!hasInvalidConstruct) {
                    if (featureTypes == null) { // i.e., types is "any" (default unless specified)
                        // Remove types "any" so constrained types can be specified
//                        featureTypes.remove(Type.get("any"));
                        // TODO: isListContent
                        if (isTextContent) {
                            featureTypes = new ArrayList<>();
                            featureTypes.add(Type.get("text"));
                            hasDomainList = true;
                        } else if (isConstructContent) {
                            // TODO: Use generic "construct" types or set specific if there's only one
                            if (isSingletonList) {
                                // e.g., has mode : list
                                // e.g., has source : port
                                featureTypes = new ArrayList<>();
                                featureTypes.add(Type.get(constraintTokens[0]));
                                // TODO: if 'text' or other construct only, then set types to featureType = TEXT and hasDomainList = false
                                if (featureTypes.contains(Type.get("list"))) {
//                                    listTypes.get(Type.get("any"));
                                }
                            } else {
                                // e.g., has my-feature : list, port
                                featureTypes = new ArrayList<>();
                                for (int i = 0; i < constraintTokens.length; i++) {
                                    featureTypes.add(Type.get(constraintTokens[i]));
                                }
                            }
                        } else if (hasTextContent && hasConstructContent) {
                            featureTypes = new ArrayList<>();
                            // Add 'text' list types
                            featureTypes.add(Type.get("text"));
                            // Add custom types to list types
                            for (int i = 0; i < constraintTokens.length; i++) {
                                // TODO: Check if types is valid!
                                if (Type.has(constraintTokens[i])) {
                                    featureTypes.add(Type.get(constraintTokens[i]));
                                    // TODO: Check for non-existent Types (from tokens)
                                }
                            }
                            // TODO: Add text literals/examples to domain
                            hasDomainList = true;
                            // TODO: Test this... (e.g., with "has foo list : port, 'bar'")
                        }
                    } else if (featureTypes.contains(Type.get("text"))) {
//                        if (isConstructContent) { // e.g., has my-feature text : non-text
//                            hasError = true;
////                            for (int i = 0; i < constraintTokens.length; i++) {
////                                String constraintToken = constraintTokens[i].trim();
////                                if (!constraintToken.equals("text")) {
////                                    hasError = true;
////                                }
////                            }
//                        } else
                        if (!isTextContent) { // INVALID: e.g., has my-feature text : 'foo', 'bar', foo-feature
                            hasError = true;
                        } else if (isTextContent) {
                            hasDomainList = true;
                        }
                    } else if (featureTypes.contains(Type.get("list"))) {
                        // Remove types "any" so constrained types can be specified
//                        if (listTypes.contains(Type.get("any"))) {
//                            listTypes.remove(Type.get("any"));
//                        }
                        if (isTextContent) {
                            // e.g., has mode list : 'none', 'input', 'output', 'bidirectional'
                            if (listTypes == null) {
                                listTypes = new ArrayList<>();
                            }
                            listTypes.add(Type.get("text"));
                            hasDomainList = true;
                        } else if (isConstructContent) {
                            // e.g., has ports list : port
                            // e.g., has ports-and-paths list : port, path
                            if (isSingletonList) {
                                // e.g., has ports list : port
                                if (!Type.has(constraintTokens[0])) {
                                    // Error: Invalid list object types.
                                    hasError = true;
                                } else {
//                                    // Remove "any" time so types constraint functions as expected
//                                    if (listTypes.contains(Type.get("any"))) {
//                                        listTypes.remove(Type.get("any"));
//                                    }
                                    if (listTypes == null) {
                                        listTypes = new ArrayList<>();
                                    }
                                    // Add the list types constraint
                                    listTypes.add(Type.get(constraintTokens[0]));
                                }
                            } else {
                                // e.g., has ports-and-paths list : port, path
                                // TODO: Convert listType to list and get all listed construct types to the types list
//                                listTypes.get(Type.get("construct"));
                                if (listTypes == null) {
                                    listTypes = new ArrayList<>();
                                }
                                for (int i = 0; i < constraintTokens.length; i++) {
                                    listTypes.add(Type.get(constraintTokens[i]));
                                    // TODO: Check for non-existent Types (from tokens)
                                }
                                hasDomainList = true;
                            }
                        } else if (hasTextContent && hasConstructContent) {
//                            listTypes.get(Type.get("any"));
//                            hasDomainList = true;
                            if (listTypes == null) {
                                listTypes = new ArrayList<>();
                            }
                            // Add 'text' list types
                            listTypes.add(Type.get("text"));
                            // Add custom types to list types
                            for (int i = 0; i < constraintTokens.length; i++) {
                                // TODO: Check if types is valid!
                                if (Type.has(constraintTokens[i])) {
                                    listTypes.add(Type.get(constraintTokens[i]));
                                    // TODO: Check for non-existent Types (from tokens)
                                }
                            }
                            // TODO: Add text literals/examples to domain
                            hasDomainList = true;

                        }
//                    } else if (featureTypes.contains(Type.get("construct"))) {
//                        // TODO: Check if the specific construct presently assigned to featureType matches the list (should be identical)
//                        if (!isConstructContent) {
//                            hasError = true;
//                        } else {
//                            for (int i = 0; i < constraintTokens.length; i++) {
//                                String constraintToken = constraintTokens[i].trim();
//                                // TODO: Verify this... it might be a bug...
//                                if (!constraintToken.equals(featureTagToken)) { // NOTE: featureTagToken is the custom types identifier.
//                                    hasError = true;
//                                }
//                            }
//                        }
                    } else {
                        // Custom non-primitive construct types
                        // e.g., has source port : port(uid:3), port(uid:4), port(uid:5)
                        // TODO: Make sure that the constraint list contains feature object of the correct types (as in the above example)
                        for (int i = 0; i < constraintTokens.length; i++) {
                            String constraintToken = constraintTokens[i].trim();
                            for (int j = 0; j < featureTypes.size(); j++) {
                                if (!constraintToken.equals(featureTypes.get(j).identifier)) { // NOTE: featureTagToken is the custom types identifier.
                                    hasError = true;
                                }
                            }
                        }
                    }
                    // TODO: NONE, ANY
                    // TODO: Support listType = Feature.Type.LIST
                }

                // Set general error flag based on specific error flags.
                if (hasInvalidConstruct) {
                    hasError = true;
                }

                // Remove construct-level elements in list (i.e., non-object)
                for (int i = constraintTokens.length - 1; i >= 0; i--) {
                    String constraintToken = constraintTokens[i].trim();
                    // Remove non-literal from domain
                    if (!constraintToken.startsWith("'") && !constraintToken.endsWith("'")) {
                        constraintTokens[i] = null;
                    }
                }

                // Add object to feature's object domain
                if (!hasError && hasDomainList) {
                    for (int i = 0; i < constraintTokens.length; i++) {
                        String constraintToken = constraintTokens[i];
                        if (constraintToken != null) {
                            Construct state = Construct.get(constraintToken.trim());
                            featureDomain.add(state);
                        }
                    }
                }

            }

            // Instantiate feature, get to construct, and print response
            if (hasError) {
                System.out.println(Color.ANSI_RED + "Error: Conflicting types present in expression." + Color.ANSI_RESET);
            } else if (featureIdentifier != null) {
                // Store feature. Allocates memory for and stores feature.
                Feature feature = new Feature(featureIdentifier);
                if (featureTypes != null) {
                    feature.types.addAll(featureTypes);
                }
                if (feature.types.contains(Type.get("list"))) {
                    if (listTypes != null) {
                        if (feature.listTypes == null) {
                            feature.listTypes = new ArrayList<>();
                        }
                        feature.listTypes.addAll(listTypes);
                    }
                }
                if (hasDomainList) {
                    // TODO: DEBUG! feature.domain should be null for ANY and empty for none
                    if (feature.domain == null) {
                        feature.domain = new ArrayList<>();
                    }
                    feature.domain.addAll(featureDomain);
                }
                // TODO: Create new version of concept here if feature is changed?
                Context.getConcept(context).features.put(featureIdentifier, feature);
                long uid = Manager.add(feature);
                // TODO: initialize "text" with default empty string construct reference (and other types accordingly)

                // Print response
                String typeString = "";
                for (int i = 0; i < feature.types.size(); i++) {
                    typeString += "" + feature.types.get(i).toColorString();
                    if ((i + 1) < feature.types.size()) {
                        typeString += ", ";
                    }
                }

                if (feature.types.size() == 1) {
                    System.out.print("feature " + feature.toColorString() + " type " + typeString + " ");
                } else if (feature.types.size() > 1) {
                    System.out.print("feature " + feature.toColorString() + " types " + typeString + " ");
                }

//                if (feature.types == Type.get("text")) {
                if (feature.types.contains(Type.get("text"))) {
                    if (feature.domain != null && feature.domain.size() == 0) {
                        // System.out.print("can assign text");
                    } else if (feature.domain != null && feature.domain.size() > 0) {
                        // System.out.print("can assign: ");
                        System.out.print("domain ");
                        for (int i = 0; i < feature.domain.size(); i++) {
                            System.out.print(feature.domain.get(i).toColorString());
                            if ((i + 1) < feature.domain.size()) {
                                System.out.print(", ");
                            }
                        }
                    }
//                } else if (feature.types == Type.get("list")) {
                } else if (feature.types.contains(Type.get("list"))) {
                    // Print list of types the list can contain
                    // System.out.print("can contain ");
                    if (feature.listTypes == null) {
                        // System.out.print("any construct");
                    } else {
                        System.out.print("contains ");
                        for (int i = 0; i < feature.listTypes.size(); i++) {
                            System.out.print(feature.listTypes.get(i).toColorString());
                            if ((i + 1) < feature.listTypes.size()) {
                                System.out.print(", ");
                            }
                        }
                    }
                    // Print the list of object that the list can contain
                    if (feature.domain != null && feature.domain.size() > 0) {
                        // System.out.print(" domain ");
                        System.out.print(": ");
                        for (int i = 0; i < feature.domain.size(); i++) {
                            System.out.print(feature.domain.get(i).toColorString());
                            if ((i + 1) < feature.domain.size()) {
                                System.out.print(", ");
                            }
                        }
                    }
//                    if (feature.listTypes.contains(Type.get("text"))) {
////                    } else if (feature.listType == Type.get("construct")) {
////                    } else if (feature.listTypes.contains(Type.get("construct"))) {
//////                        System.out.print("can contain " + feature.listType + ": ");
////                        System.out.print("can contain construct: ");
////                        for (int i = 0; i < feature.domain.size(); i++) {
////                            System.out.print(feature.domain.get(i) + " ");
////                        }
////                    } else if (feature.listType == Type.get("any")) {
//                    } else if (feature.listTypes.contains(Type.get("any"))) {
//                        System.out.print("can contain " + Type.get("any") + "");
////                    } else if (Type.has(feature.listType.identifier)) {
//                    } else { // if (Type.has(feature.listType.identifier)) {
//                        for (int i = 0; i < feature.listTypes.size(); i++) {
//                            System.out.print("can contain " + feature.listTypes.get(i) + ": ");
//                        }
////                        System.out.print("can contain " + feature.listType + ": ");
//                        for (int i = 0; i < feature.domain.size(); i++) {
//                            System.out.print(feature.domain.get(i) + " ");
//                        }
//                    }
////                } else if (feature.types == Type.get("construct")) {
////                } else if (feature.types.contains(Type.get("construct"))) { // TODO: Don't use general "construct"
                } else {
                    // Print list of types the feature can be assigned
                    System.out.print("can assign ");
                    for (int i = 0; i < feature.types.size(); i++) {
                        System.out.print(feature.types.get(i).toColorString());
                        if ((i + 1) < feature.types.size()) {
                            System.out.print(", ");
                        }
                    }
                    if (feature.domain != null && feature.domain.size() > 0) {
                        System.out.print(": ");
                        for (int i = 0; i < feature.domain.size(); i++) {
                            System.out.print(feature.domain.get(i).toColorString() + " ");
                        }
                    }
                }
                System.out.println();

            } else {
                // Print response
                System.out.println(Color.ANSI_RED + "Error: Bad feature syntax." + Color.ANSI_RESET);
            }

        } else if (Context.isConcept(context)) {

            // TODO:

        }

    }

    public void addressTask(Context context) {

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        // Defaults
        String featureIdentifierToken = null;

        // Determine identifier
        if (inputLineTokens.length >= 1) {
            featureIdentifierToken = inputLineTokens[0];

                Type type = null;
                Concept concept = null;
                if (Type.has(featureIdentifierToken)) {
                    type = Type.get(featureIdentifierToken);
                    if (Concept.exists(type)) {
                        concept = Concept.request(type);
                    }
                }

                if (type != null && concept != null) {
                    Construct construct = Construct.create(type);

                    Reference constructReference = Reference.create(construct);
                    System.out.println("reference " + type.toColorString() + " -> construct " + construct.toColorString());

//                    System.out.println("(id: " + construct.uid + ") " + Application.ANSI_GREEN + construct.types + Application.ANSI_RESET + " (uuid: " + construct.uuid + ")");
//                    System.out.println("construct " + Application.ANSI_GREEN + construct.type + Application.ANSI_RESET + " (id: " + construct.uid + ")" + " (uuid: " + construct.uuid + ")");

                    // Update object
//                    currentIdentifier = construct;
                    context.currentIdentifier = constructReference;
                } else {
                    System.out.println(Color.ANSI_RED + "Error: No types or concept matches '" + featureIdentifierToken + "'" + Color.ANSI_RESET);
                }

            }

            // Parse constraint
//            String letParameters = object.inputLine.substring(object.inputLine.indexOf(":") + 1);
//            String[] letParameterTokens = letParameters.split("[ ]*,[ ]*");

//            System.out.println("let parameters (" + letParameterTokens.length + "): " + letParameters);
//            for (int i = 0; i < letParameterTokens.length; i++) {
//                System.out.println("\t" + letParameterTokens[i].trim());
//            }

//        }

    }

    // e.g.,
    // set mode 'analog'
    // set direction 'input'
    // set source-port port(id:42)
    public void setTask(Context context) {

        // Determine interpreter's object. Concept or instance?
        if (Context.isConstruct(context)) {

            String[] inputLineTokens = context.inputLine.split("[ ]+");

            // Defaults
            String featureIdentifier = null;

            // Determine identifier
            if (inputLineTokens.length >= 3) {

                // Extract feature identifier and feature state
                featureIdentifier = inputLineTokens[1];
                String stateExpression = inputLineTokens[2];

                // TODO: if featureContentToken is instance UID/UUID, look it up and pass that into "set"

                Construct currentConstruct = Context.getConstruct(context);
                HashMap<String, Feature> currentConstructFeatures = (HashMap<String, Feature>) currentConstruct.object;

                Construct currentFeatureConstruct = currentConstruct.states.get(featureIdentifier);

                if ((currentConstructFeatures.get(featureIdentifier).types.size() == 1 && currentConstructFeatures.get(featureIdentifier).types.contains(Type.get("list")))
                        || currentFeatureConstruct.type == Type.get("list")) {
                    System.out.println(Color.ANSI_RED + "Error: Cannot assign non-list to a list." + Color.ANSI_RESET);
                } else {

                    Construct replacementFeatureConstruct = Construct.get(stateExpression);

                    Construct replacementConstruct = Manager.getPersistentConstruct(currentConstruct, featureIdentifier, replacementFeatureConstruct);
                    if (replacementConstruct != null) {
                        ((Reference) context.currentIdentifier).object = replacementConstruct;
                        if (currentConstruct == replacementConstruct) {
                            System.out.print("[SAME CONSTRUCT] ");
                        } else {
                            System.out.print("[SWITCHED CONSTRUCT] ");
                        }
                        currentConstruct = (Construct) ((Reference) context.currentIdentifier).object;
//                    System.out.println("REPLACEMENT: " + replacementConstruct);
                        System.out.println("reference " + currentConstruct.type.toColorString() + " (id: " + context.currentIdentifier.uid + ") -> construct " + currentConstruct.type.toColorString() + " (id: " + currentConstruct.uid + ")" + " (uuid: " + currentConstruct.uuid + ")");
                    }

//                currentConstruct.set(featureIdentifier, stateExpression);
                    Construct featureConstruct = currentConstruct.states.get(featureIdentifier);

//                System.out.println(currentConstruct);
                    System.out.println(featureConstruct.toColorString());
                }

            }

        } else {
            System.out.println(Color.ANSI_RED + "Error: Cannot set feature on concept." + Color.ANSI_RESET);
        }

    }

    // add some-list : port(id:34), port(uuid:<uuid>), port(id:44)
    // add some-list port(id:34), port(uuid:<uuid>), port(id:44)
    public void addTask(Context context) {

        // Determine interpreter's object. Concept or instance?
        if (Context.isConstruct(context)) {

            // Sanitize expression
            context.inputLine = context.inputLine.replaceAll("[ ]{2,}", " ");
            context.inputLine = context.inputLine.replaceAll("[ ]+\\(", "(");
            context.inputLine = context.inputLine.replaceAll("[ ]+:[ ]+", ":");

            // Defaults
            String featureIdentifier = null;

            // Tokenize
            int startIndex = context.inputLine.indexOf(" ") + 1;
            int stopIndex = context.inputLine.indexOf(" ", startIndex);
            featureIdentifier = context.inputLine.substring(startIndex, stopIndex);
            String[] stateExpressionSegment = context.inputLine.substring(stopIndex + 1).split("[ ]*,[ ]*");

            // TODO: search for existing list construct that matches the requested list
            // TODO: + if the list doesn't exist, create it
            // TODO: with the existing or created list, search for the referenced construct (in Reference) that matches the would-be updated list
            // TODO: + if the construct doesn't exist with the specified permutation hierarchy, then create it and return that
            // TODO: ++ update the referenced construct in Reference

            // Parse
            for (int j = 0; j < stateExpressionSegment.length; j++) {
                String stateExpression = stateExpressionSegment[j];

                // TODO: if featureContentToken is instance UID/UUID, look it up and pass that into "set"

                Construct currentConstruct = (Construct) ((Reference) context.currentIdentifier).object;
                HashMap<String, Feature> currentConstructFeatures = (HashMap<String, Feature>) currentConstruct.object;

                // Check if feature is valid. If not, show error.
                if (!currentConstructFeatures.containsKey(featureIdentifier)) {
                    System.out.println(Error.get("Error: " + stateExpression + " is not a feature."));
                    return;
                }

                Construct currentFeatureConstruct = currentConstruct.states.get(featureIdentifier);

                if ((currentConstructFeatures.get(featureIdentifier).types.size() == 1 && currentConstructFeatures.get(featureIdentifier).types.contains(Type.get("list")))
                        || currentFeatureConstruct.type == Type.get("list")) {

                    Construct additionalFeatureConstruct = Construct.get(stateExpression); // replacementConstruct

                    ArrayList requestedConstructList = new ArrayList();
                    if (currentFeatureConstruct.type == Type.get("list")) {
                        requestedConstructList.addAll(((List) currentFeatureConstruct.object));
                    }
                    requestedConstructList.add(additionalFeatureConstruct);

                    // TODO: Search for list!
                    Construct replacementFeatureConstruct = Manager.getPersistentListConstruct(requestedConstructList);
                    System.out.println(replacementFeatureConstruct);

                    // TODO: Search for Construct with new list...
                    Construct replacementConstruct = Manager.getPersistentConstruct(currentConstruct, featureIdentifier, replacementFeatureConstruct);
//                    System.out.println("reference -> " + replacementConstruct);

                    if (replacementConstruct != null) {
                        ((Reference) context.currentIdentifier).object = replacementConstruct;
                        if (currentConstruct == replacementConstruct) {
                            System.out.print("[SAME CONSTRUCT] ");
                        } else {
                            System.out.print("[SWITCHED CONSTRUCT] ");
                        }
                        currentConstruct = (Construct) ((Reference) context.currentIdentifier).object;
//                    System.out.println("REPLACEMENT: " + replacementConstruct);
                        System.out.println("reference " + currentConstruct.type.toColorString() + " (id: " + context.currentIdentifier.uid + ") -> construct " + currentConstruct.type.toColorString() + " (id: " + currentConstruct.uid + ")" + " (uuid: " + currentConstruct.uuid + ")");
                    }

                } else {
                    System.out.println(Color.ANSI_RED + "Error: Cannot assign non-list to a list." + Color.ANSI_RESET);

                }





//                Construct currentConstruct = (Construct) ((Reference) currentIdentifier).object;
////                Construct currentFeatureConstruct = currentConstruct.states.get(featureIdentifier);
//
////                Construct replacementFeatureConstruct = Construct.get(stateExpression);
////                Construct replacementConstruct = Manager.getPersistentConstruct(currentConstruct, featureIdentifier, replacementFeatureConstruct);
//
////                ((Construct) currentIdentifier).insert(featureIdentifier, featureContentToken);
//
//                System.out.print(featureIdentifier + " : ");
////                List list = (List) ((Construct) currentIdentifier).states.get(featureIdentifier).object;
//
//
//                Construct additionalFeatureConstruct = Construct.get(stateExpression); // replacementConstruct
//                ArrayList requestedConstructList = new ArrayList();
//                // TODO: Search for list!
//
//
//                List list = (List) currentConstruct.states.get(featureIdentifier).object;
//                for (int i = 0; i < list.size(); i++) {
//                    System.out.print(((Construct) list.get(i)));
//                    if ((i + 1) < list.size()) {
//                        System.out.print(", ");
//                    }
//                }
//                System.out.println();
            }

        }
    }

    public void removeTask(Context context) {

        // TODO:

    }

    public void whatTask(Context context) {
        if (context.currentIdentifier != null) {
            if (Context.isConcept(context)) {
                Concept concept = Context.getConcept(context);
                System.out.println(concept.type);
            } else if (Context.isConstruct(context)) {
                Construct construct = Context.getConstruct(context);
                System.out.println(construct.type);
            }
        } else {
            Error.get("Error: No identifier in context.");
        }
    }

    public void whichTask(Context context) {
        if (context.currentIdentifier != null) {
            if (Context.isConcept(context)) {
                Concept concept = Context.getConcept(context);
                System.out.println(concept);
            } else if (Context.isConstruct(context)) {
                Construct construct = Context.getConstruct(context);
                System.out.println(construct.type + " (id: " + context.currentIdentifier.uid + ") -> construct " + construct.type + " (id: " + construct.uid + ")" + " (uuid: " + construct.uuid + ")");
            }
        }
    }

    // describes the local context by default (or referenced construct by UUID)
    public void describeTask(Context context) {

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            List<Type> typeList = Type.get();
            for (int i = 0; i < typeList.size(); i++) {
                List<Reference> referenceList = Manager.get(Reference.class);
                // System.out.println("(id: " + typeList.get(i).uid + ") " + Application.ANSI_BLUE + typeList.get(i).identifier + Application.ANSI_RESET + " (" + constructList.size() + ") (uuid: " + typeList.get(i).uuid + ")");
                int typeReferenceCount = 0;
                for (int j = 0; j < referenceList.size(); j++) {
                    if (((Construct) (((Reference) referenceList.get(j)).object)).type == typeList.get(i)) {
                        typeReferenceCount++;
                    }
                }
                System.out.println(Color.ANSI_BLUE + typeList.get(i).identifier + Color.ANSI_RESET + " (count: " + typeReferenceCount + ")");
            }

        } else if (inputLineTokens.length >= 2) {

            String typeToken = inputLineTokens[1]; // "port" or "port (id: 3)"

            if (Expression.isConstruct(typeToken)) {

//                String typeIdentifierToken = typeToken.substring(0, typeToken.indexOf("(")).trim(); // text before '('
//                String addressTypeToken = typeToken.substring(typeToken.indexOf("(") + 1, typeToken.indexOf(":")).trim(); // text between '(' and ':'
//                String addressToken = typeToken.substring(typeToken.indexOf(":") + 1, typeToken.indexOf(")")).trim(); // text between ':' and ')'
//
//                if (addressTypeToken.equals("id")) {
//                    Construct construct = null;
//                    long uid = Long.parseLong(addressToken.trim());
//                    Identifier identifier = Manager.get(uid);
//                    if (identifier == null) {
//                        System.out.println(Color.ANSI_RED + "Error: No concept with UID " + uid + Color.ANSI_RESET);
//                    } else if (identifier.getClass() == Construct.class) {
//                        construct = (Construct) identifier;
////                        } else if (identifier.getClass() == Concept.class) {
////                            System.out.println("Error: The UID is for a concept.");
////                            //                                Concept concept = (Concept) identifier;
////                            //                                System.out.println("Found " + concept.types + " with UID " + uid);
//                    }
//
//                    if (construct != null && construct.type == Type.get(typeIdentifierToken)) {
//                        if (construct.type == Type.get("none")) {
//
//                            System.out.println("REFERENCE (id:X) -> " + construct);
//
//                        } else if (construct.type == Type.get("number")) {
//
//                        } else if (construct.type == Type.get("text")) {
//
////                            String feature = (String) construct.object;
//                            System.out.println("REFERENCE (id:X) -> " + construct);
//
//                        } else if (construct.type == Type.get("list")) {
//
//                        } else {
//
//                            System.out.println(Color.ANSI_BLUE + construct.type.identifier + Color.ANSI_RESET);
//
//                            HashMap<String, Feature> features = (HashMap<String, Feature>) construct.object;
//                            for (String featureIdentifier : features.keySet()) {
//                                Feature feature = features.get(featureIdentifier);
//                                String featureTypes = "";
//                                for (int i = 0; i < feature.types.size(); i++) {
//                                    featureTypes += feature.types.get(i);
//                                    if ((i + 1) < feature.types.size()) {
//                                        featureTypes += ", ";
//                                    }
//                                }
//                                System.out.println(Color.ANSI_GREEN + features.get(featureIdentifier).identifier + Color.ANSI_RESET + " " + Color.ANSI_BLUE + featureTypes + Color.ANSI_RESET);
//                                // TODO: print current object types; print available feature types
//                            }
//
//                        }
//                    }
//                } else if (addressToken.equals("uuid")) {
//
//                } else {
//
//
//                }

            } else if (Type.has(typeToken)) {
//                // TODO: Print Concept
//                System.out.println("VIEW CONCEPT");
//
//                System.out.println();

//                List<Construct> constructList = Manager.getConstructList(Type.get(typeToken));
                List<Reference> referenceList = Manager.get(Reference.class);
                for (int i = 0; i < referenceList.size(); i++) {
                    if (((Construct) (referenceList.get(i)).object).type == Type.get(typeToken)) {
                        System.out.println(referenceList.get(i).toColorString());
//                    System.out.println("(id: " + constructList.get(i).uid + ") " + Application.ANSI_GREEN + constructList.get(i).classType + Application.ANSI_RESET + " (uuid: " + constructList.get(i).uuid + ")");
                    }
                }
            }
        }
    }

    // Searches remote repository.
    //
    // Usage:
    // list [<types-identifier>]
    //
    // Examples:
    // list         Lists available types.
    // list port    Lists port constructs.
    // list path    Lists path constructs.
    public void searchTask(Context context) { // previously, viewTask

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            List<Type> typeList = Type.get();
            for (int i = 0; i < typeList.size(); i++) {
                List<Construct> constructList = Manager.getConstructList(typeList.get(i));
                // System.out.println("(id: " + typeList.get(i).uid + ") " + Application.ANSI_BLUE + typeList.get(i).identifier + Application.ANSI_RESET + " (" + constructList.size() + ") (uuid: " + typeList.get(i).uuid + ")");
                System.out.println(Color.ANSI_BLUE + typeList.get(i).identifier + Color.ANSI_RESET + " (count: " + constructList.size() + ")");
            }

        } else if (inputLineTokens.length >= 2) {

            String typeToken = inputLineTokens[1]; // "port" or "port (id: 3)"

            if (Expression.isConstruct(typeToken)) {

                String typeIdentifierToken = typeToken.substring(0, typeToken.indexOf("(")).trim(); // text before '('
                String addressTypeToken = typeToken.substring(typeToken.indexOf("(") + 1, typeToken.indexOf(":")).trim(); // text between '(' and ':'
                String addressToken = typeToken.substring(typeToken.indexOf(":") + 1, typeToken.indexOf(")")).trim(); // text between ':' and ')'

                if (addressTypeToken.equals("id")) {
                    Construct construct = null;
                    long uid = Long.parseLong(addressToken.trim());
                    Identifier identifier = Manager.get(uid);
                    if (identifier == null) {
                        System.out.println(Color.ANSI_RED + "Error: No concept with UID " + uid + Color.ANSI_RESET);
                    } else if (identifier.getClass() == Construct.class) {
                        construct = (Construct) identifier;
//                        } else if (identifier.getClass() == Concept.class) {
//                            System.out.println("Error: The UID is for a concept.");
//                            //                                Concept concept = (Concept) identifier;
//                            //                                System.out.println("Found " + concept.types + " with UID " + uid);
                    }

                    if (construct != null && construct.type == Type.get(typeIdentifierToken)) {
                        if (construct.type == Type.get("none")) {

                            System.out.println("REFERENCE (id:X) -> " + construct);

                        } else if (construct.type == Type.get("number")) {

                        } else if (construct.type == Type.get("text")) {

//                            String feature = (String) construct.object;
                            System.out.println("REFERENCE (id:X) -> " + construct);

                        } else if (construct.type == Type.get("list")) {

                        } else {

                            System.out.println(Color.ANSI_BLUE + construct.type.identifier + Color.ANSI_RESET);

                            HashMap<String, Feature> features = (HashMap<String, Feature>) construct.object;
                            for (String featureIdentifier : features.keySet()) {
                                Feature feature = features.get(featureIdentifier);
                                String featureTypes = "";
                                for (int i = 0; i < feature.types.size(); i++) {
                                    featureTypes += feature.types.get(i);
                                    if ((i + 1) < feature.types.size()) {
                                        featureTypes += ", ";
                                    }
                                }
                                System.out.println(Color.ANSI_GREEN + features.get(featureIdentifier).identifier + Color.ANSI_RESET + " " + Color.ANSI_BLUE + featureTypes + Color.ANSI_RESET);
                                // TODO: print current object types; print available feature types
                            }

                        }
                    }
                } else if (addressToken.equals("uuid")) {

                } else {

                }

            } else if (Type.has(typeToken)) {

                // TODO: Print Concept
//                System.out.println("VIEW CONCEPT");
//                System.out.println();

                List<Construct> constructList = Manager.getConstructList(Type.get(typeToken));
                for (int i = 0; i < constructList.size(); i++) {
                    System.out.println(constructList.get(i).toColorString());
//                    System.out.println("(id: " + constructList.get(i).uid + ") " + Application.ANSI_GREEN + constructList.get(i).classType + Application.ANSI_RESET + " (uuid: " + constructList.get(i).uuid + ")");
                }
            }
        }
    }

    // print <feature-identifier>
    // e.g., print mode
    public void printTask(Context context) {

        // Determine interpreter's object. Concept or instance?
        if (Context.isConstruct(context)) {

            String[] inputLineTokens = context.inputLine.split("[ ]+");

            // Defaults
            String featureToken = null;

            // Determine identifier
            if (inputLineTokens.length >= 2) {

                // Determine identifier
                featureToken = inputLineTokens[1];

                // TODO: if featureContentToken is instance UID/UUID, look it up and pass that into "set"

                Construct construct = Context.getConstruct(context).states.get(featureToken);

                System.out.println(construct);

//                if (state != null && state.types == Type.get("text")) {
////                    System.out.println("" + ((String) state.object));
//                    System.out.println("" + state);
//                } else if (state != null && state.types == Type.get("text")) {
//                    List contentList = (List) state.object;
//                    for (int i = 0; i < contentList.size(); i++) {
//                        // TODO: Possibly use Content object for values to pair types with object (like little "files with extensions")?
////                        if (contentList.get(i).types == Type.get("text")) {
////                            System.out.println("" + ((String) object.object));
////                        }
//                    }
//                }

//                if (object.types == Type.get("text")) {
//                    System.out.println("" + ((String) object.object));
//                } else if (object.types == Type.get("list")) {
//                    List contentList = (List) object;
//                    for (int i = 0; i < contentList.size(); i++) {
//                        // TODO: Possibly use Content object for values to pair types with object (like little "files with extensions")?
////                        if (contentList.get(i).types == Type.get("text")) {
////                            System.out.println("" + ((String) object.object));
////                        }
//                    }
//                }

//                Type types = null;
//                Concept identity = null;
//                if (Type.has(instanceTagToken)) {
//                    types = Type.get(instanceTagToken);
//                    if (Concept.has(types)) {
//                        identity = Concept.get(types);
//                    }
//                }

//                if (types != null && identity != null) {
//    //                    Construct instance = new Construct(identity);
//                    Construct instance = Construct.get(types);
//
//                    List<Construct> instanceList = Manager.get(Construct.class);
//                    System.out.println("added instance of identity " + instance.types + " (" + instanceList.size() + " instances)");
//                } else {
//                    System.out.println("Error: No types or identity matches '" + instanceTagToken + "'");
//                }
            }

            // Parse constraint
//            String letParameters = object.inputLine.substring(object.inputLine.indexOf(":") + 1);
//            String[] letParameterTokens = letParameters.split("[ ]*,[ ]*");

//            System.out.println("let parameters (" + letParameterTokens.length + "): " + letParameters);
//            for (int i = 0; i < letParameterTokens.length; i++) {
//                System.out.println("\t" + letParameterTokens[i].trim());
//            }

        } else {
            System.out.println(Color.ANSI_RED + "Error: Cannot set feature on concept." + Color.ANSI_RESET);
        }

    }

    // CUSTOM_CONSTRUCT CONTEXT:
    // let direction : 'none', 'input', 'output', 'bidirectional'
    // let current-construct : device, port, controller, task, script
    // let script : script
    //
    // CONSTRUCT CONTEXT:
    // let mode:digital;direction:input;voltage:cmos
    public void letTask(Context context) {

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        // Determine interpreter's object. Concept or instance?
        if (Context.isConcept(context)) {

            // Defaults
            String featureTagToken = null;

            // Determine identifier
            if (inputLineTokens.length >= 2) {
                featureTagToken = inputLineTokens[1];
            }

            // Parse constraint
            String letParameters = context.inputLine.substring(context.inputLine.indexOf(":") + 1);
            String[] letParameterTokens = letParameters.split("[ ]*,[ ]*");

            System.out.println("let parameters (" + letParameterTokens.length + "): " + letParameters);
            for (int i = 0; i < letParameterTokens.length; i++) {
                System.out.println("\t" + letParameterTokens[i].trim());
            }

//            // Determine types
//            if (inputLineTokens.length == 2) {
//                if (camp.computer.construct.Concept.has(featureTagToken)) {
//                    featureType = Feature.Type.CUSTOM_CONSTRUCT;
//                }
//            } else if (inputLineTokens.length == 3) {
//                String featureTypeToken = inputLineTokens[2];
//                if (featureTypeToken.equals("text")) {
//                    featureType = Feature.Type.TEXT;
//                } else if (featureTypeToken.equals("list")) {
//                    featureType = Feature.Type.LIST;
//                } else {
//                    if (camp.computer.construct.Concept.has(featureTagToken)) {
//                        featureType = Feature.Type.CUSTOM_CONSTRUCT;
//                    }
//                }
//            }
//
//            // Instantiate feature and print response
//            if (featureTagToken != null) {
//                Feature feature = new Feature(featureTagToken);
//                feature.types = featureType;
//                currentConcept.features.put(featureTagToken, feature);
//
//                // Print response
//                System.out.println("added feature '" + feature.identifier + "' of types '" + feature.types + "' (" + currentConcept.features.size() + ")");
//            } else {
//                // Print response
//                System.out.println("error: bad feature syntax");
//            }


        } else if (Context.isConstruct(context)) {

            // TODO:

        }
    }

    public void interpretLine_v1(String inputLine) {

        // <SANITIZE_INPUT>
        if (inputLine.contains("#")) {
            inputLine = inputLine.substring(0, inputLine.indexOf("#"));
        }

        inputLine = inputLine.trim();
        // </SANITIZE_INPUT>

        // <VALIDATE_INPUT>
        if (inputLine.length() == 0) {
            return;
        }
        // </VALIDATE_INPUT>

        if (workspace.operationConstruct != null && !inputLine.startsWith("stop")) {
            workspace.operationConstruct.operations.add(inputLine);
        } else {

            // Save line in history
            this.inputLines.add(inputLine);

            // Store object
            Context context = new Context();
            context.inputLine = inputLine;

            if (context.inputLine.startsWith("import file")) {
                importFileTask(context);
            } else if (context.inputLine.startsWith("start")) {
                startProcessTask(context);
            } else if (context.inputLine.startsWith("stop")) {
                stopProcessTask(context);
            } else if (context.inputLine.startsWith("do")) {
                doProcessTask(context);
            }

            // <VERSION_CONTROL>
            else if (context.inputLine.startsWith("save")) {
                saveConstructVersion(context);
            } else if (context.inputLine.startsWith("restore")) {
                restoreConstructVersion(context);
            }
            // </VERSION_CONTROL>

            // <REFACTOR>
            else if (context.inputLine.startsWith("get configuration")) {
                addConfigurationTask(context);
            }
            // </REFACTOR>
            else if (context.inputLine.startsWith("new")) {
                createConstructTask(context);
            } else if (context.inputLine.startsWith("browse")) {
                browseConstructsTask(context);
            } else if (context.inputLine.startsWith("get")) {
                addConstructTask(context);
            } else if (context.inputLine.startsWith("list")) {
                listConstructsTask(context);
                /*
                listProjectsTask();
                listDevicesTask();
                listPortsTask(object);
                listPathsTask();
                */
            } else if (context.inputLine.startsWith("view")) {
                describeWorkspaceTask(context);
            } else if (context.inputLine.startsWith("describe")) {
                describeConstructTask(context);
            } else if (context.inputLine.startsWith("edit")) {
                editConstructTask(context);
            } else if (context.inputLine.startsWith("remove")) {
                removeConstructTask(context);
            }
            // <REFACTOR>
            else if (context.inputLine.startsWith("set configuration")) {
                setConfigurationTask(context);
            } else if (context.inputLine.startsWith("set path configuration")) {
                setPathConfigurationTask(context);
            }
            // </REFACTOR>
            else if (context.inputLine.startsWith("set")) {
                setConstructVariable(context);
            } else if (context.inputLine.startsWith("solve")) {
                solvePathConfigurationTask(context);
            } else if (context.inputLine.startsWith("exit")) {
                exitTask(context);
            }

        }

    }

    // <REFACTOR>
    // TODO: Create "Command" class with command (1) keywords and (2) task to handle command.

    public void importFileTask(Context context) {
        // TODO: Change argument to "Context object" (temporary cache/manager)

        // TODO: Lookup object.clone("inputLine")
        String[] inputLineTokens = context.inputLine.split("[ ]+");

        String inputFilePath = inputLineTokens[2];

        new LoadBuildFileTask().execute(inputFilePath);

    }

    public void startProcessTask(Context context) {
        // TODO: Lookup object.clone("inputLine")
        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            workspace.operationConstruct = new OperationConstruct();

        } else if (inputLineTokens.length > 1) {

            String address = inputLineTokens[1];
//            if (address.startsWith("\"") && address.endsWith("\"")) {

//            String identifier = address.substring(1, address.length() - 1);
            String title = String.valueOf(address);

            workspace.operationConstruct = new OperationConstruct();
            workspace.operationConstruct.title = title;

//            }

        }

//        System.out.println("✔ edit project " + workspace.projectConstruct.uid);
//        System.out.println("> start " + workspace.operationConstruct.uid);
    }

    public void stopProcessTask(Context context) {

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            operationConstructs.add(workspace.operationConstruct);

//            System.out.println("✔ stop " + workspace.operationConstruct.uid + " (" + workspace.operationConstruct.operations.size() + " operations)");

            // Reset process construct
            workspace.operationConstruct = null;

        }

    }

    public void doProcessTask(Context context) {

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 2) {

            OperationConstruct operationConstruct = (OperationConstruct) Manager_v1.get(inputLineTokens[1]);

//            System.out.println("> do " + operationConstruct.uid);

            for (int i = 0; i < operationConstruct.operations.size(); i++) {
                // TODO: Add to "command buffer"
                interpretLine(operationConstruct.operations.get(i));
            }

        }

//        System.out.println("✔ stop " + workspace.operationConstruct.uid + " (" + workspace.operationConstruct.operations.size() + " operations)");

    }

    // push
    public void saveConstructVersion(Context context) {

        // TODO: Save new snapshot as a child/successor of the current construct version

        System.out.println("✔ save (revision XXX)");

    }

    // checkout
    public void restoreConstructVersion(Context context) {

        System.out.println("✔ restore (revision XXX)");

    }

    public void createConstructTask(Context context) {

        // TODO: Lookup object.clone("inputLine")
        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            // TODO: Add anonymous construct

        } else if (inputLineTokens.length == 2) {

            String constructTypeToken = inputLineTokens[1];

            if (constructTypeToken.equals("project")) {

                ProjectConstruct projectConstruct = new ProjectConstruct();
//                workspace.projectConstructs.get(projectConstruct);
//                workspace.lastProjectConstruct = projectConstruct; // Marketplace reference to last-created project

                System.out.println("✔ get project(uid:" + projectConstruct.uid + ") to workspace");

            } else if (constructTypeToken.equals("device")) {

//                // TODO: Ensure edit construct is a device!
//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == ProjectConstruct.class) {
//
//                    ProjectConstruct projectConstruct = (ProjectConstruct) workspace.OLD_construct;

                DeviceConstruct deviceConstruct = new DeviceConstruct();
//                projectConstruct.deviceConstructs.get(deviceConstruct);
//                workspace.lastDeviceConstruct = deviceConstruct; // Marketplace reference to last-created device

                System.out.println("✔ get device(uid:" + deviceConstruct.uid + ")");
//                }

            } else if (constructTypeToken.equals("port")) {

//                // TODO: Ensure edit OLD_construct is a device!
//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {
//
//                    DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;

                PortConstruct portConstruct = new PortConstruct();
//                    deviceConstruct.portConstructs.get(portConstruct);
//                    workspace.lastPortConstruct = portConstruct; // Marketplace reference to last-created port

                System.out.println("✔ get port(uid:" + portConstruct.uid + ")");
//                }

            } else if (constructTypeToken.equals("path")) {

//                // TODO: Ensure edit OLD_construct is a device!
//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == ProjectConstruct.class) {
//
//                    ProjectConstruct projectConstruct = (ProjectConstruct) workspace.OLD_construct;

                PathConstruct pathConstruct = new PathConstruct();
//                    projectConstruct.pathConstructs.get(pathConstruct);
//                    workspace.lastPathConstruct = pathConstruct; // Marketplace reference to last-created port
//
                System.out.println("✔ get path(uid:" + pathConstruct.uid + ")");
//                }

            } else if (constructTypeToken.equals("task")) {

//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {
//
//                    DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;

                TaskConstruct taskConstruct = new TaskConstruct();
//                    deviceConstruct.controllerConstruct.taskConstructs.get(taskConstruct);
//
//                    // Marketplace reference to last-created device
//                    workspace.lastTaskConstruct = taskConstruct;

                System.out.println("✔ get task " + taskConstruct.uid);

//                }

            }

//            System.out.println("✔ get " + constructTypeToken + " " + projectConstruct.uid);

        }
//        else if (inputLineTokens.length > 2) {
//
//            String constructTypeToken = inputLineTokens[1];
//            String constructTitleString = inputLineTokens[2];
//
//            if (constructTypeToken.equals("project")) {
//
//                ProjectConstruct projectConstruct = new ProjectConstruct();
//                projectConstruct.identifier = constructTitleString;
//                workspace.projectConstructs.get(projectConstruct);
//                workspace.lastProjectConstruct = projectConstruct; // Marketplace reference to last-created project
//
//            } else if (constructTypeToken.equals("device")) {
//
////                // TODO: Ensure edit OLD_construct is a project!
////                if (workspace.projectConstruct != null) {
////
////                    DeviceConstruct deviceConstruct = new DeviceConstruct();
////                    deviceConstruct.identifier = constructTitleString;
////                    workspace.projectConstruct.deviceConstructs.get(deviceConstruct);
////                    workspace.lastDeviceConstruct = deviceConstruct; // Marketplace reference to last-created port
////
////                    System.out.println("✔ get device " + deviceConstruct.uid);
////                }
//
//                // TODO: Ensure edit OLD_construct is a device!
//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == ProjectConstruct.class) {
//
//                    ProjectConstruct projectConstruct = (ProjectConstruct) workspace.OLD_construct;
//
//                    DeviceConstruct deviceConstruct = new DeviceConstruct();
//                    projectConstruct.deviceConstructs.get(deviceConstruct);
//                    workspace.lastDeviceConstruct = deviceConstruct; // Marketplace reference to last-created device
//
//                    deviceConstruct.identifier = constructTitleString;
//
//                    System.out.println("✔ get device " + deviceConstruct.uid + " to project " + projectConstruct.uid);
//                }
//
//            } else if (constructTypeToken.equals("port")) {
//
//                // TODO: Ensure edit OLD_construct is a device!
//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {
//
//                    DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;
//
//                    PortConstruct portConstruct = new PortConstruct();
//                    portConstruct.identifier = constructTitleString;
//                    deviceConstruct.portConstructs.get(portConstruct);
//                    workspace.lastPortConstruct = portConstruct; // Marketplace reference to last-created port
//
//                    System.out.println("✔ get port " + portConstruct.uid + " on device " + deviceConstruct.uid);
//                }
//
//            } else if (constructTypeToken.equals("path")) {
//
//                // TODO:
//
//            } else if (constructTypeToken.equals("task")) {
//
//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {
//
//                    DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;
//
//                    TaskConstruct taskConstruct = new TaskConstruct();
//                    taskConstruct.identifier = constructTitleString;
//                    deviceConstruct.controllerConstruct.taskConstructs.get(taskConstruct);
//
//                    // Marketplace reference to last-created device
//                    workspace.lastTaskConstruct = taskConstruct;
//
//                    System.out.println("✔ get task " + taskConstruct.uid + " to device " + deviceConstruct.uid);
//
//                }
//
//            }
//
////            System.out.println("✔ get " + constructTypeToken + " " + projectConstruct.uid);
//
//        }

    }

    public void browseConstructsTask(Context context) {

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            // TODO: List all constructs!

            // TODO: print "3 devices, 50 ports, 10 configurations, etc."

            for (Construct_v1 construct : Manager_v1.elements.values()) {
                System.out.println(construct.type + " (uuid:" + construct.uuid + ")");
            }

        } else if (inputLineTokens.length == 2) {

            String constructTypeToken = inputLineTokens[1];

            for (Construct_v1 construct : Manager_v1.elements.values()) {
                if (construct.type.equals(constructTypeToken)) {
                    // System.out.println("" + OLD_construct.uid + "\t" + OLD_construct.uuid.toString());
                    System.out.println("" + construct.uid);
                }
            }

        }
    }

    // Format:
    // get <OLD_construct-types-identifier> <OLD_construct-instance-identifier>
    //
    // Examples:
    // - get project
    // - get project "my-project"
    public void addConstructTask(Context context) {

        // TODO: Lookup object.clone("inputLine")
        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            // TODO: Add anonymous OLD_construct

        } else if (inputLineTokens.length == 2) {

            String constructTypeToken = inputLineTokens[1];

            if (constructTypeToken.equals("project")) {

                // TODO: Instantiate container copy of specified project (from Repository/DB)

                // get project uuid:<uuid>

                String constructIdentifierToken = inputLineTokens[2].split(":")[1];
                UUID constructUuid = UUID.fromString(constructIdentifierToken);
                Construct_v1 construct = Repository.clone(constructUuid); // TODO: Return a COPY/CLONE of the project
                // TODO: get the project to the workspace (so it can be deployed)

                ProjectConstruct projectConstruct = new ProjectConstruct();
                workspace.projectConstructs.add(projectConstruct);
                workspace.lastProjectConstruct = projectConstruct; // Marketplace reference to last-created project

                System.out.println("✔ new project(uid:" + projectConstruct.uid + ") to workspace");

            } else if (constructTypeToken.equals("device")) {

                // TODO: Ensure edit OLD_construct is a device!
                if (workspace.construct != null && workspace.construct.getClass() == ProjectConstruct.class) {

                    ProjectConstruct projectConstruct = (ProjectConstruct) workspace.construct;

                    DeviceConstruct deviceConstruct = new DeviceConstruct();
                    projectConstruct.deviceConstructs.add(deviceConstruct);
                    workspace.lastDeviceConstruct = deviceConstruct; // Marketplace reference to last-created device

                    System.out.println("✔ new device(uid:" + deviceConstruct.uid + ") to project(uid:" + projectConstruct.uid + ")");
                }

            } else if (constructTypeToken.equals("port")) {

                // TODO: Ensure edit OLD_construct is a device!
                if (workspace.construct != null && workspace.construct.getClass() == DeviceConstruct.class) {

                    DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.construct;

                    PortConstruct portConstruct = new PortConstruct();
                    deviceConstruct.portConstructs.add(portConstruct);
                    workspace.lastPortConstruct = portConstruct; // Marketplace reference to last-created port

                    System.out.println("✔ new port(uid:" + portConstruct.uid + ") to device(uid:" + deviceConstruct.uid + ")");
                }

            } else if (constructTypeToken.equals("path")) {

                // TODO: Ensure edit OLD_construct is a device!
                if (workspace.construct != null && workspace.construct.getClass() == ProjectConstruct.class) {

                    ProjectConstruct projectConstruct = (ProjectConstruct) workspace.construct;

                    PathConstruct pathConstruct = new PathConstruct();
                    projectConstruct.pathConstructs.add(pathConstruct);
                    workspace.lastPathConstruct = pathConstruct; // Marketplace reference to last-created port

                    System.out.println("✔ new path(uid:" + pathConstruct.uid + ") to project (uid:" + projectConstruct.uid + ")");
                }

            } else if (constructTypeToken.equals("task")) {

                if (workspace.construct != null && workspace.construct.getClass() == DeviceConstruct.class) {

                    DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.construct;

                    TaskConstruct taskConstruct = new TaskConstruct();
                    deviceConstruct.controllerConstruct.taskConstructs.add(taskConstruct);

                    // Marketplace reference to last-created device
                    workspace.lastTaskConstruct = taskConstruct;

                    System.out.println("✔ new task " + taskConstruct.uid + " to device " + deviceConstruct.uid);

                }

            }

//            System.out.println("✔ get " + constructTypeToken + " " + projectConstruct.uid);

        } else if (inputLineTokens.length > 2) {

            String constructTypeToken = inputLineTokens[1];
            String constructTitleString = inputLineTokens[2];

            if (constructTypeToken.equals("project")) {

                ProjectConstruct projectConstruct = new ProjectConstruct();
                projectConstruct.title = constructTitleString;
                workspace.projectConstructs.add(projectConstruct);
                workspace.lastProjectConstruct = projectConstruct; // Marketplace reference to last-created project

            } else if (constructTypeToken.equals("device")) {

//                // TODO: Ensure edit OLD_construct is a project!
//                if (workspace.projectConstruct != null) {
//
//                    DeviceConstruct deviceConstruct = new DeviceConstruct();
//                    deviceConstruct.identifier = constructTitleString;
//                    workspace.projectConstruct.deviceConstructs.get(deviceConstruct);
//                    workspace.lastDeviceConstruct = deviceConstruct; // Marketplace reference to last-created port
//
//                    System.out.println("✔ get device " + deviceConstruct.uid);
//                }

                // TODO: Ensure edit OLD_construct is a device!
                if (workspace.construct != null && workspace.construct.getClass() == ProjectConstruct.class) {

                    ProjectConstruct projectConstruct = (ProjectConstruct) workspace.construct;

                    DeviceConstruct deviceConstruct = new DeviceConstruct();
                    projectConstruct.deviceConstructs.add(deviceConstruct);
                    workspace.lastDeviceConstruct = deviceConstruct; // Marketplace reference to last-created device

                    deviceConstruct.title = constructTitleString;

                    System.out.println("✔ new device " + deviceConstruct.uid + " to project " + projectConstruct.uid);
                }

            } else if (constructTypeToken.equals("port")) {

                // TODO: Ensure edit OLD_construct is a device!
                if (workspace.construct != null && workspace.construct.getClass() == DeviceConstruct.class) {

                    DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.construct;

                    PortConstruct portConstruct = new PortConstruct();
                    portConstruct.title = constructTitleString;
                    deviceConstruct.portConstructs.add(portConstruct);
                    workspace.lastPortConstruct = portConstruct; // Marketplace reference to last-created port

                    System.out.println("✔ new port " + portConstruct.uid + " on device " + deviceConstruct.uid);
                }

            } else if (constructTypeToken.equals("path")) {

                // TODO:

            } else if (constructTypeToken.equals("task")) {

                if (workspace.construct != null && workspace.construct.getClass() == DeviceConstruct.class) {

                    DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.construct;

                    TaskConstruct taskConstruct = new TaskConstruct();
                    taskConstruct.title = constructTitleString;
                    deviceConstruct.controllerConstruct.taskConstructs.add(taskConstruct);

                    // Marketplace reference to last-created device
                    workspace.lastTaskConstruct = taskConstruct;

                    System.out.println("✔ new task " + taskConstruct.uid + " to device " + deviceConstruct.uid);

                }

            }

//            System.out.println("✔ get " + constructTypeToken + " " + projectConstruct.uid);

        }

    }

    /**
     * <strong>Examples</strong>
     * {@code list <OLD_construct-types>}
     *
     * @param context
     */
    public void listConstructsTask(Context context) {

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            // TODO: List all constructs!

        } else if (inputLineTokens.length == 2) {

            String constructTypeToken = inputLineTokens[1];

            for (Construct_v1 construct : Manager_v1.elements.values()) {
                if (construct.type.equals(constructTypeToken)) {
                    // System.out.println("" + OLD_construct.uid + "\t" + OLD_construct.uuid.toString());
                    System.out.println("" + construct.uid);
                }

                // <REFACTOR>
                if (construct.getClass() == DeviceConstruct.class) {
                    List<PortConstruct> unassignedPorts = DeviceConstruct.getUnassignedPorts((DeviceConstruct) construct);
                    System.out.print("Unassigned: ");
                    for (int j = 0; j < unassignedPorts.size(); j++) {
                        System.out.print("" + unassignedPorts.get(j).uid + " ");
                    }
                    System.out.println();
                }
                // </REFACTOR>
            }

        }
    }

    public void describeConstructTask(Context context) {

        // describe
        // describe path
        // describe port
        // describe uid(34)
        // describe uuid(35)
        // describe path(...)

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            // TODO: List all constructs!

            Construct_v1 construct = workspace.construct;

            String constructTypeToken = null;
            if (construct.getClass() == ProjectConstruct.class) {
                constructTypeToken = "project";
            } else if (construct.getClass() == DeviceConstruct.class) {
                constructTypeToken = "device";
            } else if (construct.getClass() == PortConstruct.class) {
                constructTypeToken = "port";
            } else if (construct.getClass() == PathConstruct.class) {
                constructTypeToken = "path";
            } else if (construct.getClass() == ControllerConstruct.class) {
                constructTypeToken = "controller";
            } else if (construct.getClass() == TaskConstruct.class) {
                constructTypeToken = "task";
            } else if (construct.getClass() == ScriptConstruct.class) {
                constructTypeToken = "script";
            }

            System.out.println("> " + constructTypeToken + " (uid:" + construct.uid + ")");

        } else if (inputLineTokens.length == 2) {

            String constructAddressString = inputLineTokens[1];

            Construct_v1 construct = Manager_v1.get(constructAddressString);

            String constructTypeToken = null;
            if (construct.getClass() == ProjectConstruct.class) {
                constructTypeToken = "project";
            } else if (construct.getClass() == DeviceConstruct.class) {
                constructTypeToken = "device";
            } else if (construct.getClass() == PortConstruct.class) {
                constructTypeToken = "port";
            } else if (construct.getClass() == PathConstruct.class) {
                constructTypeToken = "path";
            } else if (construct.getClass() == ControllerConstruct.class) {
                constructTypeToken = "controller";
            } else if (construct.getClass() == TaskConstruct.class) {
                constructTypeToken = "task";
            } else if (construct.getClass() == ScriptConstruct.class) {
                constructTypeToken = "script";
            }

            System.out.println("> " + constructTypeToken + " (uid:" + construct.uid + ")");

        }
    }

    public void describeWorkspaceTask(Context context) {

        // describe
        // describe path
        // describe port
        // describe uid(34)
        // describe uuid(35)
        // describe path(...)

//        String[] inputLineTokens = object.inputLine.split("[ ]+");
//
//        if (inputLineTokens.length == 1) {
//
//            // TODO: List all constructs!
//
//            Concept OLD_construct = workspace.OLD_construct;
//
//            String constructTypeToken = null;
//            if (OLD_construct.getClass() == ProjectConstruct.class) {
//                constructTypeToken = "project";
//            } else if (OLD_construct.getClass() == DeviceConstruct.class) {
//                constructTypeToken = "device";
//            } else if (OLD_construct.getClass() == PortConstruct.class) {
//                constructTypeToken = "port";
//            } else if (OLD_construct.getClass() == PathConstruct.class) {
//                constructTypeToken = "path";
//            } else if (OLD_construct.getClass() == ControllerConstruct.class) {
//                constructTypeToken = "controller";
//            } else if (OLD_construct.getClass() == TaskConstruct.class) {
//                constructTypeToken = "task";
//            } else if (OLD_construct.getClass() == ScriptConstruct.class) {
//                constructTypeToken = "script";
//            }
//
//            System.out.println("> " + constructTypeToken + " (uid:" + OLD_construct.uid + ")");
//
//        } else if (inputLineTokens.length == 2) {
//
//            String constructAddressString = inputLineTokens[1];
//
//            Concept OLD_construct = Manager_v1.clone(constructAddressString);
//
//            String constructTypeToken = null;
//            if (OLD_construct.getClass() == ProjectConstruct.class) {
//                constructTypeToken = "project";
//            } else if (OLD_construct.getClass() == DeviceConstruct.class) {
//                constructTypeToken = "device";
//            } else if (OLD_construct.getClass() == PortConstruct.class) {
//                constructTypeToken = "port";
//            } else if (OLD_construct.getClass() == PathConstruct.class) {
//                constructTypeToken = "path";
//            } else if (OLD_construct.getClass() == ControllerConstruct.class) {
//                constructTypeToken = "controller";
//            } else if (OLD_construct.getClass() == TaskConstruct.class) {
//                constructTypeToken = "task";
//            } else if (OLD_construct.getClass() == ScriptConstruct.class) {
//                constructTypeToken = "script";
//            }
//
//            System.out.println("> " + constructTypeToken + " (uid:" + OLD_construct.uid + ")");
//
//        }

        System.out.print("workspace (USERNAME)");
        System.out.println();

        for (int projectIndex = 0; projectIndex < workspace.projectConstructs.size(); projectIndex++) {
            ProjectConstruct projectConstruct = workspace.projectConstructs.get(projectIndex);

            System.out.print("\tproject");
            System.out.print(" (uid:" + projectConstruct.uid + ")");
            System.out.println();

            for (int deviceIndex = 0; deviceIndex < projectConstruct.deviceConstructs.size(); deviceIndex++) {
                DeviceConstruct deviceConstruct = projectConstruct.deviceConstructs.get(deviceIndex);

                System.out.print("\t\tdevice");
                System.out.print(" (uid:" + deviceConstruct.uid + ")");
                System.out.println();

                for (int portIndex = 0; portIndex < deviceConstruct.portConstructs.size(); portIndex++) {
                    PortConstruct portConstruct = deviceConstruct.portConstructs.get(portIndex);

                    //System.out.print("\t\t\tport" + " (" + portConstruct.configurations.size() + " configurations)");
                    System.out.print("\t\t\tport");
                    System.out.print(" (uid:" + portConstruct.uid + ")");
                    System.out.println();

                    for (int configurationIndex = 0; configurationIndex < portConstruct.configurations.size(); configurationIndex++) {
                        Configuration configuration = portConstruct.configurations.get(configurationIndex);

                        System.out.println("\t\t\t\tconfiguration (uid:???)");
                    }
                }
            }
        }
    }

    public void editConstructTask(Context context) {
        // TODO: Lookup object.clone("inputLine")
        String[] inputLineTokens = context.inputLine.split("[ ]+");

        Construct_v1 construct = null;

        if (inputLineTokens.length == 2) {

            String constructTypeToken = inputLineTokens[1];

            if (constructTypeToken.equals("project")) {
                construct = workspace.lastProjectConstruct;
            } else if (constructTypeToken.equals("device")) {
                construct = workspace.lastDeviceConstruct;
            } else if (constructTypeToken.equals("port")) {
                construct = workspace.lastPortConstruct;
            } else if (constructTypeToken.equals("path")) {
                construct = workspace.lastPathConstruct;
            } else if (constructTypeToken.equals("controller")) {
                construct = workspace.lastControllerConstruct;
            } else if (constructTypeToken.equals("task")) {
                construct = workspace.lastTaskConstruct;
            }

        } else if (inputLineTokens.length > 2) {

            construct = Manager_v1.get(inputLineTokens[2]);

        }

        if (construct != null) {

            workspace.construct = construct;
//            System.out.println("✔ edit " + workspace.OLD_construct.uid);
//            System.out.println("✔ edit " + constructTypeToken + " " + workspace.OLD_construct.uid);

        } else {

            // No port was found with the specified identifier (UID, UUID, identifier, index)

        }
    }

    /**
     * Removes the {@code Concept} with the specified identifier from the {@code Manager_v1}.
     *
     * @param context
     */
    public void removeConstructTask(Context context) {

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            // TODO: List all constructs!

        } else if (inputLineTokens.length == 2) {

            String addressString = inputLineTokens[1];

            Construct_v1 construct = Manager_v1.get(addressString);

            if (construct != null) {
                Manager_v1.remove(construct.uid);
            }

        }
    }

//    public void editProjectTask(Context object) {
//
//        // TODO: Lookup object.clone("inputLine")
//        String[] inputLineTokens = object.inputLine.split("[ ]+");
//
//        Concept OLD_construct = null;
//
//        if (inputLineTokens.length == 2) {
//            OLD_construct = workspace.lastProjectConstruct;
//        } else if (inputLineTokens.length > 2) {
//            OLD_construct = Manager_v1.clone(inputLineTokens[2]);
//        }
//
//        if (OLD_construct != null) {
//            workspace.projectConstruct = (ProjectConstruct) OLD_construct;
//            System.out.println("✔ edit project " + workspace.projectConstruct.uid);
//        }
//
//    }
//
//    public void editDeviceTask(Context object) {
//
//        // TODO: Lookup object.clone("inputLine")
//        String[] inputLineTokens = object.inputLine.split("[ ]+");
//
//        Concept deviceConstruct = null;
//
//        if (inputLineTokens.length == 2) {
//            deviceConstruct = workspace.lastDeviceConstruct;
//        } else if (inputLineTokens.length > 2) {
//            deviceConstruct = Manager_v1.clone(inputLineTokens[2]);
//        }
//
//        if (deviceConstruct != null) {
//
//            workspace.OLD_construct = deviceConstruct;
//            System.out.println("✔ edit device " + deviceConstruct.uid);
//
//        } else {
//
//            // No port was found with the specified identifier (UID, UUID, identifier, index)
//
//        }
//
//    }
//
//    public void editPortTask(Context object) {
//
//        // TODO: Lookup object.clone("inputLine")
//        String[] inputLineTokens = object.inputLine.split("[ ]+");
//
//        Concept portConstruct = null;
//
//        if (inputLineTokens.length == 2) {
//            portConstruct = workspace.lastPortConstruct;
//        } else if (inputLineTokens.length > 2) {
//            portConstruct = Manager_v1.clone(inputLineTokens[2]);
//        }
//
//        if (portConstruct != null) {
//
//            workspace.OLD_construct = portConstruct;
//            System.out.println("✔ edit port " + workspace.OLD_construct.uid);
//
//        } else {
//
//            // No port was found with the specified identifier (UID, UUID, identifier, index)
//
//        }
//
//    }
//
//    public void editPathTask(Context object) {
//        // TODO: Lookup object.clone("inputLine")
//        String[] inputLineTokens = object.inputLine.split("[ ]+");
//
//        Concept pathConstruct = null;
//
//        if (inputLineTokens.length == 2) {
//            pathConstruct = workspace.lastPathConstruct;
//        } else if (inputLineTokens.length > 2) {
//            pathConstruct = Manager_v1.clone(inputLineTokens[2]);
//        }
//
//        if (pathConstruct != null) {
//
//            workspace.OLD_construct = pathConstruct;
//            System.out.println("✔ edit path " + workspace.OLD_construct.uid);
//
//        } else {
//
//            // No port was found with the specified identifier (UID, UUID, identifier, index)
//
//        }
//    }
//
//    public void editTaskTask(Context object) {
//        // TODO: Change argument to "Context object" (temporary cache/manager)
//
//        // TODO: Lookup object.clone("inputLine")
//        String[] inputLineTokens = object.inputLine.split("[ ]+");
//
//        if (inputLineTokens.length == 2) {
//
//            Workspace.setConstruct(workspace, workspace.lastTaskConstruct);
//
//        } else if (inputLineTokens.length > 2) {
//
//        }
//
//        System.out.println("✔ edit task " + workspace.OLD_construct.uid);
//    }
//
//    public void setProjectTitleTask(Context object) {
//
//        // TODO: Lookup object.clone("inputLine")
//        if (workspace.projectConstruct != null) {
//
//            String[] inputLineTokens = object.inputLine.split("[ ]+");
//
//            String inputProjectTitle = inputLineTokens[3];
//
//            workspace.projectConstruct.identifier = inputProjectTitle;
//
//            System.out.println("project identifier changed to " + inputProjectTitle);
//        }
//
//    }

    // e.g., get configuration uart(tx);output;ttl,cmos
    public void addConfigurationTask(Context context) {

        // TODO: Parse "bus(line)" value string pattern to create bus and lines.

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        String configurationOptionString = inputLineTokens[2];

        String[] configurationVariableList = configurationOptionString.split(";");

        List<Pair<String, Tuple<String>>> variableValueSets = new ArrayList<>();

        PortConstruct portConstruct = null;
        if (workspace.construct != null && workspace.construct.getClass() == PortConstruct.class) {
            portConstruct = (PortConstruct) workspace.construct;
        }

        for (int i = 0; i < configurationVariableList.length; i++) {

            String[] configurationAssignmentList = configurationVariableList[i].split(":");
            String variableTitle = configurationAssignmentList[0];
            String variableValues = configurationAssignmentList[1];

            // <HACK>
            if (!portConstruct.variables.containsKey(variableTitle)) {
//                portConstruct.features.put(variableTitle, new Variable(variableTitle));
                portConstruct.variables.put(variableTitle, null);
            }
            // </HACK>

            String[] variableValueList = variableValues.split(",");

            // Save variable's value set for the configuration constraint
            Tuple<String> variableValueSet = new Tuple<>();
            for (int j = 0; j < variableValueList.length; j++) {
                variableValueSet.values.add(variableValueList[j]);
            }
            variableValueSets.add(new Pair<>(variableTitle, variableValueSet));

        }

        // Add VariableMap Option/Configuration
        portConstruct.configurations.add(new Configuration(variableValueSets));

    }

    // set configuration mode:digital;direction:output;voltage:ttl
    public void setConfigurationTask(Context context) {

//        // TODO: Change argument to "Context object" (temporary cache/manager)
//
//        // TODO: Lookup object.clone("inputLine")
//        String[] inputLineTokens = object.inputLine.split("[ ]+");
//
//        String configurationOptionString = inputLineTokens[2];
//
//        PortConfigurationConstraint.Mode mode = PortConfigurationConstraint.Mode.NONE;
//        PortConfigurationConstraint.Direction direction = null;
//        PortConfigurationConstraint.Voltage voltage = null;
//
//        // Separate configurations string into tokens separated by ";" substring, each an expression representing an
//        // attribute state assignment. Separate each attribute assignment by ":", into the attribute identifier and
//        // by ":" substring value.
//        String[] configurationOptionList = configurationOptionString.split(";");
//        for (int i = 0; i < configurationOptionList.length; i++) {
//
//            String[] configurationAttributeList = configurationOptionList[i].split(":");
//            String attributeTitle = configurationAttributeList[0];
//            String attributeValues = configurationAttributeList[1];
//
//            if (attributeTitle.equals("mode")) {
//
//                // Parses and caches the mode assignment.
//                if (attributeValues.equals("none")) {
//                    mode = PortConfigurationConstraint.Mode.NONE;
//                } else if (attributeValues.equals("digital")) {
//                    mode = PortConfigurationConstraint.Mode.DIGITAL;
//                } else if (attributeValues.equals("analog")) {
//                    mode = PortConfigurationConstraint.Mode.ANALOG;
//                } else if (attributeValues.equals("pwm")) {
//                    mode = PortConfigurationConstraint.Mode.PWM;
//                } else if (attributeValues.equals("resistive_touch")) {
//                    mode = PortConfigurationConstraint.Mode.RESISTIVE_TOUCH;
//                } else if (attributeValues.equals("power")) {
//                    mode = PortConfigurationConstraint.Mode.POWER;
//                } else if (attributeValues.equals("i2c(scl)")) {
//                    mode = PortConfigurationConstraint.Mode.I2C_SCL;
//                } else if (attributeValues.equals("i2c(sda)")) {
//                    mode = PortConfigurationConstraint.Mode.I2C_SDA;
//                } else if (attributeValues.equals("spi(sclk)")) {
//                    mode = PortConfigurationConstraint.Mode.SPI_SCLK;
//                } else if (attributeValues.equals("spi(mosi)")) {
//                    mode = PortConfigurationConstraint.Mode.SPI_MOSI;
//                } else if (attributeValues.equals("spi(miso)")) {
//                    mode = PortConfigurationConstraint.Mode.SPI_MISO;
//                } else if (attributeValues.equals("spi(ss)")) {
//                    mode = PortConfigurationConstraint.Mode.SPI_SS;
//                } else if (attributeValues.equals("uart(rx)")) {
//                    mode = PortConfigurationConstraint.Mode.UART_RX;
//                } else if (attributeValues.equals("uart(tx)")) {
//                    mode = PortConfigurationConstraint.Mode.UART_TX;
//                }
//
//            } else if (attributeTitle.equals("direction")) {
//
//                // Parses and caches the direction assignment.
//                if (attributeValues.equals("none")) {
//                    direction = PortConfigurationConstraint.Direction.NONE;
//                } else if (attributeValues.equals("input")) {
//                    direction = PortConfigurationConstraint.Direction.INPUT;
//                } else if (attributeValues.equals("output")) {
//                    direction = PortConfigurationConstraint.Direction.OUTPUT;
//                } else if (attributeValues.equals("bidirectional")) {
//                    direction = PortConfigurationConstraint.Direction.BIDIRECTIONAL;
//                }
//
//            } else if (attributeTitle.equals("voltage")) {
//
//                // Parses and caches the voltage assignment.
//                if (attributeValues.equals("none")) {
//                    voltage = PortConfigurationConstraint.Voltage.NONE;
//                } else if (attributeValues.equals("ttl")) {
//                    voltage = PortConfigurationConstraint.Voltage.TTL;
//                } else if (attributeValues.equals("cmos")) {
//                    voltage = PortConfigurationConstraint.Voltage.CMOS;
//                } else if (attributeValues.equals("common")) {
//                    voltage = PortConfigurationConstraint.Voltage.COMMON;
//                }
//
//            }
//
//        }
//
//        // TODO: check if specified configurations is valid
//
//        // Updates the port state.
//        workspace.portConstruct.mode = mode;
//        workspace.portConstruct.direction = direction;
//        workspace.portConstruct.voltage = voltage;
//
//        // TODO: Generalize so can set state of any OLD_construct/container. Don't assume port OLD_construct is only one with state.
//        System.out.println("✔ set port attributes to " + workspace.portConstruct.mode + " " + workspace.portConstruct.direction + " " + workspace.portConstruct.voltage);

    }

    /**
     * Given a project specification and a workspace, search unassigned ports on discovered and
     * virtual hosts for the port configuration dependencies of the project's extension device
     * requirements.
     */
    public void autoAssembleProjectWithWorkspace() {

    }

    /**
     * Given a device and a workspace, ...
     */
    public void autoAssembleDeviceWithWorkspace() {

    }

    /**
     * Given a device and a host, ...
     */
    public void autoAssembleDeviceWithHost() {

    }

    public void autoAssemblePortWithWorkspace() {

    }

    public void autoAssemblePortWithHost() {

    }

    public void autoAssemblePortWithPort() {

    }

    /**
     * Selects devices (and ports?) with unassigned ports that are compatible with the specified
     * path configuration.
     *
     * @param context
     */
    public void solveDeviceConfigurationTask(Context context) {

        // 1. Given two devices and a path, selects ports on respective paths that are compatible,
        //    if any.
        // 2. Given a path, selects the devices and then searches for a compatible port pairing
        //    (as in 1), that satisfied the path's dependencies.
        // 3. Same as 2, but for a set of paths.
        // 4. Same as 1, but for a set of paths.

    }

    /**
     * Given a path with containing two ports, determines compatible configurations (if any).
     * <p>
     * "solve <path-OLD_construct>"
     * e.g., solve uid(34)
     *
     * @param context
     */
    public void solvePathConfigurationTask(Context context) {

        // solve uid(34)
        // solve path <path-address>

        // get path <identifier>
        // edit path
        // set source-port[OLD_construct-types] uid:34
        // set target-port[OLD_construct-types] uid:34

//        if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {
//        if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == PathConstruct.class) {

//            DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;

        String[] inputLineTokens = context.inputLine.split("[ ]+");

        // TODO: Parse address token (for index, UID, UUID; identifier/key/identifier)

        PathConstruct pathConstruct = (PathConstruct) Manager_v1.get(inputLineTokens[1]);

        /**
         * "solve path [uid]"
         */

        // TODO: Resolve set of available configurations for path based on compatible configurations of ports in the path.

        // Iterate through configurations for of source port in path. For each source port configurations, check
        // the other ports' configurations for compatibility; then get each compatible configurations to a list of
        // compatible configurations.
        List<HashMap<String, Configuration>> pathConfigurations = new ArrayList<>();
        for (int i = 0; i < pathConstruct.sourcePortConstruct.configurations.size(); i++) {
            Configuration sourcePortConfiguration = pathConstruct.sourcePortConstruct.configurations.get(i);

            for (int j = 0; j < pathConstruct.targetPortConstruct.configurations.size(); j++) {
                Configuration targetPortConfiguration = pathConstruct.targetPortConstruct.configurations.get(j);

                // PATH SERIAL FORMAT:
                // ~ mode;direction;voltage + mode;direction;voltage
                //
                // ? mode;ports:uid,uid;voltage
                // ? source:uid;target:uid;mode;direction;voltage
                // > ports:uid,uid;mode;direction;voltage
                //
                // ? mode;direction;voltage&mode;direction;voltage
                // ? mode;direction;voltage+mode;direction;voltage
                // ? mode;direction;voltage|mode;direction;voltage

                List<Configuration> compatiblePortConfigurations = Configuration.computeCompatibleConfigurations(sourcePortConfiguration, targetPortConfiguration);

                if (compatiblePortConfigurations != null) {
                    HashMap<String, Configuration> pathConfiguration = new HashMap<>();
                    pathConfiguration.put("source-port", compatiblePortConfigurations.get(0));
                    pathConfiguration.put("target-port", compatiblePortConfigurations.get(1));
                    pathConfigurations.add(pathConfiguration);
                }

                // TODO: Pick up here. Configuration resolution isn't working, probably because of a logic bug in isCompatible(...)
            }
//                System.out.println();
        }

        // If there is only one path configurations in the compatible configurations list, automatically configure
        // the path with it, thereby updating the ports' configurations in the path.
        // TODO: ^
        if (pathConfigurations.size() == 1) {
            // Apply the corresponding configurations to ports.
            HashMap<String, Configuration> pathConfiguration = pathConfigurations.get(0);
            System.out.println("✔ found compatible configurations");

            // TODO: (QUESTION) Can I specify a path configurations and infer port configurations (for multi-port) or should it be a list of port configurations?
            // TODO: Apply values based on per-variable configurations?
            // TODO: Ensure there's only one compatible state for each of the configurations.

            // Source
            // TODO: print PORT ADDRESS
            System.out.print("  1. mode:" + pathConfiguration.get("source-port").variables.get("mode").values.get(0));
            System.out.print(";direction:");
            for (int k = 0; k < pathConfiguration.get("source-port").variables.get("direction").values.size(); k++) {
                System.out.print("" + pathConfiguration.get("source-port").variables.get("direction").values.get(k));
                if ((k + 1) < pathConfiguration.get("source-port").variables.get("direction").values.size()) {
                    System.out.print(", ");
                }
            }
            System.out.print(";voltage:");
            for (int k = 0; k < pathConfiguration.get("source-port").variables.get("voltage").values.size(); k++) {
                System.out.print("" + pathConfiguration.get("source-port").variables.get("voltage").values.get(k));
                if ((k + 1) < pathConfiguration.get("source-port").variables.get("voltage").values.size()) {
                    System.out.print(", ");
                }
            }
            System.out.print(" | ");

            // Target
            // TODO: print PORT ADDRESS
            System.out.print("mode:" + pathConfiguration.get("target-port").variables.get("mode").values.get(0));
            System.out.print(";direction:");
            for (int k = 0; k < pathConfiguration.get("target-port").variables.get("direction").values.size(); k++) {
                System.out.print("" + pathConfiguration.get("target-port").variables.get("direction").values.get(k));
                if ((k + 1) < pathConfiguration.get("target-port").variables.get("direction").values.size()) {
                    System.out.print(", ");
                }
            }
            System.out.print(";voltage:");
            for (int k = 0; k < pathConfiguration.get("target-port").variables.get("voltage").values.size(); k++) {
                System.out.print("" + pathConfiguration.get("target-port").variables.get("voltage").values.get(k));
                if ((k + 1) < pathConfiguration.get("target-port").variables.get("voltage").values.size()) {
                    System.out.print(", ");
                }
            }
            System.out.println();

            // Configure the ports with the single compatible configurations
            if (pathConfiguration.get("source-port").variables.get("mode").values.size() == 1) {
                if (pathConstruct.sourcePortConstruct.variables.containsKey("mode")
                        && pathConstruct.sourcePortConstruct.variables.get("mode") == null) {
                    pathConstruct.sourcePortConstruct.variables.put("mode", new Variable("mode"));
                }
                pathConstruct.sourcePortConstruct.variables.get("mode").value = pathConfiguration.get("source-port").variables.get("mode").values.get(0);
                System.out.println("  ✔ setting mode: " + pathConstruct.sourcePortConstruct.variables.get("mode").value);
            }

            if (pathConfiguration.get("source-port").variables.get("direction").values.size() == 1) {
                if (pathConstruct.sourcePortConstruct.variables.containsKey("direction")
                        && pathConstruct.sourcePortConstruct.variables.get("direction") == null) {
                    pathConstruct.sourcePortConstruct.variables.put("direction", new Variable("direction"));
                }
                pathConstruct.sourcePortConstruct.variables.get("direction").value = pathConfiguration.get("source-port").variables.get("direction").values.get(0);
                System.out.println("  ✔ setting direction: " + pathConstruct.sourcePortConstruct.variables.get("direction").value);
            }

            if (pathConfiguration.get("source-port").variables.get("voltage").values.size() == 1) {
                if (pathConstruct.sourcePortConstruct.variables.containsKey("voltage")
                        && pathConstruct.sourcePortConstruct.variables.get("voltage") == null) {
                    pathConstruct.sourcePortConstruct.variables.put("voltage", new Variable("voltage"));
                }
                pathConstruct.sourcePortConstruct.variables.get("voltage").value = pathConfiguration.get("source-port").variables.get("voltage").values.get(0);
                System.out.println("  ✔ setting voltages: " + pathConstruct.sourcePortConstruct.variables.get("voltage").value);
            }

        }

        // Otherwise, list the available path configurations and prompt the user to set one of them manually.
        else if (pathConfigurations.size() > 1) {
            // Apply the corresponding configurations to ports.
            System.out.println("✔ found compatible configurations");
            for (int i = 0; i < pathConfigurations.size(); i++) {
//                    PathConfiguration pathConfiguration = consistentPathConfigurations.clone(i);
//                    System.out.println("\t[" + i + "] (" + pathConstruct.sourcePortConstruct.uid + ", " + pathConstruct.targetPortConstruct.uid + "): (" + pathConfiguration.configurations.clone("source-port").mode + ", ...) --- (" + pathConfiguration.configurations.clone("target-port").mode + ", ...)");
            }
            System.out.println("! set one of these configurations");
        }
//        }

    }

    public void setConstructVariable(Context context) {

        // TODO: Lookup object.clone("inputLine")
        String[] inputLineTokens = context.inputLine.split("[ ]+");

        if (inputLineTokens.length == 1) {

            // TODO: Add anonymous OLD_construct

        } else if (inputLineTokens.length == 2) {

            String assignmentString = inputLineTokens[1];

            String[] assignmentTokens = assignmentString.split(":");

            String variableTitle = assignmentTokens[0];
            String variableValue = assignmentTokens[1];

            // <HACK>
            // Note: Hack to handle expressions with nested ":" like "set source-port:port(uid:6)"
            // TODO: Write custom parser to handle this! Ignore nested ":" in split.
            if (assignmentTokens.length == 3) {
                variableValue += ":" + assignmentTokens[2];
            }
            // </HACK>

            if (workspace.construct.getClass() == PathConstruct.class) {
//            if (constructTypeToken.equals("path")) {

                // set path source-port uid:4

                PathConstruct pathConstruct = (PathConstruct) workspace.construct;

                if (variableTitle.equals("source-port")) {

                    PortConstruct sourcePort = (PortConstruct) Manager_v1.get(variableValue);
                    pathConstruct.sourcePortConstruct = sourcePort;

//                    System.out.println(">>> set source-port " + variableValue);

                } else if (variableTitle.equals("target-port")) {

                    PortConstruct targetPort = (PortConstruct) Manager_v1.get(variableValue);
                    pathConstruct.targetPortConstruct = targetPort;

//                    System.out.println(">>> set target-port " + variableValue);

                }

            } else if (workspace.construct.getClass() == TaskConstruct.class) {

                TaskConstruct taskConstruct = (TaskConstruct) workspace.construct;

                if (variableTitle.equals("script")) {

                    ScriptConstruct scriptConstruct = new ScriptConstruct();
                    scriptConstruct.text = variableValue;

                    taskConstruct.scriptConstruct = scriptConstruct;

//                    System.out.println(">>> set script " + variableValue);

                }

            }

            System.out.println("✔ set script " + variableTitle + ":" + variableValue);

        }

    }

    public void setPathConfigurationTask(Context context) {
        // TODO: Change argument to "Context object" (temporary cache/manager)

        // TODO: Lookup object.clone("inputLine")
        String[] inputLineTokens = context.inputLine.split("[ ]+");

        String inputPathConfiguration = inputLineTokens[3];

        System.out.println("✔ set path configuration to \"" + inputPathConfiguration + "\"");

        // protocols:
        // - electronic, rf, none

        // electronic:
        // - voltage
    }

    public void exitTask(Context context) {
        System.exit(0);
    }
    // </REFACTOR>

//    public void addTaskTask(Context object) {
//
//        if (workspace.deviceConstruct != null) {
//
//            TaskConstruct taskConstruct = new TaskConstruct();
//            workspace.deviceConstruct.controllerConstruct.taskConstructs.get(taskConstruct);
//
//            // Marketplace reference to last-created device
//            workspace.lastTaskConstruct = taskConstruct;
//
//            System.out.println("✔ get task " + taskConstruct.uid + " to device " + workspace.deviceConstruct.uid);
//
//        }
//
//    }

    /*
    public void listProjectsTask() {

        if (workspace.projectConstructs.size() == 0) {
            System.out.println("none");
        } else {
            for (int i = 0; i < workspace.projectConstructs.size(); i++) {
                System.out.print("" + workspace.projectConstructs.clone(i).uid);

                if (workspace.projectConstructs.clone(i).deviceConstructs.size() > 0) {
                    System.out.print(" (" + workspace.projectConstructs.clone(i).deviceConstructs.size() + " devices, " + workspace.projectConstructs.clone(i).pathConstructs.size() + " paths)");
                }

                System.out.println();
            }
        }

    }

    public void listDevicesTask() {

        if (workspace.projectConstruct.deviceConstructs.size() == 0) {
            System.out.println("none");
        } else {
            for (int i = 0; i < workspace.projectConstruct.deviceConstructs.size(); i++) {
                System.out.print("" + workspace.projectConstruct.deviceConstructs.clone(i).uid);

                if (workspace.projectConstruct.deviceConstructs.clone(i).portConstructs.size() > 0) {
                    System.out.print(" (" + workspace.projectConstruct.deviceConstructs.clone(i).portConstructs.size() + " ports)");
                }

                System.out.println();
            }
        }

    }

    // list ports -configurations
    public void listPortsTask(Context object) {
        // TODO: Change argument to "Context object" (temporary cache/manager)

        // TODO: Lookup object.clone("inputLine")
        String[] inputLineTokens = object.inputLine.split("[ ]+");

        if (inputLineTokens.length == 2) {

            if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {

                DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;

                for (int i = 0; i < deviceConstruct.portConstructs.size(); i++) {

                    // Port UID
                    System.out.println("" + deviceConstruct.portConstructs.clone(i).uid);

                }

            }

        } else if (inputLineTokens.length > 2) {

            String modifiers = inputLineTokens[2];

            if (!modifiers.equals("-configurations")) {
                return;
            }

            if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {

                DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;

                for (int i = 0; i < deviceConstruct.portConstructs.size(); i++) {

                    // Port UID
                    System.out.println("" + deviceConstruct.portConstructs.clone(i).uid);

                    for (int j = 0; j < deviceConstruct.portConstructs.clone(i).configurations.size(); j++) {

                        int k = 0;
                        for (String variableTitle : deviceConstruct.portConstructs.clone(i).configurations.clone(j).features.keySet()) {

                            List<String> variableValueSet = deviceConstruct.portConstructs.clone(i).configurations.clone(j).features.clone(variableTitle).values;

                            for (int l = 0; l < variableValueSet.size(); l++) {
                                System.out.print("" + variableValueSet.clone(l));

                                if ((l + 1) < variableValueSet.size()) {
                                    System.out.print(", ");
                                }
                            }

                            if ((k + 1) < deviceConstruct.portConstructs.clone(i).configurations.clone(j).features.size()) {
                                System.out.print("; ");
                            }

                            k++;

                        }

                        System.out.println();

                    }

                }

            }

        }

    }

    public void listPathsTask() {

        if (workspace.projectConstruct != null) {

            for (int i = 0; i < workspace.projectConstruct.pathConstructs.size(); i++) {
                System.out.println("" + workspace.projectConstruct.pathConstructs.clone(i).uid + " (port " + workspace.projectConstruct.pathConstructs.clone(i).sourcePortConstruct.uid + ", port " + workspace.projectConstruct.pathConstructs.clone(i).targetPortConstruct.uid + ")");
            }

        }

    }
    */

}


// TODO: PORTS CAN BE "WIRELESS" AND SPREAD OUT ACROSS BOARDS JUST LIKE CLAY BOARDS CAN BE SPREAD OUT IN SPACE.
