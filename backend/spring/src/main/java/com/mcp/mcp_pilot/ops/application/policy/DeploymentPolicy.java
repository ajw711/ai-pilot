package com.mcp.mcp_pilot.ops.application.policy;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ops.defaults")
public record DeploymentPolicy(
        @DefaultValue("500m") String cpuLimit,
        @DefaultValue("500m") String memoryLimit
) {
}
