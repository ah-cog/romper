package camp.computer.construct;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import camp.computer.Application;
import camp.computer.util.console.Color;
import camp.computer.workspace.Manager;

public class Type extends Address {

    // TODO: Remove address and features and replace with "Structure structure" and allocate it according to the "address" string passed into the constructor or request(...)
    // TODO: (cont'd) type "address" allocates <em>text</em> Structure (so even address text will be unique in memory).

    public String identifier = null;

    /**
     * {@code features} is {@code null} for <em>none</em>, <em>type</em>, <em>text</em>,
     * <em>number</em>, <em>list</em> (i.e., the primitive types).
     *
     * {@code features} is also {@code null} for <em>every</em> default type (the first one that
     * serves as an address).
     */
    public HashMap<String, Feature> features = null; // TODO: Remove? Remove setupConfiguration?
    // TODO: Set to null for "none" type
    // TODO: Change this to {@code Structure} and allocate a HashMap structure to contain features?

    // TODO: configuration(s) : assign state to multiple features <-- do this for _Container_ not Type
    // TODO: (cont'd) Map<String, List<String>> configurations = null;

    /**
     * Constructor to create the <em>default type</em> identified by {@code address}.
     *
     * @param identifier
     */
    private Type(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Constructor used to create variations of an existing {@code Type}.
     *
     * Copy constructor. Creates {@code Type} object identical to {@code baseType}.
     *
     * @param baseType The {@code Type} object to copy.
     */
    private Type(Type baseType) {

        this.identifier = baseType.identifier;

        if (baseType.features != null) {
            this.features = new HashMap<>();
            for (String featureIdentifier : baseType.features.keySet()) {
                this.features.put(featureIdentifier, baseType.features.get(featureIdentifier));
            }
        }

        // TODO: Copy the type's configurations.

    }

    /**
     * Returns a {@code List} of the {@code Type} identifiers.
     *
     * @return
     */
    public static List<Type> list() {
        List<Type> typeList = Manager.get(Type.class);
        Iterator<Type> typeIterator = typeList.iterator();
        while (typeIterator.hasNext()) {
            Type type = typeIterator.next(); // must be called before you can call i.remove()
            if (type.features != null) {
//                System.out.println("Removing!");
                typeIterator.remove();
            }
        }
        return typeList;
    }

    /**
     * Returns a {@code List} of the {@code Type}s with the specified identifier.
     *
     * @return
     */
    public static List<Type> list(String identifier) {
        List<Type> typeList = Manager.get(Type.class);
        Iterator<Type> typeIterator = typeList.iterator();
        while (typeIterator.hasNext()) {
            Type type = typeIterator.next(); // must be called before you can call i.remove()
            if (!type.identifier.equals(identifier)) {
                typeIterator.remove();
            }
        }
        return typeList;
    }

    /**
     * e.g.,
     * count type             # counts the number of types (there's only ever one type for "type" identifier)
     * count none             # constant: 1 (primitive type that can't be modified)
     * count text             # constant: 1 (primitive type that can't be modified)
     * count number           # constant: 1 (primitive type that can't be modified)
     * count list             # constant: 1 (primitive type that can't be modified)
     *
     * @return
     */
    public static int count() {
        int count = 0;
        List<Type> typeList = Manager.get(Type.class);
        Iterator<Type> typeIterator = typeList.iterator();
        while (typeIterator.hasNext()) {
            count++;
        }
        return count;
    }

    public static int count(Type type) {
        int count = 0;
        List<Type> typeList = Manager.get(Type.class);
        Iterator<Type> typeIterator = typeList.iterator();
        while (typeIterator.hasNext()) {
            Type currentType = typeIterator.next(); // must be called before you can call i.remove()
            if (currentType.identifier.equals(type.identifier)) {
                count++;
            }
        }
        return count;
    }

    // TODO: make private
    public static Type create(String identifier) {

        List<Type> typeList = Manager.get(Type.class);
        for (int i = 0; i < typeList.size(); i++) {
            if (typeList.get(i).identifier.equals(identifier)
                    && typeList.get(i).features == null) {
//            if (typeList.get(i).type == Type.request(address)) {
                return typeList.get(i);
            }
        }

        Type type = new Type(identifier);
        long uid = Manager.add(type);
        if (uid != -1) {
            return type;
        }
        return null;
    }

    public static Type create(Type baseType, String targetFeature, Feature replacementFeature) {

        Type newType = new Type(baseType);

        // Copy states from source Structure.
//        for (String featureIdentifier : baseConstruct.states.keySet()) {
//            if (featureIdentifier.equals(targetFeature)) {
//                newType.states.put(targetFeature, replacementConstruct);
//            } else {
//                newType.states.put(featureIdentifier, baseConstruct.states.request(featureIdentifier));
//            }
//        }
        if (replacementFeature == null) {
            // Remove the feature
            newType.features.remove(targetFeature);
        } else {
            // Add or replace the feature
            if (newType.features == null) {
                newType.features = new HashMap<>();
            }
            newType.features.put(targetFeature, replacementFeature);
        }

        long uid = Manager.add(newType);
        return newType;

    }

    /**
     * Returns the <em>default type</em> for the specified {@code address} if it exists.
     *
     * e.g.,
     * Type.request("none") => Type with "none" address, null features, null configurations
     *
     * @param identifier
     * @return
     */
    public static Type request(String expression) {

        // TODO: Handle case where one or more Concepts exist with address, but none with non-null features and/or configurations.

        // Search for <em>address</em> (default type or type address).
        List<Type> typeList = Manager.get(Type.class);
        for (int i = 0; i < typeList.size(); i++) {
            if (typeList.get(i).identifier.equals(expression)
                    && typeList.get(i).features == null) {
                // Return the <em>default</em> {@code Type} for the address.
                return typeList.get(i);
            }
        }

        if (expression.startsWith("'") && expression.endsWith("'")) { // TODO: Update with regex match
            return Type.request("text");
        } else if (expression.contains(",")) { // TODO: Update with regex match
            return Type.request("list");
        }
//        } else

        if (Expression.isAddress(expression)) {
            // TODO: Test this case and all other cases (after Type refactoring from old Type/Concept/Construct paradigm)
            String[] expressionTokens = expression.split("\\.");
            String typeToken = expressionTokens[0];
            long id = Long.parseLong(expressionTokens[1].split("=")[1]);

            // TODO: Support UUID as well as UID.
            typeList = Manager.get(Type.class);
            for (int i = 0; i < typeList.size(); i++) {
                if (typeList.get(i).identifier.equals(typeToken) && typeList.get(i).uid == id) {
                    return typeList.get(i);
                }
            }
        }

        // TODO: Separate request and create! Potentially add a requestOrCreate function! Yeah do that.
//        return create(address);
//        return create(expression);
        return null;
    }

    public static Type requestOrCreate(String expression) {
        Type type = Type.request(expression);
        if (type == null) {
            type = Type.create(expression);
        }
        return type;
    }

    /**
     * Requests the {@code Type} identical to {@code baseType} with the exception of the
     * {@code Feature} with address {@code targetFeature}. The {@code Feature} corresponding to
     * address {@code targetFeature} must be identical to {@code feature}.
     *
     * Used to add, remove, or change the target.
     *
     * @param baseType
     * @param targetFeature
     * @param replacementFeature
     * @return
     */
    // to add or change a feature: request(type, "ports", new Feature(...))
    // to remove a feature: request(type, "ports", null)
    public static Type request(Type baseType, String targetFeature, Feature replacementFeature) {
//    public static Type request(Type type, String feature, Structure replacementFeature) {
//        request(new Map(new Pair(key, Feature), new Pair(key, Feature)))

        // TODO: Lookup Feature in DB (as if it were a construct, creating it if needed)
        // TODO: Check if {@code type} exists with the specified featureIdentifier set to Feature, with all else equal

        // Look for persistent "empty list" object (i.e., the default list).
        List<Address> identiferList = Manager.get();
        for (int i = 0; i < identiferList.size(); i++) {
            if (identiferList.get(i).getClass() == Type.class) {
                Type candidateType = (Type) identiferList.get(i);

                // Check (1) if constructs are based on the same specified type version, and
                //       (2) same set of features and assignments to constructs except the specified feature to change.
                HashMap<String, Feature> baseConceptFeatures = (HashMap<String, Feature>) baseType.features;
                HashMap<String, Feature> candidateConceptFeatures = (HashMap<String, Feature>) candidateType.features;

                // Compare identifer, types, domain, listTypes
                // TODO: Move comparison into Type.hasConstruct(type, construct);
                boolean isConstructMatch = true;

                if (baseConceptFeatures == null) {
                    // TODO: Is this condition sufficient?
                    isConstructMatch = false;
                } else if (!baseType.features.containsKey(targetFeature) && replacementFeature == null) {

                    // NOOP case? Trying to remove a nonexistent feature...

                } else if (!baseType.features.containsKey(targetFeature) && replacementFeature != null) { // i.e., adding
                    // i.e., ADDING FEATURE

                    if (candidateConceptFeatures == null && candidateType != null) {
                        // TODO: Is this condition sufficient?
                        isConstructMatch = false;
                    } else if (candidateConceptFeatures.size() != (baseConceptFeatures.size() + 1)) {
                        isConstructMatch = false;
                    } else {

                        // Compare candidate construct (from repository) with the current construct being updated.
                        // First, ensure all features in the base type are present in the candidate type.
                        for (String featureIdentifier : baseConceptFeatures.keySet()) {
//                            if (featureIdentifier.equals(targetFeature)) {
//                                if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
////                                    || !candidateType.states.containsKey(featureIdentifier)
////                                    || candidateType.states.request(featureIdentifier) != featureConstructReplacement) {
////                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
//                                    isConstructMatch = false;
//                                }
//                            } else {
                                if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
//                                    || !candidateType.states.containsKey(featureIdentifier)
//                                    || candidateType.states.request(featureIdentifier) != baseType.states.request(featureIdentifier)) {
//                                        || !candidateConstructFeatures.containsValue(type.features.request(featureIdentifier))) {
                                    isConstructMatch = false;
                                }
//                            }
                        }

                        // Second, ensure that the target feature exists in the candidate type.
                        if (!candidateConceptFeatures.containsKey(targetFeature)) {
//                                    || !candidateType.states.containsKey(featureIdentifier)
//                                    || candidateType.states.request(featureIdentifier) != featureConstructReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
                            isConstructMatch = false;
                        }

                        // TODO: Additional checks... (1) for missing or extra features...
                    }

                } else if (baseType.features.containsKey(targetFeature) && replacementFeature != null) { // i.e., updating
                    // i.e., CHANGING FEATURE

                    if (candidateConceptFeatures == null && baseConceptFeatures != null) {
                        isConstructMatch = false;
                    } else if (candidateConceptFeatures.size() != baseConceptFeatures.size()) {
                        isConstructMatch = false;
                    } else {

                        // Compare candidate construct (from repository) with the current construct being updated.
                        // First, ensure all features in the base type are present in the candidate type.
                        for (String featureIdentifier : baseConceptFeatures.keySet()) {
                            if (featureIdentifier.equals(targetFeature)) {
                                if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
//                                    || !candidateType.states.containsKey(featureIdentifier)
//                                    || candidateType.states.request(featureIdentifier) != featureConstructReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
                                    isConstructMatch = false;
                                }

                                // Compare actual features for equality
                                if (candidateConceptFeatures.get(featureIdentifier) != replacementFeature) {
                                    isConstructMatch = false;
                                }
                            } else {
                                if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
    //                                    || !candidateType.states.containsKey(featureIdentifier)
    //                                    || candidateType.states.request(featureIdentifier) != baseType.states.request(featureIdentifier)) {
    //                                        || !candidateConstructFeatures.containsValue(type.features.request(featureIdentifier))) {
                                    isConstructMatch = false;
                                }

                                // Compare actual features for equality
                                if (candidateConceptFeatures.get(featureIdentifier) != baseConceptFeatures.get(featureIdentifier)) {
                                    isConstructMatch = false;
                                }
                            }
                        }

                        // Second, ensure that the target feature exists in the candidate type.
                        if (!candidateConceptFeatures.containsKey(targetFeature)) {
//                                    || !candidateType.states.containsKey(featureIdentifier)
//                                    || candidateType.states.request(featureIdentifier) != featureConstructReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
                            isConstructMatch = false;
                        }

                        // TODO: Additional checks... (1) for missing or extra features...
                    }

                } else if (baseType.features.containsKey(targetFeature) && replacementFeature == null) { // i.e., removing
                    // i.e., REMOVE FEATURE

                    if (candidateConceptFeatures.size() != (baseConceptFeatures.size() - 1)) {
                        isConstructMatch = false;
                    } else {

                        // Compare candidate construct (from repository) with the current construct being updated.
                        for (String featureIdentifier : baseConceptFeatures.keySet()) {
                            if (featureIdentifier.equals(targetFeature)) {
                                if (candidateConceptFeatures.containsKey(featureIdentifier)) {
//                                    || !candidateType.states.containsKey(featureIdentifier)
//                                    || candidateType.states.request(featureIdentifier) != featureConstructReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
                                    isConstructMatch = false;
                                }
                            } else {
                                if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
//                                    || !candidateType.states.containsKey(featureIdentifier)
//                                    || candidateType.states.request(featureIdentifier) != baseType.states.request(featureIdentifier)) {
//                                        || !candidateConstructFeatures.containsValue(type.features.request(featureIdentifier))) {
                                    isConstructMatch = false;
                                }
                            }
                        }

                        // TODO: Additional checks... (1) for missing or extra features...
                    }

                }

//                if (candidateConceptFeatures.size() != baseConceptFeatures.size()) {
//                    isConstructMatch = false;
//                } else {
//
//                    // Compare candidate construct (from repository) with the current construct being updated.
//                    for (String featureIdentifier : baseConceptFeatures.keySet()) {
//                        if (featureIdentifier.equals(targetFeature)) {
//                            if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
////                                    || !candidateType.states.containsKey(featureIdentifier)
////                                    || candidateType.states.request(featureIdentifier) != featureConstructReplacement) {
////                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
//                                isConstructMatch = false;
//                            }
//                        } else {
//                            if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
////                                    || !candidateType.states.containsKey(featureIdentifier)
////                                    || candidateType.states.request(featureIdentifier) != baseType.states.request(featureIdentifier)) {
////                                        || !candidateConstructFeatures.containsValue(type.features.request(featureIdentifier))) {
//                                isConstructMatch = false;
//                            }
//                        }
//                    }
//
//                    // TODO: Additional checks... (1) for missing or extra features...
//
//                }

