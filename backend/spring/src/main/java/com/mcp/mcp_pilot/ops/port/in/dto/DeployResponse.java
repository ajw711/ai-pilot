package com.mcp.mcp_pilot.ops.port.in.dto;

public record DeployResponse(
        String trackingId,
        DeployResultStatus deployStatus,
        String message
) {
    public static DeployResponse success(String trackingId, String message) {
        return new DeployResponse(trackingId, DeployResultStatus.ACCEPTED, message);
    }

    public static DeployResponse fail(String trackingId, String message) {
        return new DeployResponse(trackingId, DeployResultStatus.FAILED, message);
    }

    public static DeployResponse rejected(String trackingId, String message) {
        return new DeployResponse(trackingId, DeployResultStatus.REJECTED, message);
    }
}
