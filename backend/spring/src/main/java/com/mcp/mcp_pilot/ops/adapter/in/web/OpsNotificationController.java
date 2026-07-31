package com.mcp.mcp_pilot.ops.adapter.in.web;

import com.mcp.mcp_pilot.common.security.CustomUserPrincipal;
import com.mcp.mcp_pilot.ops.port.in.OpsNotificationSubscribeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OpsNotificationController {

    private final OpsNotificationSubscribeUseCase subscribeUseCase;

    @GetMapping(value = "/api/v1/ops/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal CustomUserPrincipal principal) {
        log.info("[OpsNotificationController] SSE 구독 요청 접수.");
        Long userId = principal.getId();
        return subscribeUseCase.subscribe(userId);
    }
}
