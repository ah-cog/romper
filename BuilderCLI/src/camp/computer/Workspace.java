package camp.computer;

import java.util.ArrayList;
import java.util.List;

import camp.computer.OLD_construct.Construct_v1;
import camp.computer.OLD_construct.ControllerConstruct;
import camp.computer.OLD_construct.DeviceConstruct;
import camp.computer.OLD_construct.OperationConstruct;
import camp.computer.OLD_construct.PathConstruct;
import camp.computer.OLD_construct.PortConstruct;
import camp.computer.OLD_construct.ProjectConstruct;
import camp.computer.OLD_construct.TaskConstruct;

/**
 * A {@code Workspace} is provided to the user by default and defines a namespace in which the user creates their
 * projects. Conceptually, the {@code Workspace} sits alongside the user's {@code Inventory}, {@code Notebook}
 * (<em>i.e.,</em> the user's thoughts), {@code Shop} (<em>i.e.,</em> the user's online store for purchasing items).
 * <p>
 * TODO: Consider modeling the Inventory's Items, Notebook's Notes as a list.
 */
public class Workspace extends Construct_v1 {

    // TODO: Reference to User/Account that owns the Workspace.

    // <CACHE>
    public List<ProjectConstruct> projectConstructs = new ArrayList<>();
    // </CACHE>

    // TODO: MOVE BELOW INTO INTERPRETER!!!
    // <CONTEXT>
    Construct_v1 construct = null;

    OperationConstruct operationConstruct = null;

    // Automatically updated each time a OLD_construct is created.
    ProjectConstruct lastProjectConstruct = null;
    DeviceConstruct lastDeviceConstruct = null;
    PortConstruct lastPortConstruct = null;
    PathConstruct lastPathConstruct = null;
    ControllerConstruct lastControllerConstruct = null;
    TaskConstruct lastTaskConstruct = null;
    // </CONTEXT>

    public Workspace() {
        super();
        this.type = "workspace";
    }

}
