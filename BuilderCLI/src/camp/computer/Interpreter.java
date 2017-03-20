package camp.computer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import camp.computer.construct.Address;
import camp.computer.construct.Structure;
import camp.computer.construct.Type;
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
import camp.computer.construct.Error;
import camp.computer.construct.Expression;
import camp.computer.construct.Feature;
import camp.computer.construct.Reference;
import camp.computer.data.format.configuration.Configuration;
import camp.computer.data.format.configuration.Variable;
import camp.computer.platform_infrastructure.LoadBuildFileTask;
import camp.computer.util.Pair;
import camp.computer.util.Tuple;
import camp.computer.util.console.Color;
import camp.computer.workspace.Manager_v1;
import camp.computer.workspace.Manager;

public class Interpreter {

    // TODO: Move list of Processes into Workspace
    private List<OperationConstruct> operationConstructs = new ArrayList<>(); // TODO: Define namespaces (or just use construct and set types from default "container" to "namespace", then request an interpreter for that types)!

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
//        TypeId.create("type"); // TypeId.add("type");
//        TypeId.create("none");
//        TypeId.create("number");
//        TypeId.create("text");
//        TypeId.create("list");

        Type.request("type"); // TypeId.add("type");
        Type.request("none");
        Type.request("number");
        Type.request("text");
        Type.request("list");
        // "any" isn't actually represented with a type, since it's a constraint, not a type. It's
        // encoded in the way Types and Structure are represented in memory.

        // Instantiate primitive concepts
        if (!Type.exists("none")) {
            Type noneType = Type.request("none");
        }

        /*
        if (!Type.exists(Type.request("number"))) {
            Type numberConcept = Type.request(Type.request("number"));
        }
        */

        if (!Type.exists("text")) {
            Type textType = Type.request("text");
        }

