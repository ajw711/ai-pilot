package com.mcp.mcp_pilot.ops.application.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeploySpec {

    private final String trackingId;
    private final String appName;
    private final String image;
    private final String tag;
    private final int replicas;
    private final String namespace;
    private final String cpuLimit;
    private final String memoryLimit;

}
