package camp.computer.data.format.configuration;

/**
 * An {@code VariableValueSet} stores a list of identifier values for a specified identifier identified by its unique label.
 */
public class Variable {

    /**
     * {@code identifier} is a {@code String} that uniquely identifies the {@code Variable} in the
     * containing {@code VariableMap}. <em>in the
     * namespace</em>.
     */
    public String title = null; // e.g., mode; direction; voltage

//    /**
//     * The {@code ValueSet} stores the set of {@code State}s that can be assigned to the
//     * {@code Variable}. In the parlance of the <em>Configuration Satisfaction Problem</em>, the
//     * {@code ValueSet} defines the domain of the {@code Variable}.
//     */
//    public ValueSet<T> values = new ValueSet<>();

    /**
     * {@code value} references a {@code State} in the {@code values} {@code ValueSet}.
     * <p>
     * {@code value} is initialized to {@code null}, which indicates that it is
     * <em>unassigned</em>, or it has not yet been assigned to a {@code State} in {@code values}.
     */
    //public State<?> value = null;
    public String value = null;

    public Variable(String title) {

        this.title = title;
        this.value = null;

    }

    //    public Variable(String identifier, ValueSet<T> values) {
    public Variable(String title, String value) {

        this.title = title;
        this.value = value;

    }

}
