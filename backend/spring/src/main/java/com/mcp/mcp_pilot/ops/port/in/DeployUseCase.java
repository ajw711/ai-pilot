package com.mcp.mcp_pilot.ops.port.in;

import com.mcp.mcp_pilot.ops.port.in.dto.DeployCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResult;

public interface DeployUseCase {
    DeployResult deploy(DeployCommand command);
}
