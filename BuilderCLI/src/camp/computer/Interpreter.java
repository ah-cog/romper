package camp.computer;

import java.util.HashMap;
import java.util.Scanner;

import camp.computer.construct.Address;
import camp.computer.construct.Structure;
import camp.computer.construct.Type;
import camp.computer.construct.Error;
import camp.computer.construct.Expression;
import camp.computer.construct.Feature;
import camp.computer.construct.Reference;
import camp.computer.construct.Configuration;
import camp.computer.platform.LoadBuildFileTask;
import camp.computer.util.List;
import camp.computer.util.Pair;
import camp.computer.util.console.Color;
import camp.computer.workspace.Manager;

// TODO: PORTS CAN BE "WIRELESS" AND SPREAD OUT ACROSS BOARDS JUST LIKE CLAY BOARDS CAN BE SPREAD OUT IN SPACE.
public class Interpreter {

    // <SETTINGS>
    public static boolean ENABLE_VERBOSE_OUTPUT = false;
    // </SETTINGS>

    private static Interpreter instance = null;

    private Context context = null;

    private Interpreter() {

        Interpreter.instance = this;

        // Instantiate primitive types
        Type.requestOrCreate("type"); // TypeId.add("type");
        Type.requestOrCreate("none");
        Type.requestOrCreate("number");
        Type.requestOrCreate("text");
        Type.requestOrCreate("list");
        // "any" isn't actually represented with a type, since it's a constraint, not a type. It's
        // encoded in the way Types and Structure are represented in memory.

//        // Instantiate primitive concepts
//        if (!Type.exists("none")) {
//            Type noneType = Type.request("none");
//        }
//
//        /*
//        if (!Type.exists(Type.request("number"))) {
//            Type numberConcept = Type.request(Type.request("number"));
//        }
//        */
//
//        if (!Type.exists("text")) {
//            Type textType = Type.request("text");
//        }
//
//        if (!Type.exists("list")) {
//            Type listType = Type.request("list");
//        }
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

        // Store context in history
        // Context context = new Context();
        if (context == null) {
            context = new Context();
        }
        Expression expression = Context.setExpression(context, inputLine);

        // Save line in history
        context.expressionTimeline.add(inputLine);

        // open
        // type, has, let
        // structure, set, add, remove
        // reference
        // list, search, describe
        // context
        // exit

        // TODO: list configurations available for a type
        // TODO: list compatible configurations

        if (context.expression.startsWith("open")) { // previously, "import file"
            importFileTask(context);
//            } else if (context.expression.startsWith("type")) {
        } else if (context.expression.matches("^type($|[ ]+.*)")) {
            typeTask(context);
        } else if (context.expression.matches("^has[ ]+.*")) { // TODO: has? !has
            hasTask(context);
        } else if (context.expression.matches("^let[ ]+.*")) { // TODO: let?, !list
            letTask(context);
//            letConfigurationTask(context);
        } else if (context.expression.matches("^(structure|struct)[ ]+.*")) {
            structureTask(context);
        } else if (context.expression.matches("^set[ ]+.*")) {
            setTask(context);
        } else if (context.expression.matches("^add[ ]+.*")) {
            addTask(context);
        } else if (context.expression.matches("^(remove|rem|rm)[ ]+.*")) {
            removeTask(context);
        } else if (context.expression.matches("^(reference|ref)[ ]+.*")) { // !link, link?
                                                                        // TODO: Callback to create a link in the active context/namespace.
            referenceTask(context);
        } else if (context.expression.matches("^list($|[ ]+.*)")) {
            listTask(context);
            // TODO: "previous" and "next" to traverse through segments of the list
        } else if (context.expression.matches("^search[ ]+.*")) {
            searchTask(context);
        } else if (context.expression.matches("^(describe|ds)[ ]+.*")) {
            describeTask(context);
        } else if (context.expression.matches("^context($|[ ]+.*)")) {
            privateContextTask(context);
        } else if (context.expression.equals("exit")) {
            exitTask(context);
        } else {

            // Attempts to evaluate expression as a type, structure, or reference

            if (context.references != null && context.references.containsKey(context.expression.split("[ ]+")[0])) { // REFACTOR
                                                                                                                  // e.g., "foo" -> port.id.9

                String referenceKey = context.expression.split("[ ]+")[0];
                Reference reference = context.references.get(referenceKey);

                // Update object
                context.address = reference;

                System.out.println(Color.ANSI_YELLOW + referenceKey + Color.ANSI_RESET + " -> " + reference.toColorString());

            } else if (Type.exists(context.expression.split("[ ]+")[0])) { // REFACTOR

                System.out.println("TODO: evaluate type");

            } else if (Expression.isAddress(context.expression)) {

                String[] tokens = context.expression.split("\\.");
                String identifierToken = tokens[0];
                String addressExpressionToken = tokens[1];
                String[] addressExpressionTokens = addressExpressionToken.split("=");
                String addressTypeToken = addressExpressionTokens[0];
                String addressToken = addressExpressionTokens[1];

                if (Type.exists(identifierToken)) {
                    if (addressTypeToken.equals("id")) {
                        long id = Long.parseLong(addressToken);
                        Address address = Manager.get(id);
                        if (address == null) {
                            System.out.println(Color.ANSI_RED + "Error: No data exists with id=" + id + Color.ANSI_RESET);
                        } else if (address.getClass() == Reference.class) {
                            Reference reference = (Reference) address;
                            Structure structure = (Structure) reference.object;
                            System.out.println(reference.toColorString());

                            // Update object
                            context.address = reference;
                        } else if (address.getClass() == Structure.class) {
                            Structure structure = (Structure) address;
                            System.out.println(structure.toColorString());

                            // Update object
                            context.address = structure;

                        } else if (address.getClass() == Type.class) {
                            Type type = (Type) address;
                            System.out.println(type.toColorString());

                            // Update object
                            context.address = type;
                        }
                    } else if (context.expression.contains("uuid:")) {
                        // TODO:
                    }
                }
            } else {
                System.out.println(Color.ANSI_RED + "Error: Unsupported expression." + Color.ANSI_RESET);
            }
        }
    }

