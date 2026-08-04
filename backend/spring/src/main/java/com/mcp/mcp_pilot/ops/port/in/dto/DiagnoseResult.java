package com.mcp.mcp_pilot.ops.port.in.dto;

import java.util.List;

public record DiagnoseResult(
        String trackingId,
        String namespace,
        List<String> pods,
        List<String> warningEvents,
        String logs,
        String status,
        String message
) {
}
