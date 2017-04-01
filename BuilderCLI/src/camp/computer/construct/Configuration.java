package camp.computer.construct;

import java.util.HashMap;

import camp.computer.util.List;
import camp.computer.util.Pair;

/**
 * Constrains the values (or values) that can be assigned <em>simultaneously</em> to each of the
 * configurations <em>in combination with each other</em> uniquely identified by each
 * {@code variableConstraint[i].handle} to the values (or values) specified in {@code variableConstraint[i].values}.
 * <p>
 * Therefore {@code Configuration} specifies a constraint on the valid mutual state assignments among
 * the specified configurations.
 * <p>
 * In other words, this constrains the set of valid <em>combinitoric value permutations</em> that
 * can be assigned to configurations at the same time.
 */
public class Configuration {

    public HashMap<String, List<Structure>> states = new HashMap<>();

    public Configuration(Pair<String, List<Structure>>... featureStateSets) {

        for (int i = 0; i < featureStateSets.length; i++) {
            this.states.put(featureStateSets[i].key, featureStateSets[i].value);
        }

    }

    public Configuration(List<Pair<String, List<Structure>>> variableValueSets) {

        for (int variableIndex = 0; variableIndex < variableValueSets.size(); variableIndex++) {
            this.states.put(variableValueSets.get(variableIndex).key, variableValueSets.get(variableIndex).value);
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
//    public static List<Configuration> computeCompatibleConfigurations(Configuration sourceConfiguration, Configuration targetConfiguration) {
//
//        // computes the "compatible intersection" configurations of the mode configurations of the two configurations
//
//        // <VERBOSE_LOG>
//        // Source Port
////        if (ENABLE_VERBOSE_OUTPUT) {
////
////            System.out.print("? (source) " + sourceConfiguration.mode + ";");
////
////            if (sourceConfiguration.directions == null) {
////                System.out.print("null");
////            } else {
////                for (int i = 0; i < sourceConfiguration.directions.values.size(); i++) {
////                    System.out.print("" + sourceConfiguration.directions.values.clone(i));
////                    if ((i + 1) < sourceConfiguration.directions.values.size()) {
////                        System.out.print(",");
////                    }
////                }
////            }
////            System.out.print(";");
////
////            if (sourceConfiguration.voltages == null) {
////                System.out.print("null");
////            } else {
////                for (int i = 0; i < sourceConfiguration.voltages.values.size(); i++) {
////                    System.out.print("" + sourceConfiguration.voltages.values.clone(i));
////                    if ((i + 1) < sourceConfiguration.voltages.values.size()) {
////                        System.out.print(",");
////                    }
////                }
////            }
////            System.out.println();
////
////            // Target Port(s)
////            System.out.print("  (target) " + targetConfiguration.mode + ";");
////
////            if (targetConfiguration.directions == null) {
////                System.out.print("null");
////            } else {
////                for (int i = 0; i < targetConfiguration.directions.values.size(); i++) {
////                    System.out.print("" + targetConfiguration.directions.values.clone(i));
////                    if ((i + 1) < targetConfiguration.directions.values.size()) {
////                        System.out.print(",");
////                    }
////                }
////            }
////            System.out.print(";");
////
////            if (targetConfiguration.voltages == null) {
////                System.out.print("null");
////            } else {
////                for (int i = 0; i < targetConfiguration.voltages.values.size(); i++) {
////                    System.out.print("" + targetConfiguration.voltages.values.clone(i));
////                    if ((i + 1) < targetConfiguration.voltages.values.size()) {
////                        System.out.print(",");
////                    }
////                }
////            }
////        }
////        // </VERBOSE_LOG>
//
//        // <INITIALIZE_CONSTRAINTS>
//        // Generate updated configurations with features' domains containing only compatible values.
//        Configuration compatibleSourceConfiguration = new Configuration();
//        Configuration compatibleTargetConfiguration = new Configuration();
//
//        // Add source port Configuration with empty variable state sets
//        for (String variableTitle : sourceConfiguration.states.keySet()) {
//            compatibleSourceConfiguration.states.put(variableTitle, new List());
//        }
//
//        // Add target port Configuration with empty variable state sets
//        for (String variableTitle : targetConfiguration.states.keySet()) {
//            compatibleTargetConfiguration.states.put(variableTitle, new List());
//        }
//        // </INITIALIZE_CONSTRAINTS>
//
//        // <MODE_CONSTRAINT_CHECKS>
//        List<String> sourceConfigurationVariableValues = sourceConfiguration.states.get("mode");
//        List<String> targetConfigurationVariableValues = targetConfiguration.states.get("mode");
//
//        if (sourceConfigurationVariableValues.contains("digital") && targetConfigurationVariableValues.contains("digital")) {
//            compatibleSourceConfiguration.states.get("mode").add("digital");
//            compatibleTargetConfiguration.states.get("mode").add("digital");
//        }
//
//        if (sourceConfigurationVariableValues.contains("analog") && targetConfigurationVariableValues.contains("analog")) {
//            compatibleSourceConfiguration.states.get("mode").add("analog");
//            compatibleTargetConfiguration.states.get("mode").add("analog");
//        }
//
//        if (sourceConfigurationVariableValues.contains("pwm") && targetConfigurationVariableValues.contains("pwm")) {
//            compatibleSourceConfiguration.states.get("mode").add("pwm");
//            compatibleTargetConfiguration.states.get("mode").add("pwm");
//        }
//
//        if (sourceConfigurationVariableValues.contains("resistive_touch") && targetConfigurationVariableValues.contains("resistive_touch")) {
//            compatibleSourceConfiguration.states.get("mode").add("resistive_touch");
//            compatibleTargetConfiguration.states.get("mode").add("resistive_touch");
//        }
//
//        if (sourceConfigurationVariableValues.contains("power") && targetConfigurationVariableValues.contains("power")) {
//            compatibleSourceConfiguration.states.get("mode").add("power");
//            compatibleTargetConfiguration.states.get("mode").add("power");
//        }
//
//        if (sourceConfigurationVariableValues.contains("i2c(scl)") && targetConfigurationVariableValues.contains("i2c(scl)")) {
//            compatibleSourceConfiguration.states.get("mode").add("i2c(scl)");
//            compatibleTargetConfiguration.states.get("mode").add("i2c(scl)");
//        }
//
//        if (sourceConfigurationVariableValues.contains("spi(sclk)") && targetConfigurationVariableValues.contains("spi(sclk)")) {
//            compatibleSourceConfiguration.states.get("mode").add("spi(sclk)");
//            compatibleTargetConfiguration.states.get("mode").add("spi(sclk)");
//        }
//
//        if (sourceConfigurationVariableValues.contains("spi(mosi)") && targetConfigurationVariableValues.contains("spi(mosi)")) {
//            compatibleSourceConfiguration.states.get("mode").add("spi(mosi)");
//            compatibleTargetConfiguration.states.get("mode").add("spi(mosi)");
//        }
//
//        if (sourceConfigurationVariableValues.contains("spi(miso)") && targetConfigurationVariableValues.contains("spi(miso)")) {
//            compatibleSourceConfiguration.states.get("mode").add("spi(miso)");
//            compatibleTargetConfiguration.states.get("mode").add("spi(miso)");
//        }
//
//        if (sourceConfigurationVariableValues.contains("spi(ss)") && targetConfigurationVariableValues.contains("spi(ss)")) {
//            compatibleSourceConfiguration.states.get("mode").add("spi(ss)");
//            compatibleTargetConfiguration.states.get("mode").add("spi(ss)");
//        }
//
//        if (sourceConfigurationVariableValues.contains("uart(tx)") && targetConfigurationVariableValues.contains("uart(rx)")) {
//            compatibleSourceConfiguration.states.get("mode").add("uart(tx)");
//            compatibleTargetConfiguration.states.get("mode").add("uart(rx)");
//        }
//
//        if (sourceConfigurationVariableValues.contains("uart(rx)") && targetConfigurationVariableValues.contains("uart(tx)")) {
//            compatibleSourceConfiguration.states.get("mode").add("uart(rx)");
//            compatibleTargetConfiguration.states.get("mode").add("uart(tx)");
//        }
//        // </MODE_CONSTRAINT_CHECKS>
//
//        // <DIRECTION_CONSTRAINT_CHECKS>
//        sourceConfigurationVariableValues = sourceConfiguration.states.get("direction");
//        targetConfigurationVariableValues = targetConfiguration.states.get("direction");
//
//        if (sourceConfigurationVariableValues.contains("input") && targetConfigurationVariableValues.contains("output")) {
//
//            compatibleSourceConfiguration.states.get("direction").add("input");
//            compatibleTargetConfiguration.states.get("direction").add("output");
//
//        }
//
//        if (sourceConfigurationVariableValues.contains("output") && targetConfigurationVariableValues.contains("input")) {
//
//            compatibleSourceConfiguration.states.get("direction").add("output");
//            compatibleTargetConfiguration.states.get("direction").add("input");
//
//        }
//
//        if (sourceConfigurationVariableValues.contains("bidirectional") && targetConfigurationVariableValues.contains("input")) {
//
//            compatibleSourceConfiguration.states.get("direction").add("bidirectional");
//            compatibleTargetConfiguration.states.get("direction").add("input");
//
//        }
//
//        if (sourceConfigurationVariableValues.contains("input") && targetConfigurationVariableValues.contains("bidirectional")) {
//
//            compatibleSourceConfiguration.states.get("direction").add("input");
//            compatibleTargetConfiguration.states.get("direction").add("bidirectional");
//
//        }
//
//        if (sourceConfigurationVariableValues.contains("bidirectional") && targetConfigurationVariableValues.contains("output")) {
//
//            compatibleSourceConfiguration.states.get("direction").add("bidirectional");
//            compatibleTargetConfiguration.states.get("direction").add("output");
//
//        }
//
//        if (sourceConfigurationVariableValues.contains("output") && targetConfigurationVariableValues.contains("bidirectional")) {
//
//            compatibleSourceConfiguration.states.get("direction").add("output");
//            compatibleTargetConfiguration.states.get("direction").add("bidirectional");
//
//        }
//
//        if (sourceConfigurationVariableValues.contains("bidirectional") && targetConfigurationVariableValues.contains("bidirectional")) {
//
//            compatibleSourceConfiguration.states.get("direction").add("bidirectional");
//            compatibleTargetConfiguration.states.get("direction").add("bidirectional");
//
//        }
//        // </DIRECTION_CONSTRAINT_CHECKS>
//
//        // TODO: null, NONE
//
//        // <VOLTAGE_CONSTRAINT_CHECKS>
//        sourceConfigurationVariableValues = sourceConfiguration.states.get("voltage");
//        targetConfigurationVariableValues = targetConfiguration.states.get("voltage");
//
//        if (sourceConfigurationVariableValues.contains("ttl") && targetConfigurationVariableValues.contains("ttl")) {
//
//            compatibleSourceConfiguration.states.get("voltage").add("ttl");
//            compatibleTargetConfiguration.states.get("voltage").add("ttl");
//
//        }
//
//        if (sourceConfigurationVariableValues.contains("cmos") && targetConfigurationVariableValues.contains("cmos")) {
//
//            compatibleSourceConfiguration.states.get("voltage").add("cmos");
//            compatibleTargetConfiguration.states.get("voltage").add("cmos");
//
//        }
//
//        if (sourceConfigurationVariableValues.contains("common") && targetConfigurationVariableValues.contains("common")) {
//
//            compatibleSourceConfiguration.states.get("voltage").add("common");
//            compatibleTargetConfiguration.states.get("voltage").add("common");
//
//        }
//
//        // TODO: COMMON - TTL
//        // TODO: COMMON - CMOS
//
//        // TODO: null, NONE
//        // </VOLTAGE_CONSTRAINT_CHECKS>
//
//        if (Interpreter.ENABLE_VERBOSE_OUTPUT) {
//            System.out.println("\n");
//        }
//
//        // TODO: Verify this logic for returning "null"
//
//        if ((compatibleSourceConfiguration.states.get("mode").size() == 0 || compatibleTargetConfiguration.states.get("mode").size() == 0)
//                || (compatibleSourceConfiguration.states.get("direction").size() == 0 || compatibleTargetConfiguration.states.get("direction").size() == 0)
//                || (compatibleSourceConfiguration.states.get("voltage").size() == 0 || compatibleTargetConfiguration.states.get("voltage").size() == 0)) {
//            return null;
//        } else {
//            List<Configuration> compatibleConfigurations = new List<>();
//            compatibleConfigurations.add(compatibleSourceConfiguration);
//            compatibleConfigurations.add(compatibleTargetConfiguration);
//            return compatibleConfigurations;
//        }
//    }

    // TODO: rankCompatibleConfigurations(<compatible-configurations-list>)
    // </API>

}