        if (!Type.exists("list")) {
            Type listType = Type.request("list");
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

            // Store context in history
            // Context context = new Context();
            if (context == null) {
                context = new Context();
            }
            Expression expression = Context.setExpression(context, inputLine);

            // Save line in history
            context.expressionTimeline.add(inputLine);

            if (context.expression.startsWith("import file")) {
                importFileTask(context);
//            } else if (context.expression.startsWith("define") || context.expression.startsWith("def")) { // "new", "describe"
            } else if (context.expression.startsWith("type")) { // "define", "generate", "type"
                defineTask(context);
            } else if (context.expression.startsWith("structure")) {
                constructTask(context); // "address" here is in the sense of "Hereafter, until otherwise specified, expressions address type or construct <X>."
            } else if (context.expression.startsWith("has")) {
                hasTask(context);
            } else if (context.expression.startsWith("let")) {
                letTask(context);
            } else if (context.expression.startsWith("set")) {
                setTask(context);
            } else if (context.expression.startsWith("add")) {
                addTask(context);
            } else if (context.expression.startsWith("remove") || context.expression.startsWith("rem") || context.expression.startsWith("rm")) {
                removeTask(context);
            } else if (context.expression.startsWith("list index")) { // previously: list-reference / list-ref
                listReferenceTask(context);
            } else if (context.expression.startsWith("list type")) { // list-type
                listTypeTask(context);
            } else if (context.expression.startsWith("list")) { // previously: ws, show, ls, locate
                listTask(context);
//                describeTask(context);
//            } else if (context.expression.startsWith("search")) {
            } else if (context.expression.startsWith("browse")) {
                searchTask(context);
            } else if (context.expression.startsWith("describe") || context.expression.startsWith("ds")) { // previously: list, index, inspect, view, ls, cite, db, browse
                describeTask(context);
            } else if (context.expression.startsWith("context")) { // previously: list, index, inspect, view, ls, cite, db, browse
                privateContextTask(context);
//            } else if (context.expression.startsWith("next")) {
//                // TODO: Allow if previous command was "search" and more items exist (or say no more exist)
//            } else if (context.expression.startsWith("previous")) {
//                // TODO: Allow if previous command was "search" and previous items exist (or say no more exist)
//            } else if (context.expression.startsWith("print")) {
//                printTask(context);
            } else if (context.expression.equals("exit")) {
                exitTask(context);
            } else {
                // TODO: Validate string as valid construct instance address.

                if (context.references != null && context.references.containsKey(context.expression.split("[ ]+")[0])) { // REFACTOR
                    // e.g., "foo" -> port.id.9

                    String referenceKey = context.expression.split("[ ]+")[0];
                    Reference reference = context.references.get(referenceKey);
                    Structure structure = (Structure) reference.object;

                    // Update object
//                    context.address = structure;
                    context.address = reference;

//                    System.out.println("Found " + structure.type + " structure (UID: " + structure.uid + ")");

                    System.out.println(Color.ANSI_YELLOW + referenceKey + Color.ANSI_RESET + " -> " + reference.toColorString());

                } else if (Type.exists(context.expression.split("[ ]+")[0])) { // REFACTOR

//                    constructTask(context); // "address" here is in the sense of "Hereafter, until otherwise specified, expressions address type or construct <X>."

                } else if (Expression.isConstruct(context.expression)) {

                    String[] tokens = context.expression.split("\\.");
                    String typeIdentifierToken = tokens[0];
                    String addressTypeToken = tokens[1];
                    String addressToken = tokens[2];

                    if (Type.exists(typeIdentifierToken)) {
                        if (addressTypeToken.equals("id")) {
                            long uid = Long.parseLong(addressToken);
                            Address address = Manager.get(uid);
                            if (address == null) {
                                System.out.println(Color.ANSI_RED + "Error: No type with UID " + uid + Color.ANSI_RESET);
                            } else if (address.getClass() == Reference.class) {
                                Reference reference = (Reference) address;
                                Structure structure = (Structure) reference.object;
//                                System.out.println("[FOUND] " + structure.type + ".id." + reference.uid + " -> " + structure.type + ".id." + structure.uid);
                                System.out.println("(link) " + reference.toColorString());

                                // Update object
//                                currentContextType = ContextType.CONSTRUCT;
                                context.address = reference;
                            } else if (address.getClass() == Structure.class) {
                                Structure structure = (Structure) address;
//                                System.out.println("[FOUND] " + structure.toColorString());
                                System.out.println("(structure) " + structure.toColorString());

                                // Update object
//                                currentContextType = ContextType.CONSTRUCT;
                                context.address = structure;

                            } else if (address.getClass() == Type.class) {
                                System.out.println(Color.ANSI_RED + "Error: The UID is for a type." + Color.ANSI_RESET);
//                                Type type = (Type) address;
//                                System.out.println("Found " + type.types + " with UID " + uid);
                            }
                        } else if (context.expression.contains("uuid:")) {

                        }
                    }
                } else {
                    System.out.println(Color.ANSI_RED + "Error: Unsupported expression." + Color.ANSI_RESET);
                }
            }
        }
    }

    public void defineTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            System.out.println("Usage: type <address>");

        } else if (inputLineTokens.length == 2) {

            String typeToken = inputLineTokens[1];

            if (!Type.exists(typeToken)) {
                Type.create(typeToken);
            }

            Type type = Type.request(typeToken);

            if (type == Type.request("type")
                    || type == Type.request("none")
                    || type == Type.request("text")
                    || type == Type.request("list")) {
                System.out.println(Error.get("Cannot change concepts of primitive type."));
                return;
            }

            if (!Type.exists(typeToken)) {
                Type.request(typeToken);
            }

            // System.out.println("Error: Structure already exists.");

//            Type type = null;
            if (context.conceptReferences.containsKey(type.identifier)) {
                type = context.conceptReferences.get(type.identifier);
            } else {
                type = Type.request(typeToken);
            }

            System.out.println(type.toColorString());

            // Update object
            context.address = type;

            // TODO: Factor this into a function in Context (to automate tracking of most-recent type)
            context.conceptReferences.put(typeToken, type);

        }
    }

    // Examples:
    // has voltage list : 'none', 'cmos', 'ttl'
    public void hasTask(Context context) {

        String[] inputLineSegments = context.expression.split("[ ]*:[ ]*");

        // Determine interpreter's object. Type or instance?
        if (Address.isType(context.address)) {

            // Defaults
            String featureIdentifier = null;
            List<Type> featureTypes = null; // new ArrayList<>(); // if size == 0, then unconstrained!
            List<Type> listTypes = null; // new ArrayList<>(); // if size == 0, then unconstrained!

            boolean hasError = false;

            // Determine address and types
            if (inputLineSegments.length >= 1) {

                String[] inputLineTokens = inputLineSegments[0].split("[ ]+");

                // Determine address
                featureIdentifier = inputLineTokens[1];

//                // <REFACTOR>
//                // Check if the feature already exists in the current object
//                if (Address.getType(context.address).features.containsKey(featureIdentifier)) {
//                    System.out.println(Color.ANSI_RED + "Warning: Context already contains feature '" + featureIdentifier + "'. A new construct revision will be generated." + Color.ANSI_RESET);
//                }
//                // </REFACTOR>

                // Determine types
                if (inputLineTokens.length >= 3) {
                    String featureTypeToken = inputLineTokens[2];

                    if (featureTypes == null) {
                        featureTypes = new ArrayList<>();
                    }

//                    if (featureTypeToken.equals("text")) {
//                        featureTypeIds.request(Type.request(featureTypeToken));
//                    } else
                    if (featureTypeToken.equals("list")) {
                        featureTypes.add(Type.request(featureTypeToken));
                        if (Type.exists(featureIdentifier)) {
                            if (listTypes == null) {
                                listTypes = new ArrayList<>();
                            }
                            listTypes.add(Type.request(featureIdentifier)); // If address is a construct types, then constraint list to that types by default
                        } else {
                            listTypes = null; // If address is non-construct types then default list types is "any"
                        }
                    } else {
                        // TODO: Refactor. There's some weird redundancy here with 'has' and 'Type.request'.
                        if (Type.exists(featureTypeToken)) {
                            featureTypes.add(Type.request(featureTypeToken));
                        }
                    }
                } else {
                    if (Type.exists(featureIdentifier)) {
//                    if (camp.computer.construct.Type.has(featureTagToken)) {
//                            // TODO: Replace with ConstructType for reserved construct types
//            System.out.println("(id: " + type.uid + ") " + Application.ANSI_BLUE + typeToken + Application.ANSI_RESET + " (uuid: " + type.uuid + ")");
                        if (featureTypes == null) {
                            featureTypes = new ArrayList<>();
                        }
                        featureTypes.add(Type.request(featureIdentifier));
                    } else {
                        // Can contain any types (no types is specified)
//                        featureTypeIds.request(Type.request("any")); // Default types
                    }
                }
            }

            // Determine constraints
            // TODO: Replace with counters for each of these possibilities!
            boolean hasContentConstraint = false;
            boolean isSingletonList = false;
            boolean isTextContent = false;
            boolean hasTextContent = false;
            boolean isConstructContent = false;
            boolean hasConstructContent = false;
            boolean hasDomainList = false;
            boolean hasInvalidConstruct = false;

            List<Structure> featureDomain = null; // = new ArrayList<>();
            if (inputLineSegments.length >= 2) {

                // Initialize parser
                hasContentConstraint = true;
                isTextContent = true;
                isConstructContent = true;

                String[] constraintTokens = inputLineSegments[1].split("[ ]*,[ ]*");

                // Determine types
                // Note: Start with ANY, but constrain with increasing generality based on any examples present.
                if (constraintTokens.length == 1) {
                    isSingletonList = true;
                }

                // Determine elements of object list
                for (int i = 0; i < constraintTokens.length; i++) {
                    String constraintToken = constraintTokens[i].trim();
                    if (!constraintToken.startsWith("'") || !constraintToken.endsWith("'")) { // TODO: Check for validity of type/construct/type
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
//                        featureTypeIds.remove(Type.request("any"));
                        // TODO: isListContent
                        if (isTextContent) {
                            featureTypes = new ArrayList<>();
                            featureTypes.add(Type.request("text"));
                            hasDomainList = true;
                        } else if (isConstructContent) {
                            // TODO: Use generic "construct" types or set specific if there's only one
                            if (isSingletonList) {
                                // e.g., has mode : list
                                // e.g., has source : port
                                featureTypes = new ArrayList<>();
                                featureTypes.add(Type.request(constraintTokens[0]));
                                // TODO: if 'text' or other construct only, then set types to featureType = TEXT and hasDomainList = false
                                if (featureTypes.contains(Type.request("list"))) {
//                                    listTypes.request(Type.request("any"));
                                }
                            } else {
                                // e.g., has my-feature : list, port
                                featureTypes = new ArrayList<>();
                                for (int i = 0; i < constraintTokens.length; i++) {
                                    featureTypes.add(Type.request(constraintTokens[i]));
                                }
                            }
                        } else if (hasTextContent && hasConstructContent) {
                            featureTypes = new ArrayList<>();
                            // Add 'text' list types
                            featureTypes.add(Type.request("text"));
                            // Add custom types to list types
                            for (int i = 0; i < constraintTokens.length; i++) {
                                // TODO: Check if types is valid!
                                if (Type.exists(constraintTokens[i])) {
                                    featureTypes.add(Type.request(constraintTokens[i]));
                                    // TODO: Check for non-existent Types (from tokens)
                                }
                            }
                            // TODO: Add text literals/examples to domain
                            hasDomainList = true;
                            // TODO: Test this... (e.g., with "has foo list : port, 'bar'")
                        }
                    } else if (featureTypes.contains(Type.request("text"))) {
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
                    } else if (featureTypes.contains(Type.request("list"))) {
                        // Remove types "any" so constrained types can be specified
//                        if (listTypes.contains(Type.request("any"))) {
//                            listTypes.remove(Type.request("any"));
//                        }
                        if (isTextContent) {
                            // e.g., has mode list : 'none', 'input', 'output', 'bidirectional'
                            if (listTypes == null) {
                                listTypes = new ArrayList<>();
                            }
                            listTypes.add(Type.request("text"));
                            hasDomainList = true;
                        } else if (isConstructContent) {
                            // e.g., has ports list : port
                            // e.g., has ports-and-paths list : port, path
                            if (isSingletonList) {
                                // e.g., has ports list : port
                                if (!Type.exists(constraintTokens[0])) {
                                    // Error: Invalid list object types.
                                    hasError = true;
                                } else {
//                                    // Remove "any" time so types constraint functions as expected
//                                    if (listTypes.contains(Type.request("any"))) {
//                                        listTypes.remove(Type.request("any"));
//                                    }
                                    if (listTypes == null) {
                                        listTypes = new ArrayList<>();
                                    }
                                    // Add the list types constraint
                                    listTypes.add(Type.request(constraintTokens[0]));
                                }
                            } else {
                                // e.g., has ports-and-paths list : port, path
                                // TODO: Convert listType to list and request all listed construct types to the types list
//                                listTypes.request(Type.request("construct"));
                                if (listTypes == null) {
                                    listTypes = new ArrayList<>();
                                }
                                for (int i = 0; i < constraintTokens.length; i++) {
                                    listTypes.add(Type.request(constraintTokens[i]));
                                    // TODO: Check for non-existent Types (from tokens)
                                }
                                hasDomainList = true;
                            }
                        } else if (hasTextContent && hasConstructContent) {
//                            listTypes.request(Type.request("any"));
//                            hasDomainList = true;
                            if (listTypes == null) {
                                listTypes = new ArrayList<>();
                            }
                            // Add 'text' list types
                            listTypes.add(Type.request("text"));
                            // Add custom types to list types
                            for (int i = 0; i < constraintTokens.length; i++) {
                                // TODO: Check if types is valid!
                                if (Type.exists(constraintTokens[i])) {
                                    listTypes.add(Type.request(constraintTokens[i]));
                                    // TODO: Check for non-existent Types (from tokens)
                                }
                            }
                            // TODO: Add text literals/examples to domain
                            hasDomainList = true;

                        }
//                    } else if (featureTypeIds.contains(Type.request("construct"))) {
//                        // TODO: Check if the specific construct presently assigned to featureType matches the list (should be identical)
//                        if (!isConstructContent) {
//                            hasError = true;
//                        } else {
//                            for (int i = 0; i < constraintTokens.length; i++) {
//                                String constraintToken = constraintTokens[i].trim();
//                                // TODO: Verify this... it might be a bug...
//                                if (!constraintToken.equals(featureTagToken)) { // NOTE: featureTagToken is the custom types address.
//                                    hasError = true;
//                                }
//                            }
//                        }
                    } else {
                        // Custom non-primitive construct types
                        // e.g., has source port : port(uid:3), port(uid:4), port(uid:5)
                        // TODO: Make sure that the constraint list contains feature object of the correct types (as in the above example)
                        for (int i = 0; i < constraintTokens.length; i++) {
                            String constraintToken = constraintTokens[i];
                            for (int j = 0; j < featureTypes.size(); j++) {
                                if (!constraintToken.equals(featureTypes.get(j).identifier)) { // NOTE: featureTagToken is the custom types address.
                                    hasError = true;
                                }
                            }
                        }
                    }
                    // TODO: NONE, ANY
                    // TODO: Support listType = Feature.TypeId.LIST
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
                            if (featureDomain == null) {
                                featureDomain = new ArrayList<>();
                            }
                            Structure state = Structure.request(constraintToken.trim());
                            featureDomain.add(state);
                        }
                    }
                }

            }

            // Instantiate feature, request to construct, and print response
            if (hasError) {
                System.out.println(Color.ANSI_RED + "Error: Conflicting types present in expression." + Color.ANSI_RESET);
            } else if (featureIdentifier != null) {
                // Store feature. Allocates memory for and stores feature.
                Feature feature = Feature.request(featureIdentifier, featureTypes, featureDomain, listTypes);
                /*
                Feature feature = new Feature(featureIdentifier);
                if (featureTypeIds != null) {
                    if (feature.types == null) {
                        feature.types = new ArrayList<>();
                    }
                    feature.types.addAll(featureTypeIds);
                    if (feature.types.contains(Type.request("list"))) {
                        if (listTypes != null) {
                            if (feature.listTypes == null) {
                                feature.listTypes = new ArrayList<>();
                            }
                            feature.listTypes.addAll(listTypes);
                        }
                    }
                }
                if (hasDomainList) {
                    // TODO: DEBUG! feature.domain should be null for ANY and empty for none
                    if (feature.domain == null) {
                        feature.domain = new ArrayList<>();
                    }
                    feature.domain.addAll(featureDomain);
                }
                */
                // TODO: Create new version of type here if feature is changed?

                Type baseType = Address.getType(context.address);
//                Type baseType = Address.getType(context.conceptReferences.request(featureIdentifier));
                Type replacementType = Type.request(baseType, featureIdentifier, feature);
                context.address = replacementType;

                // TODO: Factor this into a function in Context (to automate tracking of most-recent type)
                context.conceptReferences.put(replacementType.identifier, replacementType);

                if (baseType != replacementType) {
                    Application.log.log(baseType.identifier + " -> " + baseType);
//                    System.out.print(Color.ANSI_GREEN);
//                    System.out.println("\t" + baseType.type + " -> " + baseType);
//                    System.out.print(Color.ANSI_RESET);
                }
//                System.out.println("\tbaseType.id: " + baseType.uid);
//                System.out.println("\treplacementType.id: " + replacementType.uid);

//                Address.getType(context.address).features.put(featureIdentifier, feature);
//                long uid = Manager.add(feature);
//                // TODO: initialize "text" with default empty string construct reference (and other types accordingly)

                // Print response
                String typeString = "";
                if (feature.types != null) {
                    for (int i = 0; i < feature.types.size(); i++) {
                        typeString += "" + feature.types.get(i).toColorString();
                        if ((i + 1) < feature.types.size()) {
                            typeString += ", ";
                        }
                    }
                } else {
                    typeString = "any";
                }

                if (feature.types == null) {
                    System.out.print("feature " + feature.toColorString() + " type " + typeString + " ");
                } else if (feature.types.size() == 1) {
                    System.out.print("feature " + feature.toColorString() + " type " + typeString + " ");
                } else if (feature.types.size() > 1) {
                    System.out.print("feature " + feature.toColorString() + " types " + typeString + " ");
                }

                if (feature.types != null) {
//                if (feature.types == Type.request("text")) {
                    if (feature.types.contains(Type.request("text"))) {
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
//                } else if (feature.types == Type.request("list")) {
                    } else if (feature.types.contains(Type.request("list"))) {
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
//                    if (feature.listTypes.contains(Type.request("text"))) {
////                    } else if (feature.listType == Type.request("construct")) {
////                    } else if (feature.listTypes.contains(Type.request("construct"))) {
//////                        System.out.print("can contain " + feature.listType + ": ");
////                        System.out.print("can contain construct: ");
////                        for (int i = 0; i < feature.domain.size(); i++) {
////                            System.out.print(feature.domain.request(i) + " ");
////                        }
////                    } else if (feature.listType == Type.request("any")) {
//                    } else if (feature.listTypes.contains(Type.request("any"))) {
//                        System.out.print("can contain " + Type.request("any") + "");
////                    } else if (TypeId.has(feature.listType.address)) {
//                    } else { // if (TypeId.has(feature.listType.address)) {
//                        for (int i = 0; i < feature.listTypes.size(); i++) {
//                            System.out.print("can contain " + feature.listTypes.request(i) + ": ");
//                        }
////                        System.out.print("can contain " + feature.listType + ": ");
//                        for (int i = 0; i < feature.domain.size(); i++) {
//                            System.out.print(feature.domain.request(i) + " ");
//                        }
//                    }
////                } else if (feature.types == Type.request("construct")) {
////                } else if (feature.types.contains(Type.request("construct"))) { // TODO: Don't use general "construct"
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
                }
                System.out.println();

            } else {
                // Print response
                System.out.println(Color.ANSI_RED + "Error: Bad feature syntax." + Color.ANSI_RESET);
            }

        } else if (Address.isType(context.address)) {

            // TODO:

        }

    }

    // e.g.,
    // structure port
    // [DELETED] structure port my-port
    public void constructTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

        // Defaults
        String featureIdentifierToken = null;

        // Determine address
        if (inputLineTokens.length >= 2) {
            featureIdentifierToken = inputLineTokens[1];

//                TypeId type = null;
                Type type = null;
//                if (Type.exists(featureIdentifierToken)) {
                    if (Type.exists(featureIdentifierToken)) {
//                        type = Type.request(type);

                        // TODO: Factor this into a function in Context (to automate tracking of most-recent type)
                        type = context.conceptReferences.get(featureIdentifierToken);
                    }
//                }

//                // Parse label if it exists
//                String label = null;
//                if (inputLineTokens.length > 2) {
//
//                    // PARSING STEPS:
//                    // Tokenize
//                    // Validate
//                    // Parse
//
//                    label = inputLineTokens[2];
//                }

//                if (type != null && type != null) {
                if (type != null) {
//                    Structure structure = Structure.create(type);
                    Structure structure = Structure.create(type);

                    Reference constructReference = Reference.create(structure);
                    System.out.println(constructReference.toColorString());
//                    System.out.println("reference " + type.toColorString() + " -> structure " + structure.toColorString());

//                    System.out.println("(id: " + structure.uid + ") " + Application.ANSI_GREEN + structure.types + Application.ANSI_RESET + " (uuid: " + structure.uuid + ")");
//                    System.out.println("structure " + Application.ANSI_GREEN + structure.type + Application.ANSI_RESET + " (id: " + structure.uid + ")" + " (uuid: " + structure.uuid + ")");

//                    // Store label in context if one was provided.
//                    if (label != null) {
//                        context.references.put(label, constructReference);
//                    }

                    // Update object
//                    address = structure;
                    context.address = constructReference;
                } else {
                    System.out.println(Color.ANSI_RED + "Error: No types or type matches '" + featureIdentifierToken + "'" + Color.ANSI_RESET);
                }

            }

            // Parse constraint
