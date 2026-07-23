package com.mcp.mcp_pilot.ops.application.adapter.out.nats.event;

public record DeployRequestedEvent(
        String trackingId,
        String appName,
        String image,
        String tag,
        int replicas,
        String namespace,
        String cpuLimit,
        String memoryLimit,
        long timestamp
) {
}
