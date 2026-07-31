package com.mcp.mcp_pilot.ops.adapter.in.web;

import com.mcp.mcp_pilot.ops.port.in.ConsumeSseTicketUseCase;
import com.mcp.mcp_pilot.ops.port.in.OpsNotificationSubscribeUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class OpsNotificationController {

    private final OpsNotificationSubscribeUseCase subscribeUseCase;
    private final ConsumeSseTicketUseCase consumeSseTicketUseCase;


    @GetMapping(value = "/api/v1/ops/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @RequestParam String ticket
    ) {
        log.info("[OpsNotificationController] SSE 연결 요청.");

        Long userId = consumeSseTicketUseCase.consume(ticket);

        return subscribeUseCase.subscribe(userId);
    }
}
