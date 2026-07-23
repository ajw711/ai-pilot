package com.mcp.mcp_pilot.ops.port.out;

import com.mcp.mcp_pilot.ops.application.event.DeploymentRequestedEvent;

public interface DeployPersistencePort {
    void save(DeploymentRequestedEvent event);
}
