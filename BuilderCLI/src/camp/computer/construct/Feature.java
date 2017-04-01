package camp.computer.construct;

import java.util.ArrayList;
import java.util.List;

import camp.computer.Application;
import camp.computer.util.console.Color;
import camp.computer.workspace.Manager;

/**
 * An {@code VariableValueSet} stores a list of handle values for a specified handle identified by its unique label.
 */
public class Feature extends Handle {

    // handle/key: string
    // types: string, list, construct-name
    // domain: list of accepted tokens; (or) for lists, stores list of values that can be stored in the list

    /**
     * {@code handle} is a {@code String} that uniquely identifies the {@code Variable} in the
     * containing {@code VariableMap}. <em>in the namespace</em>.
     */
    public String identifier = null; // e.g., mode; direction; voltage
    // TODO: Make handle separate from feature in database so it's identified uniquely by it's types list and domain list?

    // Content Type (e.g., none, any, text, list, table, etc.)
    public List<Type> types = null; // new ArrayList<>(); // if size == 0, then 'none' type! if null, then 'any'.

    // Content Domain (contains Handle Types and Handle Content)
    // NOTE: This only ever contains "text object" or references to specific constructs
    public List<Structure> domain; // if size == 0, then 'none'! if null, then 'any'!
    // TODO: Create a separate feature domain for each types in featureType

    /**
     * Only used for <em>list</em> {@code TypeId}.
     *
     * {@code listTypes} is {@code null} when <em>any</em> types is acceptable. {@code listTypes} is
     * an empty list when the list's types is <em>none</em>, meaning nothing can be added to the
     * list.
     */
    public List<Type> listTypes = null; // if size == 0, then unconstrained!
    // TODO: Remove listTypes and add it to the "list" primitive construct's architecture?

    private Feature(String identifier, List<Type> types, List<Structure> domain, List<Type> listTypes) {

        this.identifier = identifier;

        if (types != null) {
            if (this.types == null) {
                this.types = new ArrayList<>();
            }
            this.types.addAll(types);
        }

        if (domain != null) {
            if (this.domain == null) {
                this.domain = new ArrayList<>();
            }
            this.domain.addAll(domain);
        }

        if (listTypes != null) {
            if (this.listTypes == null) {
                this.listTypes = new ArrayList<>();
            }
            this.listTypes.addAll(listTypes);
        }
    }

    public static Feature create(String identifier, List<Type> types, List<Structure> domain, List<Type> listTypes) {
        Feature feature = new Feature(identifier, types, domain, listTypes);
        return feature;
    }

    public static Feature exists() {
        return null;
    }

    public static Feature request(String identifier, List<Type> types, List<Structure> domain, List<Type> listTypes) {

        List<Handle> identiferList = Manager.get();
        for (int i = 0; i < identiferList.size(); i++) {
            if (identiferList.get(i).getClass() == Feature.class) {
                Feature candidateFeature = (Feature) identiferList.get(i);

                boolean hasMatchingIdentifier = true;
                boolean hasMatchingTypes = true;
                boolean hasMatchingDomain = true;
                boolean hasMatchingListTypes = true;

                // Compare handle
                if (!candidateFeature.identifier.equals(identifier)) {
                    hasMatchingIdentifier = false;
                }

                // Compare type sets
                if ((candidateFeature.types != null && types != null)) {
                    if ((candidateFeature.types == null && types != null)
                            || (candidateFeature.types != null && types == null)
                            || candidateFeature.types.size() != types.size()) {
                        hasMatchingTypes = false;
                    } else if (candidateFeature.types != null && types != null) {
                        for (int j = 0; j < types.size(); j++) {
                            if (!candidateFeature.types.contains(types.get(j))) {
                                hasMatchingTypes = false;
                            }
                        }
                    }
                }

                // Compare domain sets
                if ((candidateFeature.domain != null && domain != null)) {
                    if ((candidateFeature.domain == null && domain != null)
                            || (candidateFeature.domain != null && domain == null)
                            || candidateFeature.domain.size() != domain.size()) {
                        hasMatchingDomain = false;
                    } else if (candidateFeature.domain != null && domain != null) {
                        for (int j = 0; j < domain.size(); j++) {
                            if (!candidateFeature.domain.contains(domain.get(j))) {
                                hasMatchingDomain = false;
                            }
                        }
                    }
                }

                // Compare list type sets
                if ((candidateFeature.listTypes != null && listTypes != null)) {
                    if ((candidateFeature.listTypes == null && listTypes != null)
                            || (candidateFeature.listTypes != null && listTypes == null)
                            || candidateFeature.listTypes.size() != listTypes.size()) {
                        hasMatchingListTypes = false;
                    } else {
                        for (int j = 0; j < listTypes.size(); j++) {
                            if (!candidateFeature.listTypes.contains(listTypes.get(j))) {
                                hasMatchingListTypes = false;
                            }
                        }
                    }
                }

                // The feature exists so return it.
                if (hasMatchingIdentifier && hasMatchingTypes && hasMatchingDomain && hasMatchingListTypes) {
                    return candidateFeature;
                }

            }
        }

        // The feature does not exist so create it then return it.
        Feature feature = Feature.create(identifier, types, domain, listTypes);
        long uid = Manager.add(feature);

        Application.log.log("\tno feature match > ");
        Application.log.log("created feature " + feature.toColorString());

//        System.out.print(Color.ANSI_GREEN);
//        System.out.print("\tno feature match > ");
//        System.out.println("created feature " + feature.toColorString());
//        System.out.print(Color.ANSI_RESET);

        return feature;
    }

    @Override
    public String toString() {
        return identifier;
    }

    public String toColorString() {
        return Color.ANSI_YELLOW + Color.ANSI_BOLD_ON + identifier + Color.ANSI_RESET;
    }

}
