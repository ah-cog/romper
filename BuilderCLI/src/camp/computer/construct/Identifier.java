package camp.computer.construct;

import java.util.UUID;

public class Identifier {

    public UUID uuid = UUID.randomUUID(); // universal identifier (unique among all in central repo)
    public UUID versionUuid = UUID.randomUUID();

    public long uid = -1L; // Manager_v1.elementCounter++; // manager/cache UID
    public long versionUid = -1L;

    public String tag = null; // label/identifier(s)

}
