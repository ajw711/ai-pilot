package com.mcp.mcp_pilot.ops.adapter.out.notification;

import com.mcp.mcp_pilot.ops.adapter.out.notification.dto.NotificationPayload;
import com.mcp.mcp_pilot.ops.port.out.OpsNotificationPort;
import com.mcp.mcp_pilot.ops.port.out.OpsNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class OpsNotificationAdapter implements OpsNotificationPort {

    private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    @Override
    public void sendToUser(OpsNotificationEvent event) {
        Long userId = event.userId();
        List<SseEmitter> list = userEmitters.get(userId);
        if (list == null || list.isEmpty()) {
            log.warn("[OpsNotificationAdapter] 연결된 Emitter 세션이 없습니다. UserId: {}", userId);
            return;
        }

        log.info("[OpsNotificationAdapter] 1:1 유니캐스트 전송. UserId: {}, 활성 탭: {}개", userId, list.size());
        NotificationPayload payload = new NotificationPayload(
                event.type(),
                event.trackingId(),
                event.appName(),
                event.status(),
                event.message()
        );
        for (SseEmitter emitter : list) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ops-result")
                        .data(payload));
            } catch (Exception e) {
                cleanupEmitter(userId, emitter);
            }
        }
    }

    @Scheduled(fixedDelay = 20000)
    public void sendHeartbeat() {
        userEmitters.forEach((userId, list) -> {
            for (SseEmitter emitter : list) {
                try {
                    emitter.send(SseEmitter.event().comment("keepalive"));
                } catch (IOException e) {
                    cleanupEmitter(userId, emitter);
                }
            }
        });
    }

    @Override
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L); // 30분
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        Runnable cleanup = () -> cleanupEmitter(userId, emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError((e) -> cleanup.run());

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"));
        } catch (IOException e) {
            cleanupEmitter(userId, emitter);
        }
        return emitter;
    }

    public void cleanupEmitter(Long userId, SseEmitter emitter) {
        List<SseEmitter> list = userEmitters.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                userEmitters.remove(userId, list);
            }
            log.debug(
                    "[SSE] Emitter cleanup. userId={}, remaining={}",
                    userId,
                    list.size()
            );
        }
        try {
            emitter.complete();
        } catch (Exception e) {
            log.debug(
                    "[SSE] Emitter already completed. userId={}",
                    userId,
                    e
            );
        }
    }
}