//            String letParameters = object.expression.substring(object.expression.indexOf(":") + 1);
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

        // Determine interpreter's object. Type or instance?
        if (Address.isStructure(context.address)) {

            String[] inputLineTokens = context.expression.split("[ ]+");

            // Defaults
            String featureIdentifier = null;

            // Determine address
            if (inputLineTokens.length >= 3) {

                // Extract feature address and feature state
                featureIdentifier = inputLineTokens[1];
                String stateExpression = inputLineTokens[2];

                // TODO: if featureContentToken is instance UID/UUID, look it up and pass that into "set"

                Structure currentStructure = Address.getStructure(context.address);
                HashMap<String, Feature> currentConstructFeatures = (HashMap<String, Feature>) currentStructure.object;

                Structure currentFeatureStructure = currentStructure.states.get(featureIdentifier);

                if (currentConstructFeatures.get(featureIdentifier).types != null
                        && (currentConstructFeatures.get(featureIdentifier).types.size() == 1 && currentConstructFeatures.get(featureIdentifier).types.contains(Type.request("list")))) {
//                        || currentFeatureStructure.type == Type.request("list")) {
                    System.out.println(Color.ANSI_RED + "Error: Cannot assign non-list to a list." + Color.ANSI_RESET);
                } else {

                    Structure replacementFeatureStructure = Structure.request(stateExpression);

                    // Determine if the replacement construct's type can be assigned to the feature
                    // Note: Any feature can be assigned 'none' and any type can be assigned if the feature supports any time (i.e., types is null).
                    if (replacementFeatureStructure != Structure.request("none")
                            && Structure.getFeature(currentStructure, featureIdentifier).types != null
                            && !Structure.getFeature(currentStructure, featureIdentifier).types.contains(replacementFeatureStructure.type2)) {
                        // TODO: Check types!
                        System.out.println(Error.get("Feature " + featureIdentifier + " doesn't support type " + replacementFeatureStructure.type2));
                        return;
                    }

                    // Determine if replacement construct is in the feature's domain
                    if (replacementFeatureStructure != Structure.request("none")
                            && Structure.getFeature(currentStructure, featureIdentifier).domain != null
                            && !Structure.getFeature(currentStructure, featureIdentifier).domain.contains(replacementFeatureStructure)) {
                        // TODO: Check domain!
                        System.out.println(Error.get("Feature " + featureIdentifier + " domain doesn't contain " + replacementFeatureStructure));
                        return;
                    }

                    boolean isSameConstruct = true;
                    Structure replacementStructure = Structure.request(currentStructure, featureIdentifier, replacementFeatureStructure);
                    if (replacementStructure != null) {
                        ((Reference) context.address).object = replacementStructure;
                        if (currentStructure == replacementStructure) {
                            isSameConstruct = true;
                        } else {
                            isSameConstruct = false;
                        }
                        currentStructure = (Structure) ((Reference) context.address).object;
                    }

                    // Print the feature construct
                    Structure featureStructure = currentStructure.states.get(featureIdentifier);
                    System.out.println(featureStructure.toColorString());

                    // Print the in-context construct (with the new feature construct)
                    if (replacementStructure != null) {
                        System.out.print(Color.ANSI_CYAN + (isSameConstruct ? "[SAME CONSTRUCT] " : "[SWITCHED CONSTRUCT] ") + Color.ANSI_RESET);
                        System.out.println(((Reference) context.address).toColorString());
                    }
                }

            }

        } else {
            System.out.println(Error.get("Cannot set feature on type."));
        }

    }

    // add some-list : port(id:34), port(uuid:<uuid>), port(id:44)
    // add some-list port(id:34), port(uuid:<uuid>), port(id:44)
    public void addTask(Context context) {

        // Determine interpreter's object. Type or instance?
        if (Address.isStructure(context.address)) {

            // Defaults
            String featureIdentifier = null;

            // Tokenize
            int startIndex = context.expression.indexOf(" ") + 1;
            int stopIndex = context.expression.indexOf(" ", startIndex);
            featureIdentifier = context.expression.substring(startIndex, stopIndex);
            String[] stateExpressionSegment = context.expression.substring(stopIndex + 1).split("[ ]*,[ ]*");

            // TODO: search for existing list construct that matches the requested list
            // TODO: + if the list doesn't exist, create it
            // TODO: with the existing or created list, search for the referenced construct (in Reference) that matches the would-be updated list
            // TODO: + if the construct doesn't exist with the specified permutation hierarchy, then create it and return that
            // TODO: ++ update the referenced construct in Reference

            // Parse
            for (int j = 0; j < stateExpressionSegment.length; j++) {
                String stateExpression = stateExpressionSegment[j];

                // TODO: if featureContentToken is instance UID/UUID, look it up and pass that into "set"

                Structure currentStructure = (Structure) ((Reference) context.address).object;
                HashMap<String, Feature> currentConstructFeatures = (HashMap<String, Feature>) currentStructure.object;

                // Check if feature is valid. If not, show error.
                if (!currentConstructFeatures.containsKey(featureIdentifier)) {
                    System.out.println(Error.get(featureIdentifier + " is not a feature."));
                    return;
                }

                Structure currentFeatureStructure = currentStructure.states.get(featureIdentifier);

                if ((currentConstructFeatures.get(featureIdentifier).types.size() == 1 && currentConstructFeatures.get(featureIdentifier).types.contains(Type.request("list")))
                        || currentFeatureStructure.type2 == Type.request("list")) {

                    Structure additionalFeatureStructure = null;
                    if (context.references.containsKey(stateExpression)) {
                        // TODO: Check for type error!
                        additionalFeatureStructure = (Structure) context.references.get(stateExpression).object;
                    } else {
                        additionalFeatureStructure = Structure.request(stateExpression); // replacementStructure
                    }

                    ArrayList requestedConstructList = new ArrayList();
                    if (currentFeatureStructure.type2 == Type.request("list")) {
                        requestedConstructList.addAll(((List) currentFeatureStructure.object));
                    }
                    requestedConstructList.add(additionalFeatureStructure);

                    // TODO: Search for list!
                    Structure replacementFeatureStructure = Structure.request(requestedConstructList);
                    System.out.println(replacementFeatureStructure);

                    // TODO: Search for Structure with new list...
                    Structure replacementStructure = Structure.request(currentStructure, featureIdentifier, replacementFeatureStructure);
//                    System.out.println("reference -> " + replacementStructure);

                    if (replacementStructure != null) {
                        ((Reference) context.address).object = replacementStructure;
                        if (currentStructure == replacementStructure) {
                            System.out.print("[SAME CONSTRUCT] ");
                        } else {
                            System.out.print("[SWITCHED CONSTRUCT] ");
                        }
                        currentStructure = (Structure) ((Reference) context.address).object;
//                    System.out.println("REPLACEMENT: " + replacementStructure);
                        System.out.println("reference " + currentStructure.type2.toColorString() + " (id: " + context.address.uid + ") -> construct " + currentStructure.type2.toColorString() + " (id: " + currentStructure.uid + ")" + " (uuid: " + currentStructure.uuid + ")");
                    }

                } else {
                    System.out.println(Color.ANSI_RED + "Error: Cannot assign non-list to a list." + Color.ANSI_RESET);

                }





//                Structure currentStructure = (Structure) ((Reference) address).object;
////                Structure currentFeatureStructure = currentStructure.states.request(featureIdentifier);
//
////                Structure replacementFeatureConstruct = Structure.request(stateExpression);
////                Structure replacementConstruct = Manager.getPersistentConstruct(currentStructure, featureIdentifier, replacementFeatureConstruct);
//
////                ((Structure) address).insert(featureIdentifier, featureContentToken);
//
//                System.out.print(featureIdentifier + " : ");
////                List list = (List) ((Structure) address).states.request(featureIdentifier).object;
//
//
//                Structure additionalFeatureConstruct = Structure.request(stateExpression); // replacementConstruct
//                ArrayList requestedConstructList = new ArrayList();
//                // TODO: Search for list!
//
//
//                List list = (List) currentStructure.states.request(featureIdentifier).object;
//                for (int i = 0; i < list.size(); i++) {
//                    System.out.print(((Structure) list.request(i)));
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

    public void describeTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            List<Type> typeIdList = Type.list();
            for (int i = 0; i < typeIdList.size(); i++) {
                List<Reference> referenceList = Manager.get(Reference.class);
                // System.out.println("(id: " + typeIdList.request(i).uid + ") " + Application.ANSI_BLUE + typeIdList.request(i).address + Application.ANSI_RESET + " (" + constructList.size() + ") (uuid: " + typeIdList.request(i).uuid + ")");
//                int typeReferenceCount = 0;
//                for (int j = 0; j < referenceList.size(); j++) {
//                    if (((Structure) (((Reference) referenceList.request(j)).object)).type == typeIdList.request(i)) {
//                        typeReferenceCount++;
//                    }
//                }
//                System.out.println(Color.ANSI_BLUE + typeIdList.request(i).address + Color.ANSI_RESET + " (count: " + typeReferenceCount + ")");
                System.out.println(Color.ANSI_BLUE + typeIdList.get(i).identifier + Color.ANSI_RESET);
            }

        } else if (inputLineTokens.length >= 2) {

            // String typeToken = inputLineTokens[1]; // "port" or "port (id: 3)"
            String typeToken = context.expression.substring(context.expression.indexOf(" ") + 1);

                if (Expression.isConstruct(typeToken)) {

                    // String[] tokens = context.expression.split("\\.");
                    String[] tokens = typeToken.split("\\.");
                    String typeIdentifierToken = tokens[0];
                    String addressTypeToken = tokens[1];
                    String addressToken = tokens[2];

                    if (addressTypeToken.equals("id")) {
                        Structure structure = null;
                        long uid = Long.parseLong(addressToken.trim());
                        Address address = Manager.get(uid);
                        if (address == null) {
                            System.out.println(Error.get("No type with UID " + uid));
                            return;
                        }

                        if (address.getClass() == Reference.class) {
                            Reference reference = (Reference) address;
                            structure = (Structure) reference.object;
                        } else if (address.getClass() == Structure.class) {
                            structure = (Structure) address;
//                        } else if (address.getClass() == Type.class) {
//                            System.out.println("Error: The UID is for a type.");
//                            //                                Type type = (Type) address;
//                            //                                System.out.println("Found " + type.types + " with UID " + uid);
                        }

                        if (structure != null && structure.type2 == Type.request(typeIdentifierToken)) {
                            if (structure.type2 == Type.request("type")) {
                                System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET + " primistructure");
                            } else if (structure.type2 == Type.request("none")) {
                                System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET + " default primitive structure that represents absence of any data structure");
                            } else if (structure.type2 == Type.request("number")) {
                                System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET + " primitive structure representing a sequence of characters");
                            } else if (structure.type2 == Type.request("text")) {
                                System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET + " primitive structure representing a sequence of characters");
                            } else if (structure.type2 == Type.request("list")) {
                                System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET + " primitive structure representing a list that contains constructs");
                            } else {

//                                System.out.println(Color.ANSI_BLUE + structure.type.address + Color.ANSI_RESET);
                                System.out.println(structure.toColorString());

                                HashMap<String, Feature> features = (HashMap<String, Feature>) structure.object;
                                HashMap<String, Structure> states = (HashMap<String, Structure>) structure.states;
                                for (String featureIdentifier : features.keySet()) {
                                    Feature feature = features.get(featureIdentifier);
                                    String featureTypes = "";
                                    for (int i = 0; i < feature.types.size(); i++) {
                                        featureTypes += feature.types.get(i);
                                        if ((i + 1) < feature.types.size()) {
                                            featureTypes += ", ";
                                        }
                                    }
                                    System.out.println(Color.ANSI_GREEN + features.get(featureIdentifier).identifier + Color.ANSI_RESET + " " + Color.ANSI_BLUE + featureTypes + Color.ANSI_RESET + " " + states.get(featureIdentifier).toColorString());
                                    // TODO: print current object types; print available feature types
                                }

                            }
                        }
                    } else if (addressToken.equals("uuid")) {

                    } else {

                }

            } else if (Type.exists(typeToken)) {
//                // TODO: Print Type
//                System.out.println("VIEW CONCEPT");
//
//                System.out.println();

//                List<Structure> constructList = Manager.getStructureList(Type.request(typeToken));
//                for (int i = 0; i < constructList.size(); i++) {
//
//                    Structure construct = constructList.request(i);

//                    if (constructList.request(i).typeId == Type.request(typeToken)) {
//                        System.out.println(constructList.request(i).toColorString());
////                    System.out.println("(id: " + constructList.request(i).uid + ") " + Application.ANSI_GREEN + constructList.request(i).classType + Application.ANSI_RESET + " (uuid: " + constructList.request(i).uuid + ")");
//                    }

                        Type typeId = Type.request(typeToken);

                        if (typeId == Type.request("typeId")) {
                            System.out.println(Color.ANSI_BLUE + typeId.identifier + Color.ANSI_RESET + " a data structure that characterizes an entity");
                        } else if (typeId == Type.request("none")) {
                            System.out.println(Color.ANSI_BLUE + typeId.identifier + Color.ANSI_RESET + " denotes absence or nonexistence (of structure)");
                        } else if (typeId == Type.request("number")) {
                            System.out.println(Color.ANSI_BLUE + typeId.identifier + Color.ANSI_RESET + " a numerical value");
                        } else if (typeId == Type.request("text")) {
                            System.out.println(Color.ANSI_BLUE + typeId.identifier + Color.ANSI_RESET + " sequence of characters");
                        } else if (typeId == Type.request("list")) {
                            System.out.println(Color.ANSI_BLUE + typeId.identifier + Color.ANSI_RESET + " sequence of constructs");
                        } else {

                            System.out.println(Color.ANSI_BLUE + typeId.identifier + Color.ANSI_RESET);

                            // Determine the type to describe for the typeId
                            Type type = null;
                            if (context.conceptReferences.containsKey(typeId)) {
                                type = context.conceptReferences.get(typeId);
                            } else {
                                type = Type.request(typeToken);
                            }

                            // Print the type's data structure
                            HashMap<String, Feature> features = (HashMap<String, Feature>) type.features;
                            for (String featureIdentifier : features.keySet()) {
                                Feature feature = features.get(featureIdentifier);
                                String featureTypes = "";
                                for (int j = 0; j < feature.types.size(); j++) {
                                    featureTypes += feature.types.get(j);
                                    if ((j + 1) < feature.types.size()) {
                                        featureTypes += ", ";
                                    }
                                }
                                System.out.println(Color.ANSI_YELLOW + features.get(featureIdentifier).identifier + Color.ANSI_RESET + " " + Color.ANSI_BLUE + featureTypes + Color.ANSI_RESET);
                                // TODO: print current object types; print available feature types
                            }

                        }
                    }

