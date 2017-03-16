package camp.computer.construct;

import java.util.HashMap;
import java.util.List;

import camp.computer.util.console.Color;
import camp.computer.workspace.Manager;

public class Concept extends Identifier {

    public Type type = null;

    public HashMap<String, Feature> features = new HashMap<>(); // TODO: Remove? Remove setupConfiguration?

    // TODO: configuration(s) : assign state to multiple features <-- do this for _Container_ not Concept

    private Concept(Type type) {
        this.type = type;
    }

    /**
     * Copy constructor. Creates {@code Concept} object identical to {@code baseConcept}.
     * @param baseConcept The {@code Concept} object to copy.
     */
    private Concept(Concept baseConcept) {

        this.type = baseConcept.type;

        if (baseConcept.features != null) {
            for (String featureIdentifier : baseConcept.features.keySet()) {
                this.features.put(featureIdentifier, baseConcept.features.get(featureIdentifier));
            }
        }

        // TODO: Copy the concept's configurations.

    }

    private static Concept create(Type type) {
        Concept concept = new Concept(type);
        long uid = Manager.add(concept);
        if (uid != -1) {
            return concept;
        }
        return null;
    }

    public static Concept create(Concept baseConcept, String targetFeature, Feature replacementFeature) {

        Concept newConcept = new Concept(baseConcept);

        // Copy states from source Construct.
//        for (String featureIdentifier : baseConstruct.states.keySet()) {
//            if (featureIdentifier.equals(targetFeature)) {
//                newConcept.states.put(targetFeature, replacementConstruct);
//            } else {
//                newConcept.states.put(featureIdentifier, baseConstruct.states.get(featureIdentifier));
//            }
//        }
        if (replacementFeature == null) {
            // Remove the feature
            newConcept.features.remove(targetFeature);
        } else {
            // Add or replace the feature
            newConcept.features.put(targetFeature, replacementFeature);
        }

        long uid = Manager.add(newConcept);
        return newConcept;

    }

    public static Concept request(Type type) {
        if (!Concept.exists(type)) {
            return create(type);
        } else {
            List<Concept> conceptList = Manager.get(Concept.class);
            for (int i = 0; i < conceptList.size(); i++) {
                if (conceptList.get(i).type == type) {
                    return conceptList.get(i);
                }
            }
        }
        return null;
    }

