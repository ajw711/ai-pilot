package com.mcp.mcp_pilot.ops.adapter.out.nats.dto;

public record DiagnoseRequest(
        String trackingId,
        String namespace,
        String podName,
        Long requestedBy
) {
}
