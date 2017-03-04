package camp.computer.data.format.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import camp.computer.Interpreter;
import camp.computer.util.Pair;
import camp.computer.util.Tuple;

/**
 * Constrains the values (or values) that can be assigned <em>simultaneously</em> to each of the
 * configurations <em>in combination with each other</em> uniquely identified by each
 * {@code variableConstraint[i].identifier} to the values (or values) specified in {@code variableConstraint[i].values}.
 * <p>
 * Therefore {@code Configuration} specifies a constraint on the valid mutual state assignments among
 * the specified configurations.
 * <p>
 * In other words, this constrains the set of valid <em>combinitoric value permutations</em> that
 * can be assigned to configurations at the same time.
 */
public class Configuration {

    public HashMap<String, Tuple<String>> variables = new HashMap<>();

    public Configuration(Pair<String, Tuple<String>>... variableValueSets) {

        for (int variableIndex = 0; variableIndex < variableValueSets.length; variableIndex++) {
            this.variables.put(variableValueSets[variableIndex].key, variableValueSets[variableIndex].value);
        }

    }

    public Configuration(List<Pair<String, Tuple<String>>> variableValueSets) {

        for (int variableIndex = 0; variableIndex < variableValueSets.size(); variableIndex++) {
            this.variables.put(variableValueSets.get(variableIndex).key, variableValueSets.get(variableIndex).value);
        }

    }

    // <API>