//                List<Reference> referenceList = Manager.request(Reference.class);
//                for (int i = 0; i < referenceList.size(); i++) {
//                    if (((Structure) (referenceList.request(i)).object).type == Type.request(typeToken)) {
//                        System.out.println(referenceList.request(i).toColorString());
////                    System.out.println("(id: " + constructList.request(i).uid + ") " + Application.ANSI_GREEN + constructList.request(i).classType + Application.ANSI_RESET + " (uuid: " + constructList.request(i).uuid + ")");
//                    }
//                }
//            }
        }
    }

    // describes the local context by default (or referenced construct by UUID)
    public void privateContextTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            List<Type> typeIdList = Type.list();
            for (int i = 0; i < typeIdList.size(); i++) {
                List<Reference> referenceList = Manager.get(Reference.class);
                // System.out.println("(id: " + typeIdList.request(i).uid + ") " + Application.ANSI_BLUE + typeIdList.request(i).address + Application.ANSI_RESET + " (" + constructList.size() + ") (uuid: " + typeIdList.request(i).uuid + ")");
                int typeReferenceCount = 0;
                for (int j = 0; j < referenceList.size(); j++) {
                    Type typeDefault = Type.request(typeIdList.get(i).identifier);
                    if (((Structure) (((Reference) referenceList.get(j)).object)).type2 == typeDefault) {
                        typeReferenceCount++;
                    }
                }
                System.out.println(Color.ANSI_BLUE + typeIdList.get(i).identifier + Color.ANSI_RESET + " (count: " + typeReferenceCount + ")");
            }

        } else if (inputLineTokens.length >= 2) {

            String typeToken = inputLineTokens[1]; // "port" or "port (id: 3)"

            if (Expression.isConstruct(typeToken)) {

//                String typeIdentifierToken = typeToken.substring(0, typeToken.indexOf("(")).trim(); // text before '('
//                String addressTypeToken = typeToken.substring(typeToken.indexOf("(") + 1, typeToken.indexOf(":")).trim(); // text between '(' and ':'
//                String addressToken = typeToken.substring(typeToken.indexOf(":") + 1, typeToken.indexOf(")")).trim(); // text between ':' and ')'
//
//                if (addressTypeToken.equals("id")) {
//                    Structure construct = null;
//                    long uid = Long.parseLong(addressToken.trim());
//                    Address address = Manager.request(uid);
//                    if (address == null) {
//                        System.out.println(Color.ANSI_RED + "Error: No type with UID " + uid + Color.ANSI_RESET);
//                    } else if (address.getClass() == Structure.class) {
//                        construct = (Structure) address;
////                        } else if (address.getClass() == Type.class) {
////                            System.out.println("Error: The UID is for a type.");
////                            //                                Type type = (Type) address;
////                            //                                System.out.println("Found " + type.types + " with UID " + uid);
//                    }
//
//                    if (construct != null && construct.type == Type.request(typeIdentifierToken)) {
//                        if (construct.type == Type.request("none")) {
//
//                            System.out.println("REFERENCE (id:X) -> " + construct);
//
//                        } else if (construct.type == Type.request("number")) {
//
//                        } else if (construct.type == Type.request("text")) {
//
////                            String feature = (String) construct.object;
//                            System.out.println("REFERENCE (id:X) -> " + construct);
//
//                        } else if (construct.type == Type.request("list")) {
//
//                        } else {
//
//                            System.out.println(Color.ANSI_BLUE + construct.type.address + Color.ANSI_RESET);
//
//                            HashMap<String, Feature> features = (HashMap<String, Feature>) construct.object;
//                            for (String featureIdentifier : features.keySet()) {
//                                Feature feature = features.request(featureIdentifier);
//                                String featureTypes = "";
//                                for (int i = 0; i < feature.types.size(); i++) {
//                                    featureTypes += feature.types.request(i);
//                                    if ((i + 1) < feature.types.size()) {
//                                        featureTypes += ", ";
//                                    }
//                                }
//                                System.out.println(Color.ANSI_GREEN + features.request(featureIdentifier).address + Color.ANSI_RESET + " " + Color.ANSI_BLUE + featureTypes + Color.ANSI_RESET);
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

            } else if (Type.exists(typeToken)) {
//                // TODO: Print Type
//                System.out.println("VIEW CONCEPT");
//
//                System.out.println();

                List<Structure> structureList = Manager.getStructureList(Type.request(typeToken));
                for (int i = 0; i < structureList.size(); i++) {

                    Structure structure = structureList.get(i);

//                    if (structureList.request(i).type == Type.request(typeToken)) {
//                        System.out.println(structureList.request(i).toColorString());
////                    System.out.println("(id: " + structureList.request(i).uid + ") " + Application.ANSI_GREEN + structureList.request(i).classType + Application.ANSI_RESET + " (uuid: " + structureList.request(i).uuid + ")");
//                    }

                    if (structure != null && structure.type2 == Type.request(typeToken)) {
                        if (structure.type2 == Type.request("none")) {

                            System.out.println("REFERENCE (id:X) -> " + structure);

                        } else if (structure.type2 == Type.request("number")) {

                        } else if (structure.type2 == Type.request("text")) {

//                            String feature = (String) structure.object;
                            System.out.println("REFERENCE (id:X) -> " + structure);

                        } else if (structure.type2 == Type.request("list")) {

                        } else {

                            System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET);

                            HashMap<String, Feature> features = (HashMap<String, Feature>) structure.object;
                            for (String featureIdentifier : features.keySet()) {
                                Feature feature = features.get(featureIdentifier);
                                String featureTypes = "";
                                for (int j = 0; j < feature.types.size(); j++) {
                                    featureTypes += feature.types.get(j);
                                    if ((j + 1) < feature.types.size()) {
                                        featureTypes += ", ";
                                    }
                                }
                                System.out.println(Color.ANSI_GREEN + features.get(featureIdentifier).identifier + Color.ANSI_RESET + " " + Color.ANSI_BLUE + featureTypes + Color.ANSI_RESET);
                                // TODO: print current object types; print available feature types
                            }

                        }
                    }
                }

//                List<Reference> referenceList = Manager.request(Reference.class);
//                for (int i = 0; i < referenceList.size(); i++) {
//                    if (((Structure) (referenceList.request(i)).object).type == Type.request(typeToken)) {
//                        System.out.println(referenceList.request(i).toColorString());
////                    System.out.println("(id: " + structureList.request(i).uid + ") " + Application.ANSI_GREEN + structureList.request(i).classType + Application.ANSI_RESET + " (uuid: " + structureList.request(i).uuid + ")");
//                    }
//                }
            }
        }
    }

    // Searches remote repository.
    //
    // Usage:
    // list [<types-address>]
    //
    // Examples:
    // list         Lists available types.
    // list port    Lists port constructs.
    // list path    Lists path constructs.
    public void searchTask(Context context) { // previously, viewTask

        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            List<Type> typeList = Type.list();
            for (int i = 0; i < typeList.size(); i++) {
                List<Structure> structureList = Manager.getStructureList(typeList.get(i));
                // System.out.println("(id: " + typeIdList.request(i).uid + ") " + Application.ANSI_BLUE + typeIdList.request(i).address + Application.ANSI_RESET + " (" + structureList.size() + ") (uuid: " + typeIdList.request(i).uuid + ")");
                System.out.println(Color.ANSI_BLUE + typeList.get(i).identifier + Color.ANSI_RESET + " (count: " + structureList.size() + ")");
            }

        } else if (inputLineTokens.length >= 2) {

            String typeToken = inputLineTokens[1]; // "port" or "port (id: 3)"

            if (Expression.isConstruct(typeToken)) {

                String typeIdentifierToken = typeToken.substring(0, typeToken.indexOf("(")).trim(); // text before '('
                String addressTypeToken = typeToken.substring(typeToken.indexOf("(") + 1, typeToken.indexOf(":")).trim(); // text between '(' and ':'
                String addressToken = typeToken.substring(typeToken.indexOf(":") + 1, typeToken.indexOf(")")).trim(); // text between ':' and ')'

                if (addressTypeToken.equals("id")) {
                    Structure structure = null;
                    long uid = Long.parseLong(addressToken.trim());
                    Address address = Manager.get(uid);
                    if (address == null) {
                        System.out.println(Color.ANSI_RED + "Error: No type with UID " + uid + Color.ANSI_RESET);
                        return;
                    }

                    if (address.getClass() == Reference.class) {
                        Reference reference = (Reference) address;
                        structure = (Structure) reference.object;
                    } else if (address.getClass() == Structure.class) {
                        structure = (Structure) address;
//                        } else if (address.getClass() == Type.class) {
//                            System.out.println("Error: The UID is for a type.");
//                            //                                Type type = (Type) address;
//                            //                                System.out.println("Found " + type.types + " with UID " + uid);
                    }

                    if (structure != null && structure.type2 == Type.request(typeIdentifierToken)) {
                        if (structure.type2 == Type.request("none")) {

                            System.out.println("REFERENCE (id:X) -> " + structure);

                        } else if (structure.type2 == Type.request("number")) {

                        } else if (structure.type2 == Type.request("text")) {

//                            String feature = (String) structure.object;
                            System.out.println("REFERENCE (id:X) -> " + structure);

                        } else if (structure.type2 == Type.request("list")) {

                        } else {

                            System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET);

                            HashMap<String, Feature> features = (HashMap<String, Feature>) structure.object;
                            HashMap<String, Structure> states = (HashMap<String, Structure>) structure.states;
                            for (String featureIdentifier : features.keySet()) {
                                Feature feature = features.get(featureIdentifier);
                                String featureTypes = "";
                                for (int i = 0; i < feature.types.size(); i++) {
                                    featureTypes += feature.types.get(i);
                                    if ((i + 1) < feature.types.size()) {
                                        featureTypes += ", ";
                                    }
                                }
                                System.out.println(Color.ANSI_GREEN + features.get(featureIdentifier).identifier + Color.ANSI_RESET + " " + Color.ANSI_BLUE + featureTypes + Color.ANSI_RESET + " " + states.get(featureIdentifier));
                                // TODO: print current object types; print available feature types
                            }

                        }
                    }
                } else if (addressToken.equals("uuid")) {

                } else {

                }

            } else if (Type.exists(typeToken)) {

                // TODO: Print Type
//                System.out.println("VIEW CONCEPT");
//                System.out.println();

                List<Structure> structureList = Manager.getStructureList(Type.request(typeToken));
                for (int i = 0; i < structureList.size(); i++) {
                    System.out.println(structureList.get(i).toColorString());
//                    System.out.println("(id: " + structureList.request(i).uid + ") " + Application.ANSI_GREEN + structureList.request(i).classType + Application.ANSI_RESET + " (uuid: " + structureList.request(i).uuid + ")");
                }
            }
        }
    }

    // Lists references to constructs in the current (private) context
    public void listTask(Context context) { // previously, viewTask

        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            List<Type> typeList = Type.list();
            for (int i = 0; i < typeList.size(); i++) {
                List<Structure> structureList = Manager.getStructureList(typeList.get(i));
                // System.out.println("(id: " + typeIdList.request(i).uid + ") " + Application.ANSI_BLUE + typeIdList.request(i).address + Application.ANSI_RESET + " (" + structureList.size() + ") (uuid: " + typeIdList.request(i).uuid + ")");
                System.out.println(Color.ANSI_BLUE + typeList.get(i).identifier + Color.ANSI_RESET + " (count: " + structureList.size() + ")");
            }

        } else if (inputLineTokens.length >= 2) {

            String typeToken = inputLineTokens[1]; // "port" or "port (id: 3)"

            if (Expression.isConstruct(typeToken)) {

                String typeIdentifierToken = typeToken.substring(0, typeToken.indexOf("(")).trim(); // text before '('
                String addressTypeToken = typeToken.substring(typeToken.indexOf("(") + 1, typeToken.indexOf(":")).trim(); // text between '(' and ':'
                String addressToken = typeToken.substring(typeToken.indexOf(":") + 1, typeToken.indexOf(")")).trim(); // text between ':' and ')'

                if (addressTypeToken.equals("id")) {
                    Structure structure = null;
                    long uid = Long.parseLong(addressToken.trim());
                    Address address = Manager.get(uid);
                    if (address == null) {
                        System.out.println(Color.ANSI_RED + "Error: No type with UID " + uid + Color.ANSI_RESET);
                        return;
                    }

                    if (address.getClass() == Reference.class) {
                        Reference reference = (Reference) address;
                        structure = (Structure) reference.object;
                    } else if (address.getClass() == Structure.class) {
                        structure = (Structure) address;
//                        } else if (address.getClass() == Type.class) {
//                            System.out.println("Error: The UID is for a type.");
//                            //                                Type type = (Type) address;
//                            //                                System.out.println("Found " + type.types + " with UID " + uid);
                    }

                    if (structure != null && structure.type2 == Type.request(typeIdentifierToken)) {
                        if (structure.type2 == Type.request("none")) {

//                            System.out.println("REFERENCE (id:X) -> " + structure);
                            System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET + " default primitive structure that represents absence of any data structure");

                        } else if (structure.type2 == Type.request("number")) {

                        } else if (structure.type2 == Type.request("text")) {

//                            System.out.println("REFERENCE (id:X) -> " + structure);
                            System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET + " primitive structure representing a sequence of characters");

                        } else if (structure.type2 == Type.request("list")) {

                            System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET + " primitive structure representing a list that contains constructs");

                        } else {

                            System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET);

                            HashMap<String, Feature> features = (HashMap<String, Feature>) structure.object;
                            HashMap<String, Structure> states = (HashMap<String, Structure>) structure.states;
                            for (String featureIdentifier : features.keySet()) {
                                Feature feature = features.get(featureIdentifier);
                                String featureTypes = "";
                                for (int i = 0; i < feature.types.size(); i++) {
                                    featureTypes += feature.types.get(i);
                                    if ((i + 1) < feature.types.size()) {
                                        featureTypes += ", ";
                                    }
                                }
                                System.out.println(Color.ANSI_GREEN + features.get(featureIdentifier).identifier + Color.ANSI_RESET + " " + Color.ANSI_BLUE + featureTypes + Color.ANSI_RESET + " " + states.get(featureIdentifier));
                                // TODO: print current object types; print available feature types
                            }

                        }
                    }
                } else if (addressToken.equals("uuid")) {

                } else {

                }

            } else if (Type.exists(typeToken)) {

                // TODO: Print Type
//                System.out.println("VIEW CONCEPT");
//                System.out.println();

                List<Structure> structureList = Manager.getStructureList(Type.request(typeToken));
                for (int i = 0; i < structureList.size(); i++) {
                    System.out.println(structureList.get(i).toColorString());
//                    System.out.println("(id: " + structureList.request(i).uid + ") " + Application.ANSI_GREEN + structureList.request(i).classType + Application.ANSI_RESET + " (uuid: " + structureList.request(i).uuid + ")");
                }
            }
        }
    }

    // Lists references to constructs in the current (private) context
    public void listReferenceTask(Context context) { // previously, viewTask

        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            for (String referenceKey : context.references.keySet()) {
                Reference reference = context.references.get(referenceKey);
                Structure structure = (Structure) reference.object;
//                System.out.println(Color.ANSI_YELLOW + referenceKey + Color.ANSI_RESET + " -> " + reference.toColorString());
//                System.out.println(Color.ANSI_YELLOW + referenceKey + Color.ANSI_RESET + " -- " + reference.toColorString());
                System.out.println(Color.ANSI_YELLOW + referenceKey + Color.ANSI_RESET + " >> " + reference.toColorString());
            }

            // List anonymous references
            List<Reference> referenceList = Manager.get(Reference.class);
            for (Reference reference : referenceList) {
                if (!context.references.containsValue(reference)) {
//                    System.out.println(Color.ANSI_YELLOW + "(anonymous)" + Color.ANSI_RESET + " " + reference.toColorString());
                    System.out.println(reference.toColorString());
                }
            }

//            List<TypeId> typeList = Type.request();
//            for (int i = 0; i < typeList.size(); i++) {
//                List<Structure> constructList = Manager.getStructureList(typeList.request(i));
//                // System.out.println("(id: " + typeList.request(i).uid + ") " + Application.ANSI_BLUE + typeList.request(i).address + Application.ANSI_RESET + " (" + constructList.size() + ") (uuid: " + typeList.request(i).uuid + ")");
//                System.out.println(Color.ANSI_BLUE + typeList.request(i).address + Color.ANSI_RESET + " (count: " + constructList.size() + ")");
//            }

        } else if (inputLineTokens.length >= 2) {

            String typeToken = inputLineTokens[1]; // "port" or "port (id: 3)"

            if (Expression.isConstruct(typeToken)) {

                String typeIdentifierToken = typeToken.substring(0, typeToken.indexOf("(")).trim(); // text before '('
                String addressTypeToken = typeToken.substring(typeToken.indexOf("(") + 1, typeToken.indexOf(":")).trim(); // text between '(' and ':'
                String addressToken = typeToken.substring(typeToken.indexOf(":") + 1, typeToken.indexOf(")")).trim(); // text between ':' and ')'

                if (addressTypeToken.equals("id")) {
                    Structure structure = null;
                    long uid = Long.parseLong(addressToken.trim());
                    Address address = Manager.get(uid);
                    if (address == null) {
                        System.out.println(Color.ANSI_RED + "Error: No type with UID " + uid + Color.ANSI_RESET);
                        return;
                    }

                    if (address.getClass() == Reference.class) {
                        Reference reference = (Reference) address;
                        structure = (Structure) reference.object;
                    } else if (address.getClass() == Structure.class) {
                        structure = (Structure) address;
//                        } else if (address.getClass() == Type.class) {
//                            System.out.println("Error: The UID is for a type.");
//                            //                                Type type = (Type) address;
//                            //                                System.out.println("Found " + type.types + " with UID " + uid);
                    }

                    if (structure != null && structure.type2 == Type.request(typeIdentifierToken)) {
                        if (structure.type2 == Type.request("none")) {

                            System.out.println("REFERENCE (id:X) -> " + structure);

                        } else if (structure.type2 == Type.request("number")) {

                        } else if (structure.type2 == Type.request("text")) {

//                            String feature = (String) structure.object;
                            System.out.println("REFERENCE (id:X) -> " + structure);

                        } else if (structure.type2 == Type.request("list")) {

                        } else {

                            System.out.println(Color.ANSI_BLUE + structure.type2.identifier + Color.ANSI_RESET);

                            HashMap<String, Feature> features = (HashMap<String, Feature>) structure.object;
                            HashMap<String, Structure> states = (HashMap<String, Structure>) structure.states;
                            for (String featureIdentifier : features.keySet()) {
                                Feature feature = features.get(featureIdentifier);
                                String featureTypes = "";
                                for (int i = 0; i < feature.types.size(); i++) {
                                    featureTypes += feature.types.get(i);
                                    if ((i + 1) < feature.types.size()) {
                                        featureTypes += ", ";
                                    }
                                }
                                System.out.println(Color.ANSI_GREEN + features.get(featureIdentifier).identifier + Color.ANSI_RESET + " " + Color.ANSI_BLUE + featureTypes + Color.ANSI_RESET + " " + states.get(featureIdentifier));
                                // TODO: print current object types; print available feature types
                            }

                        }
                    }
                } else if (addressToken.equals("uuid")) {

                } else {

                }

            } else if (Type.exists(typeToken)) {

                // TODO: Print Type
//                System.out.println("VIEW CONCEPT");
//                System.out.println();

                List<Structure> structureList = Manager.getStructureList(Type.request(typeToken));
                for (int i = 0; i < structureList.size(); i++) {
                    System.out.println(structureList.get(i).toColorString());
//                    System.out.println("(id: " + structureList.request(i).uid + ") " + Application.ANSI_GREEN + structureList.request(i).classType + Application.ANSI_RESET + " (uuid: " + structureList.request(i).uuid + ")");
                }
            }
        }
    }

    public void listTypeTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            for (Type type : Type.list()) {
                System.out.println(type.toColorString() + " (count: TODO-LIST-CONCEPT-COUNT)");
            }

        } else if (inputLineTokens.length >= 2) {

            String typeToken = inputLineTokens[1]; // "port" or "port (id: 3)"
            Type type = Type.request(typeToken);
            if (type != null) {
                List<Type> types = Manager.get(Type.class);
                for (int i = 0; i < types.size(); i++) {
                    if (types.get(i).identifier.equals(typeToken)) {
                        System.out.println(types.get(i).toColorString());
                    }
                }
            }

        }
    }

    // print <feature-address>
    // e.g., print mode
    public void printTask(Context context) {

        // Determine interpreter's object. Type or instance?
        if (Address.isStructure(context.address)) {

            String[] inputLineTokens = context.expression.split("[ ]+");

            // Defaults
            String featureToken = null;

            // Determine address
            if (inputLineTokens.length >= 2) {

                // Determine address
                featureToken = inputLineTokens[1];

                // TODO: if featureContentToken is instance UID/UUID, look it up and pass that into "set"

                Structure structure = Address.getStructure(context.address).states.get(featureToken);

                System.out.println(structure);

//                if (state != null && state.types == Type.request("text")) {
////                    System.out.println("" + ((String) state.object));
//                    System.out.println("" + state);
//                } else if (state != null && state.types == Type.request("text")) {
//                    List contentList = (List) state.object;
//                    for (int i = 0; i < contentList.size(); i++) {
//                        // TODO: Possibly use Content object for values to pair types with object (like little "files with extensions")?
////                        if (contentList.request(i).types == Type.request("text")) {
////                            System.out.println("" + ((String) object.object));
////                        }
//                    }
//                }

//                if (object.types == Type.request("text")) {
//                    System.out.println("" + ((String) object.object));
//                } else if (object.types == Type.request("list")) {
//                    List contentList = (List) object;
//                    for (int i = 0; i < contentList.size(); i++) {
//                        // TODO: Possibly use Content object for values to pair types with object (like little "files with extensions")?
////                        if (contentList.request(i).types == Type.request("text")) {
////                            System.out.println("" + ((String) object.object));
////                        }
//                    }
//                }

//                TypeId types = null;
//                Type identity = null;
//                if (TypeId.has(instanceTagToken)) {
//                    types = Type.request(instanceTagToken);
//                    if (Type.has(types)) {
//                        identity = Type.request(types);
//                    }
//                }

//                if (types != null && identity != null) {
//    //                    Structure instance = new Structure(identity);
//                    Structure instance = Structure.request(types);
//
//                    List<Structure> instanceList = Manager.request(Structure.class);
//                    System.out.println("added instance of identity " + instance.types + " (" + instanceList.size() + " instances)");
//                } else {
//                    System.out.println("Error: No types or identity matches '" + instanceTagToken + "'");
//                }
            }

            // Parse constraint
//            String letParameters = object.expression.substring(object.expression.indexOf(":") + 1);
//            String[] letParameterTokens = letParameters.split("[ ]*,[ ]*");

//            System.out.println("let parameters (" + letParameterTokens.length + "): " + letParameters);
//            for (int i = 0; i < letParameterTokens.length; i++) {
//                System.out.println("\t" + letParameterTokens[i].trim());
//            }

        } else {
            System.out.println(Color.ANSI_RED + "Error: Cannot set feature on type." + Color.ANSI_RESET);
        }

    }

    // CUSTOM_CONSTRUCT CONTEXT:
    // let direction : 'none', 'input', 'output', 'bidirectional'
    // let current-construct : device, port, controller, task, script
    // let script : script
    //
    // CONSTRUCT CONTEXT:
    // let mode:digital;direction:input;voltage:cmos
    // let mode: 'digital', 'analog'; direction: 'input', 'output'; voltage: 'ttl', 'cmos'
    // let mode: 'digital', 'analog' :: direction: 'input', 'output' :: voltage: 'ttl', 'cmos'
    // let mode 'digital', 'analog' :: direction 'input', 'output' :: voltage 'ttl', 'cmos'
    // let mode 'digital', 'analog' : direction 'input', 'output' : voltage 'ttl', 'cmos'
    public void letTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

        // Determine interpreter's object. Type or instance?
        if (Address.isType(context.address)) {

            // Defaults
            String featureTagToken = null;

            // Determine address
            if (inputLineTokens.length >= 2) {
                featureTagToken = inputLineTokens[1];
            }

            // Parse constraint
//            String letParameters = context.expression.substring(context.expression.indexOf(":") + 1);
            String letParameters = context.expression.substring(context.expression.indexOf(" ") + 1); // consume whitespace after "let"
//            String[] letParameterTokens = letParameters.split("[ ]*,[ ]*");
            String[] letParameterTokens = letParameters.split("[ ]*;[ ]*");

            System.out.println("let parameters (" + letParameterTokens.length + "): " + letParameters);
            for (int i = 0; i < letParameterTokens.length; i++) {
//                System.out.println("\t" + letParameterTokens[i].trim());

                String[] letParameter = letParameterTokens[i].split("[ ]*:[ ]*");

                System.out.print("\t" + letParameter[0].trim() + ": ");
//                System.out.print("\t" + letParameter[1] + " ");
                String[] letParameterList = letParameter[1].split("[ ]*,[ ]*");
                for (int j = 0; j < letParameterList.length; j++) {
                    Structure letParameterStructure = Structure.request(letParameterList[j]);
                    if (letParameterStructure == null) {
                        System.out.println(Error.get("Invalid construct provided with 'let'."));
                        return;
                    }
                    System.out.print(letParameterStructure + " ");
                }
                System.out.println();
            }

            // TODO: Store configuration domain/constraint for type feature assignment (not allowed for construct?)
            // TODO: ^^ this is analogous to the feature-level domain, but for multiple features

//            // Determine types
//            if (inputLineTokens.length == 2) {
//                if (camp.computer.construct.Type.has(featureTagToken)) {
//                    featureType = Feature.TypeId.CUSTOM_CONSTRUCT;
//                }
//            } else if (inputLineTokens.length == 3) {
//                String featureTypeToken = inputLineTokens[2];
//                if (featureTypeToken.equals("text")) {
//                    featureType = Feature.TypeId.TEXT;
//                } else if (featureTypeToken.equals("list")) {
//                    featureType = Feature.TypeId.LIST;
//                } else {
//                    if (camp.computer.construct.Type.has(featureTagToken)) {
//                        featureType = Feature.TypeId.CUSTOM_CONSTRUCT;
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
//                System.out.println("added feature '" + feature.address + "' of types '" + feature.types + "' (" + currentConcept.features.size() + ")");
//            } else {
//                // Print response
//                System.out.println("error: bad feature syntax");
//            }


        } else if (Address.isStructure(context.address)) {

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
            this.context.expressionTimeline.add(inputLine);

            // Store object
            Context context = new Context();
            context.expression = inputLine;

            if (context.expression.startsWith("import file")) {
                importFileTask(context);
            } else if (context.expression.startsWith("start")) {
                startProcessTask(context);
            } else if (context.expression.startsWith("stop")) {
                stopProcessTask(context);
            } else if (context.expression.startsWith("do")) {
                doProcessTask(context);
            }

            // <VERSION_CONTROL>
            else if (context.expression.startsWith("save")) {
                saveConstructVersion(context);
            } else if (context.expression.startsWith("restore")) {
                restoreConstructVersion(context);
            }
            // </VERSION_CONTROL>

            // <REFACTOR>
            else if (context.expression.startsWith("request configuration")) {
                addConfigurationTask(context);
            }
            // </REFACTOR>
            else if (context.expression.startsWith("new")) {
                createConstructTask(context);
            } else if (context.expression.startsWith("browse")) {
                browseConstructsTask(context);
            } else if (context.expression.startsWith("request")) {
                addConstructTask(context);
            } else if (context.expression.startsWith("list")) {
                listConstructsTask(context);
                /*
                listProjectsTask();
                listDevicesTask();
                listPortsTask(object);
                listPathsTask();
                */
            } else if (context.expression.startsWith("view")) {
                describeWorkspaceTask(context);
            } else if (context.expression.startsWith("describe")) {
                describeConstructTask(context);
            } else if (context.expression.startsWith("edit")) {
                editConstructTask(context);
            } else if (context.expression.startsWith("remove")) {
                removeConstructTask(context);
            }
            // <REFACTOR>
            else if (context.expression.startsWith("set configuration")) {
                setConfigurationTask(context);
            } else if (context.expression.startsWith("set path configuration")) {
                setPathConfigurationTask(context);
            }
            // </REFACTOR>
            else if (context.expression.startsWith("set")) {
                setConstructVariable(context);
            } else if (context.expression.startsWith("solve")) {
                solvePathConfigurationTask(context);
            } else if (context.expression.startsWith("exit")) {
                exitTask(context);
            }

        }

    }

    // <REFACTOR>
    // TODO: Create "Command" class with command (1) keywords and (2) task to handle command.

    public void importFileTask(Context context) {
        // TODO: Change argument to "Context object" (temporary cache/manager)

        // TODO: Lookup object.clone("expression")
        String[] inputLineTokens = context.expression.split("[ ]+");

        String inputFilePath = inputLineTokens[2];

        new LoadBuildFileTask().execute(inputFilePath);

    }

    public void startProcessTask(Context context) {
        // TODO: Lookup object.clone("expression")
        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            workspace.operationConstruct = new OperationConstruct();

        } else if (inputLineTokens.length > 1) {

            String address = inputLineTokens[1];
//            if (address.startsWith("\"") && address.endsWith("\"")) {

//            String address = address.substring(1, address.length() - 1);
            String title = String.valueOf(address);

            workspace.operationConstruct = new OperationConstruct();
            workspace.operationConstruct.title = title;

//            }

        }

