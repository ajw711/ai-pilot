package com.mcp.mcp_pilot.ops.port.in.dto;

public record DeployResult(
        String trackingId,
        DeploymentStatus status,
        String message
) {}