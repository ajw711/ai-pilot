package com.mcp.mcp_pilot.common.config;

import com.mcp.mcp_pilot.common.config.event.NatsConnectedEvent;
import com.mcp.mcp_pilot.knowledge.adapter.out.messaging.config.NatsProperties;
import io.nats.client.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Slf4j
@Getter
@Component
@RequiredArgsConstructor
@EnableConfigurationProperties(NatsProperties.class)
public class NatsConnectionHolder {

    private final NatsProperties natsProperties;
    private final ApplicationEventPublisher eventPublisher;
    // 스레드 A (백그라운드, NATS 연결 시도) → connection 필드에 값 씀
    // 스레드 B (사용자 채팅 요청 처리)      → connection 필드를 읽음
    // 이렇게 한 스레드가 쓰고 다른 스레드가 읽는 상황이라 volatile 없으면
    // 실제로는 연결됐는데 다른 스레드에서는 계속 null로 보이는 이상한 버그가 생길 수 있음
    private volatile Connection connection;

    @EventListener(ApplicationReadyEvent.class)
    public void connectAsync() {
        // Spring Boot 시동을 지연시키지 않도록 가상 스레드에서 백그라운드 연결 시도
        Thread.ofVirtual().name("nats-async-connector").start(this::connectWithRetry);
    }

    private void connectWithRetry() {
        while (connection == null || connection.getStatus() != Connection.Status.CONNECTED) {
            try {
                log.info("[NatsConnectionHolder] NATS 백그라운드 연결 시도 중: {}", natsProperties.url());
                Options options = new Options.Builder()
                        .server(natsProperties.url())
                        .connectionTimeout(Duration.ofSeconds(3))
                        .maxReconnects(-1)
                        .connectionListener((conn, events) -> {
                            log.info("[NatsConnectionHolder] NATS 연결 상태 변경: {}", events);

                            // 최초 연결 및 런타임 재연결 시 단일 이벤트 1회 발행
                            if (events == ConnectionListener.Events.RECONNECTED) {
                                eventPublisher.publishEvent(new NatsConnectedEvent(this));
                            }
                        })
                        .build();
                this.connection = Nats.connect(options);
                log.info("[NatsConnectionHolder] NATS 연결 성공! 진단 파이프라인 활성화.");
                eventPublisher.publishEvent(new NatsConnectedEvent(this));
            } catch (Exception e) {
                log.warn("[NatsConnectionHolder] NATS 연결 실패 ({}), 5초 후 재시도합니다.", e.getMessage());
                sleep(5000);
            }
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public JetStream getJetStream() throws IOException {
        return connection.jetStream();
    }

    public JetStreamManagement getJetStreamManagement() throws IOException {
        return connection.jetStreamManagement();
    }

}
