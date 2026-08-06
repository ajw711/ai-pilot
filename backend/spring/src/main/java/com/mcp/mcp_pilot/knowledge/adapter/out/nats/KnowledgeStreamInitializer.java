package com.mcp.mcp_pilot.knowledge.adapter.out.nats;

import com.mcp.mcp_pilot.common.config.NatsConnectionHolder;
import com.mcp.mcp_pilot.common.config.event.NatsConnectedEvent;
import io.nats.client.JetStreamManagement;
import io.nats.client.api.RetentionPolicy;
import io.nats.client.api.StorageType;
import io.nats.client.api.StreamConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeStreamInitializer {

    private final NatsConnectionHolder natsConnectionHolder;

    @EventListener(NatsConnectedEvent.class)
    public void onNatsConnected(NatsConnectedEvent event) {
        initStream();
    }

    public void initStream() {
        try {
            JetStreamManagement jsm = natsConnectionHolder.getJetStreamManagement();
            StreamConfiguration streamConfiguration = StreamConfiguration.builder()
                    .name("KNOWLEDGE_EVENTS")
                    .subjects("knowledge.>")
                    .storageType(StorageType.File)      // 디스크 영속 저장
                    .retentionPolicy(RetentionPolicy.Limits)
                    .maxAge(Duration.ofDays(7))         // 7일간 보관
                    .duplicateWindow(Duration.ofMinutes(10)) // 10분간 messageId 기준 중복 제거
                    .build();

            try {
                jsm.getStreamInfo("KNOWLEDGE_EVENTS");
                log.info("[KnowledgeStreamInitializer] JetStream KNOWLEDGE_EVENTS 이미 존재함");
            } catch (Exception e) {
                // 카프카 브로커 서버에 새로운 Topic(토픽)을 만드세요랑 비슷
                jsm.addStream(streamConfiguration);
                log.info("[KnowledgeStreamInitializer] JetStream KNOWLEDGE_EVENTS 생성 완료");
            }
        } catch (Exception e) {
            log.warn("[KnowledgeStreamInitializer] JetStream 초기화 대기 (NATS 미연결/지연 접속 상태): {}", e.getMessage());
        }
    }
}