    /**
     * Computes/resolves the pair of "minimal compatible" {@code PortConfigurationConstraint}s
     * given a pair of two {@code PortConfigurationConstraint}s. The resulting {@code PortConfigurationConstraint}s
     * will remove values that cannot be assigned to configurations in the {@code PortConfigurationConstraint}.
     *
     * @param sourceConfiguration
     * @param targetConfiguration
     * @return
     */
    public static List<Configuration> computeCompatibleConfigurations(Configuration sourceConfiguration, Configuration targetConfiguration) {

        // computes the "compatible intersection" configurations of the mode configurations of the two configurations

        // <VERBOSE_LOG>
        // Source Port
//        if (ENABLE_VERBOSE_OUTPUT) {
//
//            System.out.print("? (source) " + sourceConfiguration.mode + ";");
//
//            if (sourceConfiguration.directions == null) {
//                System.out.print("null");
//            } else {
//                for (int i = 0; i < sourceConfiguration.directions.values.size(); i++) {
//                    System.out.print("" + sourceConfiguration.directions.values.clone(i));
//                    if ((i + 1) < sourceConfiguration.directions.values.size()) {
//                        System.out.print(",");
//                    }
//                }
//            }
//            System.out.print(";");
//
//            if (sourceConfiguration.voltages == null) {
//                System.out.print("null");
//            } else {
//                for (int i = 0; i < sourceConfiguration.voltages.values.size(); i++) {
//                    System.out.print("" + sourceConfiguration.voltages.values.clone(i));
//                    if ((i + 1) < sourceConfiguration.voltages.values.size()) {
//                        System.out.print(",");
//                    }
//                }
//            }
//            System.out.println();
//
//            // Target Port(s)
//            System.out.print("  (target) " + targetConfiguration.mode + ";");
//
//            if (targetConfiguration.directions == null) {
//                System.out.print("null");
//            } else {
//                for (int i = 0; i < targetConfiguration.directions.values.size(); i++) {
//                    System.out.print("" + targetConfiguration.directions.values.clone(i));
//                    if ((i + 1) < targetConfiguration.directions.values.size()) {
//                        System.out.print(",");
//                    }
//                }
//            }
//            System.out.print(";");
//
//            if (targetConfiguration.voltages == null) {
//                System.out.print("null");
//            } else {
//                for (int i = 0; i < targetConfiguration.voltages.values.size(); i++) {
//                    System.out.print("" + targetConfiguration.voltages.values.clone(i));
//                    if ((i + 1) < targetConfiguration.voltages.values.size()) {
//                        System.out.print(",");
//                    }
//                }
//            }
//        }
//        // </VERBOSE_LOG>

        // <INITIALIZE_CONSTRAINTS>
        // Generate updated configurations with features' domains containing only compatible values.
        Configuration compatibleSourceConfiguration = new Configuration();
        Configuration compatibleTargetConfiguration = new Configuration();

        // Add source port Configuration with empty variable state sets
        for (String variableTitle : sourceConfiguration.variables.keySet()) {
            compatibleSourceConfiguration.variables.put(variableTitle, new Tuple());
        }

        // Add target port Configuration with empty variable state sets
        for (String variableTitle : targetConfiguration.variables.keySet()) {
            compatibleTargetConfiguration.variables.put(variableTitle, new Tuple());
        }
        // </INITIALIZE_CONSTRAINTS>

        // <MODE_CONSTRAINT_CHECKS>
        List<String> sourceConfigurationVariableValues = sourceConfiguration.variables.get("mode").values;
        List<String> targetConfigurationVariableValues = targetConfiguration.variables.get("mode").values;

        if (sourceConfigurationVariableValues.contains("digital") && targetConfigurationVariableValues.contains("digital")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("digital");
            compatibleTargetConfiguration.variables.get("mode").values.add("digital");
        }

        if (sourceConfigurationVariableValues.contains("analog") && targetConfigurationVariableValues.contains("analog")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("analog");
            compatibleTargetConfiguration.variables.get("mode").values.add("analog");
        }

        if (sourceConfigurationVariableValues.contains("pwm") && targetConfigurationVariableValues.contains("pwm")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("pwm");
            compatibleTargetConfiguration.variables.get("mode").values.add("pwm");
        }

        if (sourceConfigurationVariableValues.contains("resistive_touch") && targetConfigurationVariableValues.contains("resistive_touch")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("resistive_touch");
            compatibleTargetConfiguration.variables.get("mode").values.add("resistive_touch");
        }

        if (sourceConfigurationVariableValues.contains("power") && targetConfigurationVariableValues.contains("power")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("power");
            compatibleTargetConfiguration.variables.get("mode").values.add("power");
        }

        if (sourceConfigurationVariableValues.contains("i2c(scl)") && targetConfigurationVariableValues.contains("i2c(scl)")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("i2c(scl)");
            compatibleTargetConfiguration.variables.get("mode").values.add("i2c(scl)");
        }

        if (sourceConfigurationVariableValues.contains("spi(sclk)") && targetConfigurationVariableValues.contains("spi(sclk)")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("spi(sclk)");
            compatibleTargetConfiguration.variables.get("mode").values.add("spi(sclk)");
        }

        if (sourceConfigurationVariableValues.contains("spi(mosi)") && targetConfigurationVariableValues.contains("spi(mosi)")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("spi(mosi)");
            compatibleTargetConfiguration.variables.get("mode").values.add("spi(mosi)");
        }

        if (sourceConfigurationVariableValues.contains("spi(miso)") && targetConfigurationVariableValues.contains("spi(miso)")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("spi(miso)");
            compatibleTargetConfiguration.variables.get("mode").values.add("spi(miso)");
        }

        if (sourceConfigurationVariableValues.contains("spi(ss)") && targetConfigurationVariableValues.contains("spi(ss)")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("spi(ss)");
            compatibleTargetConfiguration.variables.get("mode").values.add("spi(ss)");
        }

        if (sourceConfigurationVariableValues.contains("uart(tx)") && targetConfigurationVariableValues.contains("uart(rx)")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("uart(tx)");
            compatibleTargetConfiguration.variables.get("mode").values.add("uart(rx)");
        }

        if (sourceConfigurationVariableValues.contains("uart(rx)") && targetConfigurationVariableValues.contains("uart(tx)")) {
            compatibleSourceConfiguration.variables.get("mode").values.add("uart(rx)");
            compatibleTargetConfiguration.variables.get("mode").values.add("uart(tx)");
        }
        // </MODE_CONSTRAINT_CHECKS>

        // <DIRECTION_CONSTRAINT_CHECKS>
        sourceConfigurationVariableValues = sourceConfiguration.variables.get("direction").values;
        targetConfigurationVariableValues = targetConfiguration.variables.get("direction").values;

        if (sourceConfigurationVariableValues.contains("input") && targetConfigurationVariableValues.contains("output")) {

            compatibleSourceConfiguration.variables.get("direction").values.add("input");
            compatibleTargetConfiguration.variables.get("direction").values.add("output");

        }

        if (sourceConfigurationVariableValues.contains("output") && targetConfigurationVariableValues.contains("input")) {

            compatibleSourceConfiguration.variables.get("direction").values.add("output");
            compatibleTargetConfiguration.variables.get("direction").values.add("input");

        }

        if (sourceConfigurationVariableValues.contains("bidirectional") && targetConfigurationVariableValues.contains("input")) {

            compatibleSourceConfiguration.variables.get("direction").values.add("bidirectional");
            compatibleTargetConfiguration.variables.get("direction").values.add("input");

        }

        if (sourceConfigurationVariableValues.contains("input") && targetConfigurationVariableValues.contains("bidirectional")) {

            compatibleSourceConfiguration.variables.get("direction").values.add("input");
            compatibleTargetConfiguration.variables.get("direction").values.add("bidirectional");

        }

        if (sourceConfigurationVariableValues.contains("bidirectional") && targetConfigurationVariableValues.contains("output")) {

            compatibleSourceConfiguration.variables.get("direction").values.add("bidirectional");
            compatibleTargetConfiguration.variables.get("direction").values.add("output");

        }

        if (sourceConfigurationVariableValues.contains("output") && targetConfigurationVariableValues.contains("bidirectional")) {

            compatibleSourceConfiguration.variables.get("direction").values.add("output");
            compatibleTargetConfiguration.variables.get("direction").values.add("bidirectional");

        }

        if (sourceConfigurationVariableValues.contains("bidirectional") && targetConfigurationVariableValues.contains("bidirectional")) {

            compatibleSourceConfiguration.variables.get("direction").values.add("bidirectional");
            compatibleTargetConfiguration.variables.get("direction").values.add("bidirectional");

        }
        // </DIRECTION_CONSTRAINT_CHECKS>

        // TODO: null, NONE

        // <VOLTAGE_CONSTRAINT_CHECKS>
        sourceConfigurationVariableValues = sourceConfiguration.variables.get("voltage").values;
        targetConfigurationVariableValues = targetConfiguration.variables.get("voltage").values;

        if (sourceConfigurationVariableValues.contains("ttl") && targetConfigurationVariableValues.contains("ttl")) {

            compatibleSourceConfiguration.variables.get("voltage").values.add("ttl");
            compatibleTargetConfiguration.variables.get("voltage").values.add("ttl");

        }

        if (sourceConfigurationVariableValues.contains("cmos") && targetConfigurationVariableValues.contains("cmos")) {

            compatibleSourceConfiguration.variables.get("voltage").values.add("cmos");
            compatibleTargetConfiguration.variables.get("voltage").values.add("cmos");

        }

        if (sourceConfigurationVariableValues.contains("common") && targetConfigurationVariableValues.contains("common")) {

            compatibleSourceConfiguration.variables.get("voltage").values.add("common");
            compatibleTargetConfiguration.variables.get("voltage").values.add("common");

        }

        // TODO: COMMON - TTL
        // TODO: COMMON - CMOS

        // TODO: null, NONE
        // </VOLTAGE_CONSTRAINT_CHECKS>

        if (Interpreter.ENABLE_VERBOSE_OUTPUT) {
            System.out.println("\n");
        }

        // TODO: Verify this logic for returning "null"

        if ((compatibleSourceConfiguration.variables.get("mode").values.size() == 0 || compatibleTargetConfiguration.variables.get("mode").values.size() == 0)
                || (compatibleSourceConfiguration.variables.get("direction").values.size() == 0 || compatibleTargetConfiguration.variables.get("direction").values.size() == 0)
                || (compatibleSourceConfiguration.variables.get("voltage").values.size() == 0 || compatibleTargetConfiguration.variables.get("voltage").values.size() == 0)) {
            return null;
        } else {
            List<Configuration> compatibleConfigurations = new ArrayList<>();
            compatibleConfigurations.add(compatibleSourceConfiguration);
            compatibleConfigurations.add(compatibleTargetConfiguration);
            return compatibleConfigurations;
        }
    }

    // TODO: rankCompatibleConfigurations(<compatible-configurations-list>)
    // </API>

}
