package com.mcp.mcp_pilot.ops.port.out;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface OpsNotificationPort {
    void sendToUser(OpsNotificationEvent event);
    SseEmitter subscribe(Long userId);
}
