package camp.computer.OLD_construct;

import java.util.ArrayList;
import java.util.List;

public class DeviceConstruct extends Construct_v1 {

    public List<PortConstruct> portConstructs = new ArrayList<>();

    public ControllerConstruct controllerConstruct = new ControllerConstruct();

    public DeviceConstruct() {
        super();
        this.type = "device";
    }

    // <API>
    public static List<PortConstruct> getUnassignedPorts(DeviceConstruct deviceConstruct) {

        List<PortConstruct> unassignedPorts = new ArrayList<>();

        for (int i = 0; i < deviceConstruct.portConstructs.size(); i++) {
            if (PortConstruct.isUnassigned(deviceConstruct.portConstructs.get(i))) {
                unassignedPorts.add(deviceConstruct.portConstructs.get(i));
            }
        }

        return unassignedPorts;
    }
    // </API>

}
