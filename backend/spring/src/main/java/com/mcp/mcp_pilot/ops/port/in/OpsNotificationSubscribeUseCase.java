package com.mcp.mcp_pilot.ops.port.in;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface OpsNotificationSubscribeUseCase {
    SseEmitter subscribe(Long userId);
}