    /**
     * Requests the {@code Concept} identical to {@code baseConcept} with the exception of the
     * {@code Feature} with identifier {@code targetFeature}. The {@code Feature} corresponding to
     * identifier {@code targetFeature} must be identical to {@code feature}.
     *
     * Used to add, remove, or change the target.
     *
     * @param baseConcept
     * @param targetFeature
     * @param replacementFeature
     * @return
     */
    // to add or change a feature: request(concept, "ports", new Feature(...))
    // to remove a feature: request(concept, "ports", null)
    public static Concept request(Concept baseConcept, String targetFeature, Feature replacementFeature) {
//    public static Concept request(Concept concept, String feature, Construct replacementFeature) {
//        request(new Map(new Pair(key, Feature), new Pair(key, Feature)))

        // TODO: Lookup Feature in DB (as if it were a construct, creating it if needed)
        // TODO: Check if {@code concept} exists with the specified featureIdentifier set to Feature, with all else equal

        Type type = baseConcept.type; // Construct type

        // Look for persistent "empty list" object (i.e., the default list).
        List<Identifier> identiferList = Manager.get();
        for (int i = 0; i < identiferList.size(); i++) {
            if (identiferList.get(i).getClass() == Concept.class) {
                Concept candidateConcept = (Concept) identiferList.get(i);

                // Check (1) if constructs are based on the same specified concept version, and
                //       (2) same set of features and assignments to constructs except the specified feature to change.
                HashMap<String, Feature> baseConceptFeatures = (HashMap<String, Feature>) baseConcept.features;
                HashMap<String, Feature> candidateConceptFeatures = (HashMap<String, Feature>) candidateConcept.features;

                // Compare identifer, types, domain, listTypes
                // TODO: Move comparison into Concept.hasConstruct(concept, construct);
                boolean isConstructMatch = true;


                if (!baseConcept.features.containsKey(targetFeature) && replacementFeature == null) {

                    // NOOP case? Trying to remove a nonexistent feature...

                } else if (!baseConcept.features.containsKey(targetFeature) && replacementFeature != null) { // i.e., adding
                    // i.e., ADDING FEATURE

                    if (candidateConceptFeatures.size() != (baseConceptFeatures.size() + 1)) {
                        isConstructMatch = false;
                    } else {

                        // Compare candidate construct (from repository) with the current construct being updated.
                        // First, ensure all features in the base concept are present in the candidate concept.
                        for (String featureIdentifier : baseConceptFeatures.keySet()) {
//                            if (featureIdentifier.equals(targetFeature)) {
//                                if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
////                                    || !candidateConcept.states.containsKey(featureIdentifier)
////                                    || candidateConcept.states.get(featureIdentifier) != featureConstructReplacement) {
////                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
//                                    isConstructMatch = false;
//                                }
//                            } else {
                                if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
//                                    || !candidateConcept.states.containsKey(featureIdentifier)
//                                    || candidateConcept.states.get(featureIdentifier) != baseConcept.states.get(featureIdentifier)) {
//                                        || !candidateConstructFeatures.containsValue(concept.features.get(featureIdentifier))) {
                                    isConstructMatch = false;
                                }
//                            }
                        }

                        // Second, ensure that the target feature exists in the candidate concept.
                        if (!candidateConceptFeatures.containsKey(targetFeature)) {
//                                    || !candidateConcept.states.containsKey(featureIdentifier)
//                                    || candidateConcept.states.get(featureIdentifier) != featureConstructReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
                            isConstructMatch = false;
                        }

                        // TODO: Additional checks... (1) for missing or extra features...
                    }

                } else if (baseConcept.features.containsKey(targetFeature) && replacementFeature != null) { // i.e., updating
                    // i.e., CHANGING FEATURE

                    if (candidateConceptFeatures.size() != baseConceptFeatures.size()) {
                        isConstructMatch = false;
                    } else {

                        // Compare candidate construct (from repository) with the current construct being updated.
                        // First, ensure all features in the base concept are present in the candidate concept.
                        for (String featureIdentifier : baseConceptFeatures.keySet()) {
                            if (featureIdentifier.equals(targetFeature)) {
                                if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
//                                    || !candidateConcept.states.containsKey(featureIdentifier)
//                                    || candidateConcept.states.get(featureIdentifier) != featureConstructReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
                                    isConstructMatch = false;
                                }

                                // Compare actual features for equality
                                if (candidateConceptFeatures.get(featureIdentifier) != replacementFeature) {
                                    isConstructMatch = false;
                                }
                            } else {
                                if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
    //                                    || !candidateConcept.states.containsKey(featureIdentifier)
    //                                    || candidateConcept.states.get(featureIdentifier) != baseConcept.states.get(featureIdentifier)) {
    //                                        || !candidateConstructFeatures.containsValue(concept.features.get(featureIdentifier))) {
                                    isConstructMatch = false;
                                }

                                // Compare actual features for equality
                                if (candidateConceptFeatures.get(featureIdentifier) != baseConceptFeatures.get(featureIdentifier)) {
                                    isConstructMatch = false;
                                }
                            }
                        }

                        // Second, ensure that the target feature exists in the candidate concept.
                        if (!candidateConceptFeatures.containsKey(targetFeature)) {
//                                    || !candidateConcept.states.containsKey(featureIdentifier)
//                                    || candidateConcept.states.get(featureIdentifier) != featureConstructReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
                            isConstructMatch = false;
                        }

                        // TODO: Additional checks... (1) for missing or extra features...
                    }

                } else if (baseConcept.features.containsKey(targetFeature) && replacementFeature == null) { // i.e., removing
                    // i.e., REMOVE FEATURE

                    if (candidateConceptFeatures.size() != (baseConceptFeatures.size() - 1)) {
                        isConstructMatch = false;
                    } else {

                        // Compare candidate construct (from repository) with the current construct being updated.
                        for (String featureIdentifier : baseConceptFeatures.keySet()) {
                            if (featureIdentifier.equals(targetFeature)) {
                                if (candidateConceptFeatures.containsKey(featureIdentifier)) {
//                                    || !candidateConcept.states.containsKey(featureIdentifier)
//                                    || candidateConcept.states.get(featureIdentifier) != featureConstructReplacement) {
//                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
                                    isConstructMatch = false;
                                }
                            } else {
                                if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
//                                    || !candidateConcept.states.containsKey(featureIdentifier)
//                                    || candidateConcept.states.get(featureIdentifier) != baseConcept.states.get(featureIdentifier)) {
//                                        || !candidateConstructFeatures.containsValue(concept.features.get(featureIdentifier))) {
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
////                                    || !candidateConcept.states.containsKey(featureIdentifier)
////                                    || candidateConcept.states.get(featureIdentifier) != featureConstructReplacement) {
////                                        || !candidateConstructFeatures.containsValue(featureConstructReplacement)) {
//                                isConstructMatch = false;
//                            }
//                        } else {
//                            if (!candidateConceptFeatures.containsKey(featureIdentifier)) {
////                                    || !candidateConcept.states.containsKey(featureIdentifier)
////                                    || candidateConcept.states.get(featureIdentifier) != baseConcept.states.get(featureIdentifier)) {
////                                        || !candidateConstructFeatures.containsValue(concept.features.get(featureIdentifier))) {
//                                isConstructMatch = false;
//                            }
//                        }
//                    }
//
//                    // TODO: Additional checks... (1) for missing or extra features...
//
//                }

                if (isConstructMatch) {
                    return candidateConcept;
                }


                // TODO: Look for permutation of a list (matching list of constructs)?
//                    return construct;
            }
        }

        // Create new Construct if got to this point because an existing one was not found
        Concept replacementConcept = Concept.create(baseConcept, targetFeature, replacementFeature);
        if (replacementConcept != null) {
            System.out.print(Color.ANSI_GREEN);
            System.out.print("\tno concept match > ");
            System.out.println("created concept " + replacementConcept);
            System.out.print(Color.ANSI_RESET);
            return replacementConcept;
        }

        // TODO: Iterate through constructs searching for one that matches the default construct hierarchy for the specified type (based on the Concept used to create it).
        return null;
    }

    /**
     * Returns {@code true} if at least one {@code Concept} exists for the specified
     * {@code identifier}.
     *
     * @param type
     * @return True
     */
    public static boolean exists(Type type) {
        List<Concept> conceptList = Manager.get(Concept.class);
        for (int i = 0; i < conceptList.size(); i++) {
            if (conceptList.get(i).type == type) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
//        return type + " (id: " + uid + " -> uuid:" + uuid + ")";
        return type + ".id." + uid;
        // return type + " (id:" + uid + ")";
    }

    public String toColorString() {
//        return Color.ANSI_BLUE + Color.ANSI_BOLD_ON + type + Color.ANSI_RESET + " (id: " + uid + ")";
        return Color.ANSI_BLUE + Color.ANSI_BOLD_ON + type + Color.ANSI_RESET + ".id." + uid;
        // return Color.ANSI_BLUE + Color.ANSI_BOLD_ON + type + Color.ANSI_RESET + " (id:" + uid + " -> uuid: " + uuid + ")";
    }

}
