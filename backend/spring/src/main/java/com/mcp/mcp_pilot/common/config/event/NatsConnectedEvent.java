package com.mcp.mcp_pilot.common.config.event;

import org.springframework.context.ApplicationEvent;

public class NatsConnectedEvent extends ApplicationEvent {
    public NatsConnectedEvent(Object source) {
        super(source);
    }
}
