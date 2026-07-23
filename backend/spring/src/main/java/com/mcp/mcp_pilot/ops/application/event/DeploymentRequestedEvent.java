package com.mcp.mcp_pilot.ops.application.event;

import com.mcp.mcp_pilot.ops.application.model.DeploySpec;

public record DeploymentRequestedEvent(
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
    public static DeploymentRequestedEvent from(DeploySpec spec) {
        return new DeploymentRequestedEvent(
                spec.getTrackingId(),
                spec.getAppName(),
                spec.getImage(),
                spec.getTag(),
                spec.getReplicas(),
                spec.getNamespace(),
                spec.getCpuLimit(),
                spec.getMemoryLimit(),
                System.currentTimeMillis()
        );
    }
}