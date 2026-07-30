package com.mcp.mcp_pilot.ops.adapter.out.notification.dto;

public record NotificationPayload(
        OperationType type,
        String trackingId,
        String appName,
        String status,
        String message
) {}
