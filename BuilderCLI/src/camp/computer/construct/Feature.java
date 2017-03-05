package camp.computer.construct;

import java.util.ArrayList;
import java.util.List;

import camp.computer.util.console.Color;

/**
 * An {@code VariableValueSet} stores a list of identifier values for a specified identifier identified by its unique label.
 */
public class Feature extends Identifier {

    // identifier/key: string
    // types: string, list, construct-name
    // domain: list of accepted tokens; (or) for lists, stores list of values that can be stored in the list

    /**
     * {@code identifier} is a {@code String} that uniquely identifies the {@code Variable} in the
     * containing {@code VariableMap}. <em>in the namespace</em>.
     */
    public String identifier = null; // e.g., mode; direction; voltage
    // TODO: Make identifier separate from feature in database so it's identified uniquely by it's types list and domain list?

    // Content Type (e.g., none, any, text, list, etc.)
//    public List<Type> types = new ArrayList<>(); // if size == 0, then unconstrained!
    public List<Type> types = new ArrayList<>(); // if size == 0, then unconstrained! if null, then 'none' (impossible).

    // Content Domain (contains Identifier Types and Identifier Content)
    // NOTE: This only ever contains "text object" or references to specific constructs
    public List<Construct> domain; // if size == 0, then 'none'! if null, then 'any'!
    // TODO: Create a separate feature domain for each types in featureType

    /**
     * Only used for <em>list</em> {@code Type}.
     *
     * {@code listTypes} is {@code null} when <em>any</em> types is acceptable. {@code listTypes} is
     * an empty list when the list's types is <em>none</em>, meaning nothing can be added to the
     * list.
     */
    public List<Type> listTypes = null; // if size == 0, then unconstrained!

    public Feature(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return identifier;
    }

    public String toColorString() {
        return Color.ANSI_YELLOW + Color.ANSI_BOLD_ON + identifier + Color.ANSI_RESET;
    }

}
