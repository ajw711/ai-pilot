package com.mcp.mcp_pilot.ops.port.out;

import com.mcp.mcp_pilot.ops.application.model.DeploySpec;

public interface DeployPort {
    void publishDeploy(DeploySpec spec);
}
