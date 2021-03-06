package com.codeahoy.rocksolid.server.resolution;

import com.codeahoy.rocksolid.server.common.ServerId;
import com.codeahoy.rocksolid.server.common.UserId;

/**
 *
 * Created by umermansoor on 2017-03-05.
 */
public class LookupResult {
    private final UserId userId;
    private final boolean found;
    private final ServerId serverId;

    public LookupResult(UserId u, boolean o, ServerId s) {
        this.userId = u;
        this.found = o;
        this.serverId = s;
    }

    public ServerId getServerId() {
        return serverId;
    }

    public boolean isFound() {
        return found;
    }

    public UserId getUserId() {
        return userId;
    }
}