//        System.out.println("✔ edit project " + workspace.projectConstruct.uid);
//        System.out.println("> start " + workspace.operationConstruct.uid);
    }

    public void stopProcessTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            operationConstructs.add(workspace.operationConstruct);

//            System.out.println("✔ stop " + workspace.operationConstruct.uid + " (" + workspace.operationConstruct.operations.size() + " operations)");

            // Reset process construct
            workspace.operationConstruct = null;

        }

    }

    public void doProcessTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

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

        // TODO: Lookup object.clone("expression")
        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            // TODO: Add anonymous construct

        } else if (inputLineTokens.length == 2) {

            String constructTypeToken = inputLineTokens[1];

            if (constructTypeToken.equals("project")) {

                ProjectConstruct projectConstruct = new ProjectConstruct();
//                workspace.projectConstructs.request(projectConstruct);
//                workspace.lastProjectConstruct = projectConstruct; // Marketplace reference to last-created project

                System.out.println("✔ request project(uid:" + projectConstruct.uid + ") to workspace");

            } else if (constructTypeToken.equals("device")) {

//                // TODO: Ensure edit construct is a device!
//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == ProjectConstruct.class) {
//
//                    ProjectConstruct projectConstruct = (ProjectConstruct) workspace.OLD_construct;

                DeviceConstruct deviceConstruct = new DeviceConstruct();
//                projectConstruct.deviceConstructs.request(deviceConstruct);
//                workspace.lastDeviceConstruct = deviceConstruct; // Marketplace reference to last-created device

                System.out.println("✔ request device(uid:" + deviceConstruct.uid + ")");
//                }

            } else if (constructTypeToken.equals("port")) {

//                // TODO: Ensure edit OLD_construct is a device!
//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {
//
//                    DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;

                PortConstruct portConstruct = new PortConstruct();
//                    deviceConstruct.portConstructs.request(portConstruct);
//                    workspace.lastPortConstruct = portConstruct; // Marketplace reference to last-created port

                System.out.println("✔ request port(uid:" + portConstruct.uid + ")");
//                }

            } else if (constructTypeToken.equals("path")) {

//                // TODO: Ensure edit OLD_construct is a device!
//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == ProjectConstruct.class) {
//
//                    ProjectConstruct projectConstruct = (ProjectConstruct) workspace.OLD_construct;

                PathConstruct pathConstruct = new PathConstruct();
//                    projectConstruct.pathConstructs.request(pathConstruct);
//                    workspace.lastPathConstruct = pathConstruct; // Marketplace reference to last-created port
//
                System.out.println("✔ request path(uid:" + pathConstruct.uid + ")");
//                }

            } else if (constructTypeToken.equals("task")) {

//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {
//
//                    DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;

                TaskConstruct taskConstruct = new TaskConstruct();
//                    deviceConstruct.controllerConstruct.taskConstructs.request(taskConstruct);
//
//                    // Marketplace reference to last-created device
//                    workspace.lastTaskConstruct = taskConstruct;

                System.out.println("✔ request task " + taskConstruct.uid);

//                }

            }

//            System.out.println("✔ request " + constructTypeToken + " " + projectConstruct.uid);

        }
