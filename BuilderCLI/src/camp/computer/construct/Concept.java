package camp.computer.construct;

import java.util.HashMap;
import java.util.List;

import camp.computer.Application;
import camp.computer.util.terminal.Color;
import camp.computer.workspace.Manager;

public class Concept extends Identifier {

    // TODO: public boolean isPrimitive = false;

    public Type type = null;

    public HashMap<String, Feature> features = new HashMap<>(); // TODO: Remove? Remove setupConfiguration?

    // TODO: configuration(s) : assign state to multiple features <-- do this for _Container_ not Concept

    private Concept() {
    }

    public static Concept add(Type type) {
        if (!has(type)) {
            Concept concept = new Concept();
            concept.type = type;
            long uid = Manager.add(concept);
            return concept;
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

    public static Concept get(Type type) {
        if (has(type)) {
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
     * Returns true of a construct has been defined with the specified {@code identifier}.
     *
     * @param type
     * @return True
     */
    public static boolean has(Type type) {
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
        return Color.ANSI_BLUE + Color.ANSI_BOLD_ON + type + Color.ANSI_RESET + " (id:" + uid + ")";
    }

}
