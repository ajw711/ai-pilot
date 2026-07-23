package com.mcp.mcp_pilot.ops.port.in.dto;

public record DeployResult(
        String trackingId,
        DeployStatus deployStatus,
        String message
) {
    public static DeployResult success(String trackingId, String message) {
        return new DeployResult(trackingId, DeployStatus.ACCEPTED, message);
    }

    public static DeployResult fail(String trackingId, String message) {
        return new DeployResult(trackingId, DeployStatus.FAILED, message);
    }

    public static DeployResult rejected(String trackingId, String message) {
        return new DeployResult(trackingId, DeployStatus.REJECTED, message);
    }
}