//        else if (inputLineTokens.length > 2) {
//
//            String constructTypeToken = inputLineTokens[1];
//            String constructTitleString = inputLineTokens[2];
//
//            if (constructTypeToken.equals("project")) {
//
//                ProjectConstruct projectConstruct = new ProjectConstruct();
//                projectConstruct.address = constructTitleString;
//                workspace.projectConstructs.request(projectConstruct);
//                workspace.lastProjectConstruct = projectConstruct; // Marketplace reference to last-created project
//
//            } else if (constructTypeToken.equals("device")) {
//
////                // TODO: Ensure edit OLD_construct is a project!
////                if (workspace.projectConstruct != null) {
////
////                    DeviceConstruct deviceConstruct = new DeviceConstruct();
////                    deviceConstruct.address = constructTitleString;
////                    workspace.projectConstruct.deviceConstructs.request(deviceConstruct);
////                    workspace.lastDeviceConstruct = deviceConstruct; // Marketplace reference to last-created port
////
////                    System.out.println("✔ request device " + deviceConstruct.uid);
////                }
//
//                // TODO: Ensure edit OLD_construct is a device!
//                if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == ProjectConstruct.class) {
//
//                    ProjectConstruct projectConstruct = (ProjectConstruct) workspace.OLD_construct;
//
//                    DeviceConstruct deviceConstruct = new DeviceConstruct();
//                    projectConstruct.deviceConstructs.request(deviceConstruct);
//                    workspace.lastDeviceConstruct = deviceConstruct; // Marketplace reference to last-created device
//
//                    deviceConstruct.address = constructTitleString;
//
//                    System.out.println("✔ request device " + deviceConstruct.uid + " to project " + projectConstruct.uid);
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
//                    portConstruct.address = constructTitleString;
//                    deviceConstruct.portConstructs.request(portConstruct);
//                    workspace.lastPortConstruct = portConstruct; // Marketplace reference to last-created port
//
//                    System.out.println("✔ request port " + portConstruct.uid + " on device " + deviceConstruct.uid);
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
//                    taskConstruct.address = constructTitleString;
//                    deviceConstruct.controllerConstruct.taskConstructs.request(taskConstruct);
//
//                    // Marketplace reference to last-created device
//                    workspace.lastTaskConstruct = taskConstruct;
//
//                    System.out.println("✔ request task " + taskConstruct.uid + " to device " + deviceConstruct.uid);
//
//                }
//
//            }
//
////            System.out.println("✔ request " + constructTypeToken + " " + projectConstruct.uid);
//
//        }

    }

    public void browseConstructsTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

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
    // request <OLD_construct-types-address> <OLD_construct-instance-address>
    //
    // Examples:
    // - request project
    // - request project "my-project"
    public void addConstructTask(Context context) {

        // TODO: Lookup object.clone("expression")
        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

            // TODO: Add anonymous OLD_construct

        } else if (inputLineTokens.length == 2) {

            String constructTypeToken = inputLineTokens[1];

            if (constructTypeToken.equals("project")) {

                // TODO: Instantiate container copy of specified project (from Repository/DB)

                // request project uuid:<uuid>

                String constructIdentifierToken = inputLineTokens[2].split(":")[1];
                UUID constructUuid = UUID.fromString(constructIdentifierToken);
                Construct_v1 construct = Repository.clone(constructUuid); // TODO: Return a COPY/CLONE of the project
                // TODO: request the project to the workspace (so it can be deployed)

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

//            System.out.println("✔ request " + constructTypeToken + " " + projectConstruct.uid);

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
//                    deviceConstruct.address = constructTitleString;
//                    workspace.projectConstruct.deviceConstructs.request(deviceConstruct);
//                    workspace.lastDeviceConstruct = deviceConstruct; // Marketplace reference to last-created port
//
//                    System.out.println("✔ request device " + deviceConstruct.uid);
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

//            System.out.println("✔ request " + constructTypeToken + " " + projectConstruct.uid);

        }

    }

    /**
     * <strong>Examples</strong>
     * {@code list <OLD_construct-types>}
     *
     * @param context
     */
    public void listConstructsTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

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

        String[] inputLineTokens = context.expression.split("[ ]+");

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

