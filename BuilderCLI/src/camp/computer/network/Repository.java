package camp.computer.network;

import java.util.UUID;

import camp.computer.OLD_construct.Construct_v1;

public class Repository {

    // TODO: Load repository object from file/Redis!

    // Represents the "public" assets repository.
    // Refactor Concept into generic one and allow making custom types
    // Stream in object for revisions, etc. into same OLD_construct. It's a container for object that changes per-revision.

    public static Construct_v1 clone(UUID uuid) { // request/clone/branch
        // TODO:
        return null;
    }

}
