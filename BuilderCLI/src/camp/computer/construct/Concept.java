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

    private static Concept create(Type type) {
        Concept concept = new Concept(type);
        long uid = Manager.add(concept);
        if (uid != -1) {
            return concept;
        }
        return null;
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
        return type + " (id: " + uid + " -> uuid:" + uuid + ")";
        // return type + " (id:" + uid + ")";
    }

    public String toColorString() {
        return Color.ANSI_BLUE + Color.ANSI_BOLD_ON + type + Color.ANSI_RESET + " (id: " + uid + ")";
        // return Color.ANSI_BLUE + Color.ANSI_BOLD_ON + type + Color.ANSI_RESET + " (id:" + uid + " -> uuid: " + uuid + ")";
    }

}
