package com.mcp.mcp_pilot.ops.port.in;

import com.mcp.mcp_pilot.ops.port.in.dto.DiagnoseCommand;

public interface K8sDiagnoseUseCase {
    String diagnose(DiagnoseCommand command, Long requestedBy);
}
