package com.mcp.mcp_pilot.ops.port.out;

import com.mcp.mcp_pilot.ops.adapter.out.nats.dto.DiagnoseRequest;
import com.mcp.mcp_pilot.ops.port.in.dto.DiagnoseResult;

public interface DiagnosePort {

    DiagnoseResult requestDiagnose(DiagnoseRequest request);
}
