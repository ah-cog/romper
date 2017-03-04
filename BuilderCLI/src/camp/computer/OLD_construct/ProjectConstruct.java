package camp.computer.OLD_construct;

import java.util.ArrayList;
import java.util.List;

public class ProjectConstruct extends Construct_v1 {

    // TODO: public boolean isPublished = true; // For publishing projects into portfolio.

    public List<DeviceConstruct> deviceConstructs = new ArrayList<>();

    public List<PathConstruct> pathConstructs = new ArrayList<>();

    public ProjectConstruct() {
        super();
        this.type = "project";
    }

}
