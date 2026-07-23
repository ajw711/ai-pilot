package com.mcp.mcp_pilot.ops.port.out;

public interface DeployPort {
    void publish(String eventPayload);
}