                if (isConstructMatch) {
                    return candidateType;
                }


                // TODO: Look for permutation of a list (matching list of constructs)?
//                    return construct;
            }
        }

        // Create new Structure if got to this point because an existing one was not found
        Type replacementType = Type.create(baseType, targetFeature, replacementFeature);
        if (replacementType != null) {

            Application.log.log("\tno type match > ");
            Application.log.log("created type " + replacementType);

//            System.out.print(Color.ANSI_GREEN);
//            System.out.print("\tno type match > ");
//            System.out.println("created type " + replacementType);
//            System.out.print(Color.ANSI_RESET);
            return replacementType;
        }

        // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Type used to create it).
        return null;
    }

    /**
     * Returns {@code true} if at least one {@code Type} exists for the specified
     * {@code address}.
     *
     * @return True
     */
    public static boolean exists(String identifier) {
        // TODO: Verify that this is correct...
        List<Type> typeList = Manager.get(Type.class);
        for (int i = 0; i < typeList.size(); i++) {
            if (typeList.get(i).identifier.equals(identifier)
                    /* && typeList.get(i).features == null */) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return identifier + ".id=" + uid;
    }

    public String toColorString() {
        return Color.ANSI_BLUE + Color.ANSI_BOLD_ON + identifier + Color.ANSI_RESET + ".id=" + uid;
    }

}
