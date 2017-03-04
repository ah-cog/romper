package camp.computer.OLD_construct;

import java.util.ArrayList;
import java.util.List;

public class ControllerConstruct extends Construct_v1 {

    public List<TaskConstruct> taskConstructs = new ArrayList<>();

    public ControllerConstruct() {
        super();
        this.type = "controller";
    }

}