    public void typeTask(Context context) {

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
            if (context.typeReferences.containsKey(type.identifier)) {
                type = context.typeReferences.get(type.identifier);
            } else {
                type = Type.request(typeToken);
            }

            System.out.println(type.toColorString());

            // Update object
            context.address = type;

            // TODO: Factor this into a function in Context (to automate tracking of most-recent type)
            context.typeReferences.put(typeToken, type);

        }
    }

    // e.g.,
    // structure port
    // [DELETED] structure port my-port
    public void structureTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

        // Defaults
        String featureIdentifierToken = null;

        // Determine address
        if (inputLineTokens.length >= 2) {
            featureIdentifierToken = inputLineTokens[1];

            Type type = null;
            if (Type.exists(featureIdentifierToken)) {
                // TODO: Factor this into a function in Context (to automate tracking of most-recent type)
                type = context.typeReferences.get(featureIdentifierToken);
            }

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

                    // Update context reference corresponding to the type
                    context.references.put(structure.type.identifier, constructReference);

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
    // link device ir-rangefinder-ttl
    public void referenceTask(Context context) {

        String[] inputLineTokens = context.expression.split("[ ]+");

        // Defaults
        String featureIdentifierToken = null;

        // Determine address
        if (inputLineTokens.length >= 3) {
            featureIdentifierToken = inputLineTokens[1];
            String referenceIdentifier = inputLineTokens[2];

//            Type type = null;
//            // TODO: Check if type identifier token is an EXACT type address. Otherwise, get indexed, or default.
//            if (Type.exists(featureIdentifierToken)) {
//                // TODO: Factor this into a function in Context (to automate tracking of most-recent type)
//                type = context.typeReferences.get(featureIdentifierToken);
//            }
//            Structure structure = null;
            Reference reference = null;
            // TODO: Check if structure identifier token is an EXACT structure address. Otherwise, get indexed, or default.
//            if (Type.exists(featureIdentifierToken)) {
                // TODO: Factor this into a function in Context (to automate tracking of most-recent structure)
            // Resolve the referenced type or structure.
            // 1. Check if the referenced entity is a reference (i.e., in the context).
            Reference featureObject = context.references.get(featureIdentifierToken);
            if (featureObject != null) {
                if (Reference.isStructure(featureObject)) {
                    Structure structure = (Structure) featureObject.object;
                    reference = Reference.create(structure);
                } else if (Reference.isType(featureObject)) {
                    Type type = (Type) featureObject.object;
                    reference = Reference.create(type);
                }
            }
            // 2. Check if the referenced entity is a <em>type</em>
            if (reference == null) {
                Type type = Type.request(featureIdentifierToken);
                if (type != null) {
//                Type type = Type.request(featureIdentifierToken);
                    reference = Reference.create(type);
                }
            }
            // 3. Check if the referenced entity is a <em>structure</em>
            if (reference == null) {
                Structure structure = Structure.request(featureIdentifierToken);
                if (structure != null) {
//                Structure structure = Structure.request(featureIdentifierToken);
                    reference = Reference.create(structure);
                }
            }

            // Create the reference and add it to the context
//            Reference reference = Reference.create(structure);
            context.references.put(referenceIdentifier, reference);

            // Feedback
            System.out.println(Color.ANSI_YELLOW + referenceIdentifier + Color.ANSI_RESET + " -> " + reference.toColorString());
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
                        featureTypes = new List<>();
                    }

//                    if (featureTypeToken.equals("text")) {
//                        featureTypeIds.request(Type.request(featureTypeToken));
//                    } else
                    if (featureTypeToken.equals("list")) {
                        featureTypes.add(Type.request(featureTypeToken));
                        if (Type.exists(featureIdentifier)) {
                            if (listTypes == null) {
                                listTypes = new List<>();
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
                            featureTypes = new List<>();
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
                            featureTypes = new List<>();
                            featureTypes.add(Type.request("text"));
                            hasDomainList = true;
                        } else if (isConstructContent) {
                            // TODO: Use generic "construct" types or set specific if there's only one
                            if (isSingletonList) {
                                // e.g., has mode : list
                                // e.g., has source : port
                                featureTypes = new List<>();
                                featureTypes.add(Type.request(constraintTokens[0]));
                                // TODO: if 'text' or other construct only, then set types to featureType = TEXT and hasDomainList = false
                                if (featureTypes.contains(Type.request("list"))) {
//                                    listTypes.request(Type.request("any"));
                                }
                            } else {
                                // e.g., has my-feature : list, port
                                featureTypes = new List<>();
                                for (int i = 0; i < constraintTokens.length; i++) {
                                    featureTypes.add(Type.request(constraintTokens[i]));
                                }
                            }
                        } else if (hasTextContent && hasConstructContent) {
                            featureTypes = new List<>();
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
                                listTypes = new List<>();
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
                                        listTypes = new List<>();
                                    }
                                    // Add the list types constraint
                                    listTypes.add(Type.request(constraintTokens[0]));
                                }
                            } else {
                                // e.g., has ports-and-paths list : port, path
                                // TODO: Convert listType to list and request all listed construct types to the types list
//                                listTypes.request(Type.request("construct"));
                                if (listTypes == null) {
                                    listTypes = new List<>();
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
                                listTypes = new List<>();
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
                                featureDomain = new List<>();
                            }
                            Structure structure = Structure.request(constraintToken);
                            if (structure == null) {
                                structure = Structure.create(constraintToken);
                            }
                            featureDomain.add(structure);
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
//                Type baseType = Address.getType(context.typeReferences.request(featureIdentifier));
                Type replacementType = Type.request(baseType, featureIdentifier, feature);
                context.address = replacementType;

                // TODO: Factor this into a function in Context (to automate tracking of most-recent type)
                context.typeReferences.put(replacementType.identifier, replacementType);

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
                HashMap<String, Feature> currentConstructFeatures = currentStructure.type.features; // (HashMap<String, Feature>) currentStructure.object;

                Structure currentFeatureStructure = Structure.getStates(currentStructure).get(featureIdentifier);

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
                            && !Structure.getFeature(currentStructure, featureIdentifier).types.contains(replacementFeatureStructure.type)) {
                        // TODO: Check types!
                        System.out.println(Error.get("Feature " + featureIdentifier + " doesn't support type " + replacementFeatureStructure.type));
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
                    Structure featureStructure = Structure.getStates(currentStructure).get(featureIdentifier);
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
                HashMap<String, Feature> currentConstructFeatures = currentStructure.type.features; // (HashMap<String, Feature>) currentStructure.object;

                // Check if feature is valid. If not, show error.
                if (!currentConstructFeatures.containsKey(featureIdentifier)) {
                    System.out.println(Error.get(featureIdentifier + " is not a feature."));
                    return;
                }

                Structure currentFeatureStructure = Structure.getStates(currentStructure).get(featureIdentifier);

                if ((currentConstructFeatures.get(featureIdentifier).types.size() == 1 && currentConstructFeatures.get(featureIdentifier).types.contains(Type.request("list")))
                        || currentFeatureStructure.type== Type.request("list")) {

                    Structure additionalFeatureStructure = null;
                    if (context.references.containsKey(stateExpression)) {
                        // TODO: Check for type error!
                        additionalFeatureStructure = (Structure) context.references.get(stateExpression).object;
                    } else {
                        additionalFeatureStructure = Structure.request(stateExpression); // replacementStructure
                    }

                    List requestedConstructList = new List();
                    if (currentFeatureStructure.type== Type.request("list")) {
                        requestedConstructList.addAll(((List) currentFeatureStructure.object));
                    }
                    requestedConstructList.add(additionalFeatureStructure);

                    // TODO: Search for list!
                    Structure replacementFeatureStructure = Structure.request(requestedConstructList);
                    System.out.println(replacementFeatureStructure.toColorString());

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
//                        System.out.println("reference " + currentStructure.type.toColorString() + " (id: " + context.address.uid + ") -> construct " + currentStructure.type.toColorString() + " (id: " + currentStructure.uid + ")" + " (uuid: " + currentStructure.uuid + ")");
                        System.out.println(currentStructure.type.identifier + ".id=" + context.address.uid + " -> " + currentStructure.type.identifier + ".id=" + currentStructure.uid);
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

                if (Expression.isAddress(typeToken)) {

                    // String[] tokens = context.expression.split("\\.");
//                    String[] tokens = typeToken.split("\\.");
//                    String typeIdentifierToken = tokens[0];
//                    String addressTypeToken = tokens[1];
//                    String addressToken = tokens[2];
                    String[] expressionTokens = typeToken.split("\\.");
                    String typeIdentifierToken = expressionTokens[0]; // e.g., "port"
                    String identifierTypeToken = expressionTokens[1].split("=")[0]; // e.g., "id"
                    long uid = Long.parseLong(expressionTokens[1].split("=")[1]); // e.g., "9"

                    if (identifierTypeToken.equals("id")) {
                        Structure structure = null;
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

//                        if (structure != null && structure.type== Type.request(typeIdentifierToken)) {
                        if (structure != null && structure.type.identifier.equals(typeIdentifierToken)) {
                            if (structure.type == Type.request("type")) {
                                System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET + " primistructure");
                            } else if (structure.type == Type.request("none")) {
                                System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET + " default primitive structure that represents absence of any data structure");
                            } else if (structure.type == Type.request("number")) {
                                System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET + " primitive structure representing a sequence of characters");
                            } else if (structure.type == Type.request("text")) {
                                System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET + " primitive structure representing a sequence of characters");
                            } else if (structure.type == Type.request("list")) {
                                System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET + " primitive structure representing a list that contains constructs");
                            } else {

//                                System.out.println(Color.ANSI_BLUE + structure.type.address + Color.ANSI_RESET);
                                System.out.println(structure.toColorString());

                                HashMap<String, Feature> features = structure.type.features; // (HashMap<String, Feature>) structure.object;
                                HashMap<String, Structure> states = Structure.getStates(structure); // (HashMap<String, Structure>) structure.states;
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
                    } else if (identifierTypeToken.equals("uuid")) {

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
                    String[] expressionTokens = typeToken.split("\\.");
                    String typeIdentifierToken = expressionTokens[0]; // e.g., "port"
                    String identifierTypeToken = expressionTokens[1].split("=")[0]; // e.g., "id"
                    long uid = Long.parseLong(expressionTokens[1].split("=")[1]); // e.g., "9"

                        Type typeId = Type.request(typeToken);

                        if (typeId == Type.request("type")) {
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

                            // Determine the type to describe for the type
                            Type type = null;
                            if (context.typeReferences.containsKey(typeId)) {
                                type = context.typeReferences.get(typeId);
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
                    if (((Structure) (((Reference) referenceList.get(j)).object)).type== typeDefault) {
                        typeReferenceCount++;
                    }
                }
                System.out.println(Color.ANSI_BLUE + typeIdList.get(i).identifier + Color.ANSI_RESET + " (count: " + typeReferenceCount + ")");
            }

        } else if (inputLineTokens.length >= 2) {

            String typeToken = inputLineTokens[1]; // "port" or "port (id: 3)"

            if (Expression.isAddress(typeToken)) {

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

                    if (structure != null && structure.type== Type.request(typeToken)) {
                        if (structure.type== Type.request("none")) {

                            System.out.println("REFERENCE (id:X) -> " + structure);

                        } else if (structure.type== Type.request("number")) {

                        } else if (structure.type== Type.request("text")) {

//                            String feature = (String) structure.object;
                            System.out.println("REFERENCE (id:X) -> " + structure);

                        } else if (structure.type== Type.request("list")) {

                        } else {

                            System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET);

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

            if (Expression.isAddress(typeToken)) {

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

                    if (structure != null && structure.type== Type.request(typeIdentifierToken)) {
                        if (structure.type== Type.request("none")) {

                            System.out.println("REFERENCE (id:X) -> " + structure);

                        } else if (structure.type== Type.request("number")) {

                        } else if (structure.type== Type.request("text")) {

//                            String feature = (String) structure.object;
                            System.out.println("REFERENCE (id:X) -> " + structure);

                        } else if (structure.type== Type.request("list")) {

                        } else {

                            System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET);

                            HashMap<String, Feature> features = structure.type.features; // (HashMap<String, Feature>) structure.object;
                            HashMap<String, Structure> states = Structure.getStates(structure); // HashMap<String, Structure>) structure.states;
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
    // e.g.,
    // list
    // list type [<identifier>]
    // list structure [<identifier>]
    // list link [<identifier>]
    public void listTask(Context context) { // previously, viewTask

        String[] inputLineTokens = context.expression.split("[ ]+");

        if (inputLineTokens.length == 1) {

//            List<Type> typeList = Type.list();
//            for (int i = 0; i < typeList.size(); i++) {
//                List<Structure> structureList = Manager.getStructureList(typeList.get(i));
//                // System.out.println("(id: " + typeIdList.request(i).uid + ") " + Application.ANSI_BLUE + typeIdList.request(i).address + Application.ANSI_RESET + " (" + structureList.size() + ") (uuid: " + typeIdList.request(i).uuid + ")");
//                System.out.println(Color.ANSI_BLUE + typeList.get(i).identifier + Color.ANSI_RESET + " (count: " + structureList.size() + ")");
//            }

            System.out.println("list <type|structure|reference> [<identifier>]");

        } else if (inputLineTokens.length >= 2) {

            String constructToken = inputLineTokens[1]; // i.e., "type", "structure", "link"

            if (constructToken.equals("type")) {

                if (inputLineTokens.length == 2) {
                    for (Type type : Type.list()) {
                        System.out.println(Color.ANSI_BLUE + type.identifier + Color.ANSI_RESET + " (count: " + Type.count(type) + ")");
                    }
                } else if (inputLineTokens.length >= 3) {

                    // TODO: print "identifier" next to the default type for each identifier
                    // TODO: separate namespace contexts from data. create namespaces then link things into it from the central repository.

                    String typeToken = inputLineTokens[2]; // "port" or "port (id: 3)"
                    Type type = Type.request(typeToken);
                    if (type != null) {
                        List<Type> types = Manager.get(Type.class);
                        for (int i = 0; i < types.size(); i++) {
                            if (types.get(i).identifier.equals(typeToken)) {
                                System.out.print(types.get(i).toColorString());
                                if (types.get(i).features == null) {
                                    System.out.print(" [identifier]");
                                }
                                System.out.println();
                            }
                        }
                    }

                }

            } else if (constructToken.equals("structure") || constructToken.equals("struct")) {

                if (inputLineTokens.length == 2) {
                    for (Type type : Type.list()) {
                        System.out.println(Color.ANSI_BLUE + type.identifier + Color.ANSI_RESET + " (count: " + Structure.count(type) + ")");
                    }
                } else if (inputLineTokens.length >= 3) {

                    // TODO: print "identifier" next to the default type for each identifier
                    // TODO: separate namespace contexts from data. create namespaces then link things into it from the central repository.

//                    String typeToken = inputLineTokens[2]; // "port" or "port (id: 3)"
//                    Type type = Type.request(typeToken);
//                    if (type != null) {
//                        List<Type> types = Manager.get(Type.class);
//                        for (int i = 0; i < types.size(); i++) {
//                            if (types.get(i).identifier.equals(typeToken)) {
//                                System.out.print(types.get(i).toColorString());
//                                if (types.get(i).features == null) {
//                                    System.out.print(" [identifier]");
//                                }
//                                System.out.println();
//                            }
//                        }
//                    }
//
//                }

                    String typeToken = inputLineTokens[2]; // "port" or "port (id: 3)"

                    if (Expression.isAddress(typeToken)) {

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

//                            if (address.getClass() == Reference.class) {
//                                Reference reference = (Reference) address;
//                                structure = (Structure) reference.object;
//                            } else if (address.getClass() == Structure.class) {
                                structure = (Structure) address;
//                        } else if (address.getClass() == Type.class) {
//                            System.out.println("Error: The UID is for a type.");
//                            //                                Type type = (Type) address;
//                            //                                System.out.println("Found " + type.types + " with UID " + uid);
//                            }

                            if (structure != null && structure.type == Type.request(typeIdentifierToken)) {
                                if (structure.type == Type.request("none")) {

//                            System.out.println("REFERENCE (id:X) -> " + structure);
                                    System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET + " default primitive structure that represents absence of any data structure");

                                } else if (structure.type == Type.request("number")) {

                                } else if (structure.type == Type.request("text")) {

//                            System.out.println("REFERENCE (id:X) -> " + structure);
                                    System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET + " primitive structure representing a sequence of characters");

                                } else if (structure.type == Type.request("list")) {

                                    System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET + " primitive structure representing a list that contains constructs");

                                } else {

                                    System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET);

                                    HashMap<String, Feature> features = structure.type.features; // (HashMap<String, Feature>) structure.object;
                                    HashMap<String, Structure> states = Structure.getStates(structure); // HashMap<String, Structure>) structure.states;
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

                        List<Structure> structureList = Structure.list(Type.request(typeToken));
                        for (int i = 0; i < structureList.size(); i++) {
                            System.out.println(structureList.get(i).toColorString());
                        }

                    }
                }

            } else if (constructToken.matches("^(reference|ref)$")) {

                if (inputLineTokens.length == 2) {
                    // List context references
                    for (String referenceKey : context.references.keySet()) {
                        Reference reference = context.references.get(referenceKey);
                        // Structure structure = (Structure) reference.object;
                        System.out.println(Color.ANSI_YELLOW + referenceKey + Color.ANSI_RESET + " -> " + reference.toColorString());
                    }

                    // Type References
                    // TODO: Update this so it actually uses references to types!
                    for (String referenceKey : context.typeReferences.keySet()) {
                        Type reference = context.typeReferences.get(referenceKey);
                        // Structure structure = (Structure) reference.object;
                        System.out.println(Color.ANSI_YELLOW + referenceKey + Color.ANSI_RESET + " -> " + reference.toColorString());
                    }
//                    // List anonymous references
//                    List<Reference> referenceList = Manager.get(Reference.class);
//                    for (Reference reference : referenceList) {
//                        if (!context.references.containsValue(reference)) {
////                    System.out.println(Color.ANSI_YELLOW + "(anonymous)" + Color.ANSI_RESET + " " + reference.toColorString());
//                            System.out.println(reference.toColorString());
//                        }
//                    }
                } else if (inputLineTokens.length >= 3) {

                    String typeToken = inputLineTokens[2]; // "port" or "port (id: 3)"

                    if (Expression.isAddress(typeToken)) {

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

                            if (structure != null && structure.type== Type.request(typeIdentifierToken)) {
                                if (structure.type== Type.request("none")) {

                                    System.out.println("REFERENCE (id:X) -> " + structure);

                                } else if (structure.type== Type.request("number")) {

                                } else if (structure.type== Type.request("text")) {

//                            String feature = (String) structure.object;
                                    System.out.println("REFERENCE (id:X) -> " + structure);

                                } else if (structure.type== Type.request("list")) {

                                } else {

                                    System.out.println(Color.ANSI_BLUE + structure.type.identifier + Color.ANSI_RESET);

                                    HashMap<String, Feature> features = structure.type.features; // (HashMap<String, Feature>) structure.object;
                                    HashMap<String, Structure> states = Structure.getStates(structure); // HashMap<String, Structure>) structure.states;
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

            } else {

                System.out.println(Error.get("Invalid construct identifier \"" + constructToken + "\""));
                return;

            }
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

//            // Defaults
//            String featureTagToken = null;
//
//            // Determine address
//            if (inputLineTokens.length >= 2) {
//                featureTagToken = inputLineTokens[1];
//            }

            // Parse constraint
            String configurationString = context.expression.substring(context.expression.indexOf(" ") + 1); // consume whitespace after "let"
            String[] configurationStrings = configurationString.split("[ ]*;[ ]*");

            // Intermediate storage of feature states
            List<Pair<String, List<Structure>>> featureStateSets = new List<>();

            System.out.println("let parameters (" + configurationStrings.length + "): " + configurationString);
            for (int i = 0; i < configurationStrings.length; i++) {
//                System.out.println("\t" + letParameterTokens[i].trim());

                String[] configurationStateListString = configurationStrings[i].split("[ ]*:[ ]*");

                String featureIdentifier = configurationStateListString[0];
                System.out.print("\t" + featureIdentifier + ": ");
//                System.out.print("\t" + letParameter[1] + " ");

                String[] featureStateStrings = configurationStateListString[1].split("[ ]*,[ ]*");
                for (int j = 0; j < featureStateStrings.length; j++) {
                    Structure featureState = Structure.request(featureStateStrings[j]);
                    if (featureState == null) {
                        System.out.println(Error.get("Invalid construct provided with 'let'."));
                        return;
                    }
                    System.out.print(featureState + " ");
                }
                System.out.println();

                // Save variable's value set for the configuration constraint
                List<Structure> featureStateSet = new List<>();
                for (int j = 0; j < featureStateStrings.length; j++) {
                    Structure featureState = Structure.request(featureStateStrings[j]);
                    featureStateSet.add(featureState);
                }
                featureStateSets.add(new Pair<>(featureIdentifier, featureStateSet));
            }

            // TODO: Store configuration domain/constraint for type feature assignment (not allowed for construct?)
            // TODO: ^^ this is analogous to the feature-level domain, but for multiple features

            // Add VariableMap Option/Configuration
            Type type = (Type) context.address;
            if (type.configurations == null) {
                type.configurations = new List<>();
            }
            Configuration configuration = new Configuration(featureStateSets);

            boolean hasConfiguration = false;
            // TODO: Determine hasConfiguration truth value
            if (!hasConfiguration) {
                type.configurations.add(configuration);
            }

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

    // <REFACTOR>
    // e.g.,
    // add configuration uart(tx);output;ttl,cmos
//    public void letConfigurationTask(Context context) {
//
//        // TODO: Parse "bus(line)" value string pattern to create bus and lines.
//
//        // CUSTOM_CONSTRUCT CONTEXT:
//        // let direction : 'none', 'input', 'output', 'bidirectional'
//        // let current-construct : device, port, controller, task, script
//        // let script : script
//        //
//        // CONSTRUCT CONTEXT:
//        // let mode:digital;direction:input;voltage:cmos
//        // let mode: 'digital', 'analog'; direction: 'input', 'output'; voltage: 'ttl', 'cmos'
//        // let mode: 'digital', 'analog' :: direction: 'input', 'output' :: voltage: 'ttl', 'cmos'
//        // let mode 'digital', 'analog' :: direction 'input', 'output' :: voltage 'ttl', 'cmos'
//        // let mode 'digital', 'analog' : direction 'input', 'output' : voltage 'ttl', 'cmos'
//        // let mode: 'digital', 'analog' ; direction: 'input', 'output' ; voltage: 'ttl', 'cmos'
//        String[] inputLineTokens = context.expression.split("[ ]+");
//
//        String featureStateSetString = inputLineTokens[2];
//
//        String[] featureList = featureStateSetString.split(";");
//
//        // e.g.,
//        // let mode 'digital', 'analog'     # feature #1 state set
//        //     direction 'input', 'output'  # feature #2 state set
//        //     voltage 'ttl', 'cmos'        # feature #3 state set
//        List<Pair<String, List<String>>> featureStateSets = new List<>();
//
//        PortConstruct portConstruct = null;
//        if (workspace.construct != null && workspace.construct.getClass() == PortConstruct.class) {
//            portConstruct = (PortConstruct) workspace.construct;
//        }
//
//        for (int i = 0; i < featureList.length; i++) {
//
//            String[] configurationAssignmentList = featureList[i].split(":");
//            String featureIdentifierToken = configurationAssignmentList[0];
//            String featureStateListToken = configurationAssignmentList[1];
//
//            // <HACK>
//            if (!portConstruct.variables.containsKey(featureIdentifierToken)) {
////                portConstruct.features.put(variableTitle, new Variable(variableTitle));
//                portConstruct.variables.put(featureIdentifierToken, null);
//            }
//            // </HACK>
//
//            String[] featureStateList = featureStateListToken.split(","); // e.g., "'digital', 'analog'"
//
//            // Save variable's value set for the configuration constraint
//            List<String> featureStateSet = new List<>();
//            for (int j = 0; j < featureStateList.length; j++) {
//                featureStateSet.add(featureStateList[j]);
//            }
//            featureStateSets.add(new Pair<>(featureIdentifierToken, featureStateSet));
//        }
//
//        // Add VariableMap Option/Configuration
//        portConstruct.configurations.add(new Configuration(featureStateSets));
//
//    }

    // <REFACTOR>
    // e.g.,
    // add configuration uart(tx);output;ttl,cmos
//    public void addConfigurationTask(Context context) {
//
//        // TODO: Parse "bus(line)" value string pattern to create bus and lines.
//
//        String[] inputLineTokens = context.expression.split("[ ]+");
//
//        String configurationOptionString = inputLineTokens[2];
//
//        String[] configurationVariableList = configurationOptionString.split(";");
//
//        List<Pair<String, List<String>>> variableValueSets = new List<>();
//
//        PortConstruct portConstruct = null;
//        if (workspace.construct != null && workspace.construct.getClass() == PortConstruct.class) {
//            portConstruct = (PortConstruct) workspace.construct;
//        }
//
//        for (int i = 0; i < configurationVariableList.length; i++) {
//
//            String[] configurationAssignmentList = configurationVariableList[i].split(":");
//            String variableTitle = configurationAssignmentList[0];
//            String variableValues = configurationAssignmentList[1];
//
//            // <HACK>
//            if (!portConstruct.variables.containsKey(variableTitle)) {
////                portConstruct.features.put(variableTitle, new Variable(variableTitle));
//                portConstruct.variables.put(variableTitle, null);
//            }
//            // </HACK>
//
//            String[] variableValueList = variableValues.split(",");
//
//            // Save variable's value set for the configuration constraint
//            List<String> variableValueSet = new List<>();
//            for (int j = 0; j < variableValueList.length; j++) {
//                variableValueSet.add(variableValueList[j]);
//            }
//            variableValueSets.add(new Pair<>(variableTitle, variableValueSet));
//
//        }
//
//        // Add VariableMap Option/Configuration
//        portConstruct.configurations.add(new Configuration(variableValueSets));
//
//    }

    /**
     * Imports the resource at the specified URI.
     * Reference:
     * - https://en.wikipedia.org/wiki/Uniform_Resource_Identifier
     * @param context
     */
    public void importFileTask(Context context) {
        // TODO: Change argument to "Context object" (temporary cache/manager)

        // TODO: Lookup object.clone("expression")
        String[] inputLineTokens = context.expression.split("[ ]+");

        String resourceUri = inputLineTokens[1];

        String uriScheme = resourceUri.split(":")[0];
        String uriPath = resourceUri.split(":")[1];

        new LoadBuildFileTask().execute(uriPath);
    }

    public void exitTask(Context context) {
        System.exit(0);
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
//    public void solvePathConfigurationTask(Context context) {
//
//        // solve uid(34)
//        // solve path <path-address>
//
//        // request path <address>
//        // edit path
//        // set source-port[OLD_construct-types] uid:34
//        // set target-port[OLD_construct-types] uid:34
//
////        if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == DeviceConstruct.class) {
////        if (workspace.OLD_construct != null && workspace.OLD_construct.getClass() == PathConstruct.class) {
//
////            DeviceConstruct deviceConstruct = (DeviceConstruct) workspace.OLD_construct;
//
//        String[] inputLineTokens = context.expression.split("[ ]+");
//
//        // TODO: Parse address token (for index, UID, UUID; address/key/address)
//
//        PathConstruct pathConstruct = (PathConstruct) Manager_v1.get(inputLineTokens[1]);
//
//        /**
//         * "solve path [uid]"
//         */
//
//        // TODO: Resolve set of available configurations for path based on compatible configurations of ports in the path.
//
//        // Iterate through configurations for of source port in path. For each source port configurations, check
//        // the other ports' configurations for compatibility; then request each compatible configurations to a list of
//        // compatible configurations.
//        List<HashMap<String, Configuration>> pathConfigurations = new List<>();
//        for (int i = 0; i < pathConstruct.sourcePortConstruct.configurations.size(); i++) {
//            Configuration sourcePortConfiguration = pathConstruct.sourcePortConstruct.configurations.get(i);
//
//            for (int j = 0; j < pathConstruct.targetPortConstruct.configurations.size(); j++) {
//                Configuration targetPortConfiguration = pathConstruct.targetPortConstruct.configurations.get(j);
//
//                // PATH SERIAL FORMAT:
//                // ~ mode;direction;voltage + mode;direction;voltage
//                //
//                // ? mode;ports:uid,uid;voltage
//                // ? source:uid;target:uid;mode;direction;voltage
//                // > ports:uid,uid;mode;direction;voltage
//                //
//                // ? mode;direction;voltage&mode;direction;voltage
//                // ? mode;direction;voltage+mode;direction;voltage
//                // ? mode;direction;voltage|mode;direction;voltage
//
//                List<Configuration> compatiblePortConfigurations = Configuration.computeCompatibleConfigurations(sourcePortConfiguration, targetPortConfiguration);
//
//                if (compatiblePortConfigurations != null) {
//                    HashMap<String, Configuration> pathConfiguration = new HashMap<>();
//                    pathConfiguration.put("source-port", compatiblePortConfigurations.get(0));
//                    pathConfiguration.put("target-port", compatiblePortConfigurations.get(1));
//                    pathConfigurations.add(pathConfiguration);
//                }
//
//                // TODO: Pick up here. Configuration resolution isn't working, probably because of a logic bug in isCompatible(...)
//            }
////                System.out.println();
//        }
//
//        // If there is only one path configurations in the compatible configurations list, automatically configure
//        // the path with it, thereby updating the ports' configurations in the path.
//        // TODO: ^
//        if (pathConfigurations.size() == 1) {
//            // Apply the corresponding configurations to ports.
//            HashMap<String, Configuration> pathConfiguration = pathConfigurations.get(0);
//            System.out.println("✔ found compatible configurations");
//
//            // TODO: (QUESTION) Can I specify a path configurations and infer port configurations (for multi-port) or should it be a list of port configurations?
//            // TODO: Apply values based on per-variable configurations?
//            // TODO: Ensure there's only one compatible state for each of the configurations.
//
//            // Source
//            // TODO: print PORT ADDRESS
//            System.out.print("  1. mode:" + pathConfiguration.get("source-port").states.get("mode").get(0));
//            System.out.print(";direction:");
//            for (int k = 0; k < pathConfiguration.get("source-port").states.get("direction").size(); k++) {
//                System.out.print("" + pathConfiguration.get("source-port").states.get("direction").get(k));
//                if ((k + 1) < pathConfiguration.get("source-port").states.get("direction").size()) {
//                    System.out.print(", ");
//                }
//            }
//            System.out.print(";voltage:");
//            for (int k = 0; k < pathConfiguration.get("source-port").states.get("voltage").size(); k++) {
//                System.out.print("" + pathConfiguration.get("source-port").states.get("voltage").get(k));
//                if ((k + 1) < pathConfiguration.get("source-port").states.get("voltage").size()) {
//                    System.out.print(", ");
//                }
//            }
//            System.out.print(" | ");
//
//            // Target
//            // TODO: print PORT ADDRESS
//            System.out.print("mode:" + pathConfiguration.get("target-port").states.get("mode").get(0));
//            System.out.print(";direction:");
//            for (int k = 0; k < pathConfiguration.get("target-port").states.get("direction").size(); k++) {
//                System.out.print("" + pathConfiguration.get("target-port").states.get("direction").get(k));
//                if ((k + 1) < pathConfiguration.get("target-port").states.get("direction").size()) {
//                    System.out.print(", ");
//                }
//            }
//            System.out.print(";voltage:");
//            for (int k = 0; k < pathConfiguration.get("target-port").states.get("voltage").size(); k++) {
//                System.out.print("" + pathConfiguration.get("target-port").states.get("voltage").get(k));
//                if ((k + 1) < pathConfiguration.get("target-port").states.get("voltage").size()) {
//                    System.out.print(", ");
//                }
//            }
//            System.out.println();
//
//            // Configure the ports with the single compatible configurations
//            if (pathConfiguration.get("source-port").states.get("mode").size() == 1) {
//                if (pathConstruct.sourcePortConstruct.variables.containsKey("mode")
//                        && pathConstruct.sourcePortConstruct.variables.get("mode") == null) {
//                    pathConstruct.sourcePortConstruct.variables.put("mode", new Variable("mode"));
//                }
//                pathConstruct.sourcePortConstruct.variables.get("mode").value = pathConfiguration.get("source-port").states.get("mode").get(0);
//                System.out.println("  ✔ setting mode: " + pathConstruct.sourcePortConstruct.variables.get("mode").value);
//            }
//
//            if (pathConfiguration.get("source-port").states.get("direction").size() == 1) {
//                if (pathConstruct.sourcePortConstruct.variables.containsKey("direction")
//                        && pathConstruct.sourcePortConstruct.variables.get("direction") == null) {
//                    pathConstruct.sourcePortConstruct.variables.put("direction", new Variable("direction"));
//                }
//                pathConstruct.sourcePortConstruct.variables.get("direction").value = pathConfiguration.get("source-port").states.get("direction").get(0);
//                System.out.println("  ✔ setting direction: " + pathConstruct.sourcePortConstruct.variables.get("direction").value);
//            }
//
//            if (pathConfiguration.get("source-port").states.get("voltage").size() == 1) {
//                if (pathConstruct.sourcePortConstruct.variables.containsKey("voltage")
//                        && pathConstruct.sourcePortConstruct.variables.get("voltage") == null) {
//                    pathConstruct.sourcePortConstruct.variables.put("voltage", new Variable("voltage"));
//                }
//                pathConstruct.sourcePortConstruct.variables.get("voltage").value = pathConfiguration.get("source-port").states.get("voltage").get(0);
//                System.out.println("  ✔ setting voltages: " + pathConstruct.sourcePortConstruct.variables.get("voltage").value);
//            }
//
//        }
//
//        // Otherwise, list the available path configurations and prompt the user to set one of them manually.
//        else if (pathConfigurations.size() > 1) {
//            // Apply the corresponding configurations to ports.
//            System.out.println("✔ found compatible configurations");
//            for (int i = 0; i < pathConfigurations.size(); i++) {
////                    PathConfiguration pathConfiguration = consistentPathConfigurations.clone(i);
////                    System.out.println("\t[" + i + "] (" + pathConstruct.sourcePortConstruct.uid + ", " + pathConstruct.targetPortConstruct.uid + "): (" + pathConfiguration.configurations.clone("source-port").mode + ", ...) --- (" + pathConfiguration.configurations.clone("target-port").mode + ", ...)");
//            }
//            System.out.println("! set one of these configurations");
//        }
////        }
//
//    }
}
