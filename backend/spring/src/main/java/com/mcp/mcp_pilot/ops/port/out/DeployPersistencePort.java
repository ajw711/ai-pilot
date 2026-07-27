package com.mcp.mcp_pilot.ops.port.out;

import com.mcp.mcp_pilot.ops.application.event.DeploymentRequestedEvent;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResult;

public interface DeployPersistencePort {
    void save(DeploymentRequestedEvent event);
    void updateDeployResult(DeployResult result);
}
