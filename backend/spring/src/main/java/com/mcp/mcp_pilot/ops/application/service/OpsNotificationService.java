package com.mcp.mcp_pilot.ops.application.service;

import com.mcp.mcp_pilot.ops.port.in.OpsNotificationSubscribeUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class OpsNotificationService implements OpsNotificationSubscribeUseCase {

    @Override
    public SseEmitter subscribe(Long userId) {
        log.info("[OpsNotificationService] 임시 SSE 구독 요청 수신 - UserId: {}", userId);

        // 60초 타임아웃 SSE Emitter 생성 및 연결 이벤트 발행
        SseEmitter emitter = new SseEmitter(60_000L);
        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECT")
                    .data("SSE Connected successfully (Dummy)"));
        } catch (Exception e) {
            log.warn("[OpsNotificationService] 임시 SSE 전송 실패", e);
        }
        return emitter;
    }
}