//        String[] inputLineTokens = object.expression.split("[ ]+");
//
//        if (inputLineTokens.length == 1) {
//
//            // TODO: List all constructs!
//
//            Type OLD_construct = workspace.OLD_construct;
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
//            Type OLD_construct = Manager_v1.clone(constructAddressString);
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
        // TODO: Lookup object.clone("expression")
        String[] inputLineTokens = context.expression.split("[ ]+");

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

            // No port was found with the specified address (UID, UUID, address, index)

        }
    }

    /**
     * Removes the {@code Type} with the specified address from the {@code Manager_v1}.
     *
     * @param context
     */
    public void removeConstructTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

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
//        // TODO: Lookup object.clone("expression")
//        String[] inputLineTokens = object.expression.split("[ ]+");
//
//        Type OLD_construct = null;
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
//        // TODO: Lookup object.clone("expression")
//        String[] inputLineTokens = object.expression.split("[ ]+");
//
//        Type deviceConstruct = null;
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
//            // No port was found with the specified address (UID, UUID, address, index)
//
//        }
//
//    }
//
//    public void editPortTask(Context object) {
//
//        // TODO: Lookup object.clone("expression")
//        String[] inputLineTokens = object.expression.split("[ ]+");
//
//        Type portConstruct = null;
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
//            // No port was found with the specified address (UID, UUID, address, index)
//
//        }
//
//    }
//
//    public void editPathTask(Context object) {
//        // TODO: Lookup object.clone("expression")
//        String[] inputLineTokens = object.expression.split("[ ]+");
//
//        Type pathConstruct = null;
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
//            // No port was found with the specified address (UID, UUID, address, index)
//
//        }
//    }
//
//    public void editTaskTask(Context object) {
//        // TODO: Change argument to "Context object" (temporary cache/manager)
//
//        // TODO: Lookup object.clone("expression")
//        String[] inputLineTokens = object.expression.split("[ ]+");
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
//        // TODO: Lookup object.clone("expression")
//        if (workspace.projectConstruct != null) {
//
//            String[] inputLineTokens = object.expression.split("[ ]+");
//
//            String inputProjectTitle = inputLineTokens[3];
//
//            workspace.projectConstruct.address = inputProjectTitle;
//
//            System.out.println("project address changed to " + inputProjectTitle);
//        }
//
//    }

    // e.g., request configuration uart(tx);output;ttl,cmos
    public void addConfigurationTask(Context context) {

        // TODO: Parse "bus(line)" value string pattern to create bus and lines.

        String[] inputLineTokens = context.expression.split("[ ]+");

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
//        // TODO: Lookup object.clone("expression")
//        String[] inputLineTokens = object.expression.split("[ ]+");
//
//        String configurationOptionString = inputLineTokens[2];
//
//        PortConfigurationConstraint.Mode mode = PortConfigurationConstraint.Mode.NONE;
//        PortConfigurationConstraint.Direction direction = null;
//        PortConfigurationConstraint.Voltage voltage = null;
//
//        // Separate configurations string into tokens separated by ";" substring, each an expression representing an
//        // attribute state assignment. Separate each attribute assignment by ":", into the attribute address and
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

        // request path <address>
        // edit path
        // set source-port[OLD_construct-types] uid:34
        // set target-port[OLD_construct-types] uid:34

//        if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {
//        if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == PathConstruct.class) {

//            DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;

        String[] inputLineTokens = context.expression.split("[ ]+");

        // TODO: Parse address token (for index, UID, UUID; address/key/address)

        PathConstruct pathConstruct = (PathConstruct) Manager_v1.get(inputLineTokens[1]);

        /**
         * "solve path [uid]"
         */

        // TODO: Resolve set of available configurations for path based on compatible configurations of ports in the path.

        // Iterate through configurations for of source port in path. For each source port configurations, check
        // the other ports' configurations for compatibility; then request each compatible configurations to a list of
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

        // TODO: Lookup object.clone("expression")
        String[] inputLineTokens = context.expression.split("[ ]+");

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

        // TODO: Lookup object.clone("expression")
        String[] inputLineTokens = context.expression.split("[ ]+");

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
//            workspace.deviceConstruct.controllerConstruct.taskConstructs.request(taskConstruct);
//
//            // Marketplace reference to last-created device
//            workspace.lastTaskConstruct = taskConstruct;
//
//            System.out.println("✔ request task " + taskConstruct.uid + " to device " + workspace.deviceConstruct.uid);
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

        // TODO: Lookup object.clone("expression")
        String[] inputLineTokens = object.expression.split("[ ]+");

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
