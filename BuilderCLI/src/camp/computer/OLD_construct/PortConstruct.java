package camp.computer.OLD_construct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import camp.computer.data.format.configuration.Configuration;
import camp.computer.data.format.configuration.Variable;

public class PortConstruct extends Construct_v1 {

    // TODO: Consider renaming VariableMap to State. VariableMap would be a set of configurations AND value assignments to the configurations.
    public HashMap<String, Variable> variables = new HashMap<>(); // TODO: Remove? Remove setupConfiguration?

    public List<Configuration> configurations = new ArrayList<>();

    public PortConstruct() {
        super();
        this.type = "port";
    }

    public static boolean isUnassigned(PortConstruct portConstruct) {

        // TODO: store unassigned variable with key and null value
        if (portConstruct.variables.get("mode") == null
                && portConstruct.variables.get("direction") == null
                && portConstruct.variables.get("voltage") == null) {
//                portConstruct.features.clone("mode").value == null
//                && portConstruct.features.clone("direction").value == null
//                && portConstruct.features.clone("voltage").value == null) {

            return true;
        }

        return false;

    }

}
