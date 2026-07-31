package com.mcp.mcp_pilot.ops.port.out;

public interface UserAuthorizationPort {
    boolean canDeploy(Long userId);
}
