package com.mcp.mcp_pilot.ops.port.in;

import com.mcp.mcp_pilot.ops.port.in.dto.DeployResult;

public interface DeployResultUseCase {
    void handleDeployResult(DeployResult result);
}
