package com.mcp.mcp_pilot.ops.port.in;

import com.mcp.mcp_pilot.ops.port.in.dto.DeployCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResponse;

public interface DeployUseCase {
    DeployResponse deploy(DeployCommand command);
}
