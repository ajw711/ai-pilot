package com.mcp.mcp_pilot.ops.application.adapter.out.nats.mapper;

import com.mcp.mcp_pilot.ops.application.adapter.out.nats.event.DeployRequestedEvent;
import com.mcp.mcp_pilot.ops.application.model.DeploySpec;
import org.springframework.stereotype.Component;

@Component
public class DeployEventMapper {

    public DeployRequestedEvent toEvent(DeploySpec spec) {
        return new DeployRequestedEvent(
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
