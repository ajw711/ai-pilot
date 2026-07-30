package com.mcp.mcp_pilot.ops.port.out;

import com.mcp.mcp_pilot.ops.adapter.out.notification.dto.OperationType;

public record OpsNotificationEvent(
        Long userId,
        OperationType type,
        String trackingId,
        String appName,
        String status,
        String message
) {}
