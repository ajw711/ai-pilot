package com.mcp.mcp_pilot.ops.port.in.dto;

public record DeployResult(
        String trackingId,
        DeployResultStatus deployStatus,
        String message
) {
    public static DeployResult success(String trackingId, String message) {
        return new DeployResult(trackingId, DeployResultStatus.ACCEPTED, message);
    }

    public static DeployResult fail(String trackingId, String message) {
        return new DeployResult(trackingId, DeployResultStatus.FAILED, message);
    }

    public static DeployResult rejected(String trackingId, String message) {
        return new DeployResult(trackingId, DeployResultStatus.REJECTED, message);
    }
}
