package com.mcp.mcp_pilot.ops.adapter.in.event;


import com.mcp.mcp_pilot.ops.port.in.DeployResultUseCase;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResult;
import com.mcp.mcp_pilot.ops.port.in.dto.DeploymentStatus;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class NatsDeployResultListener {

    private final Connection natsConnection;
    private final DeployResultUseCase deployResultUseCase;
    private final JsonMapper jsonMapper;
    private final AsyncTaskExecutor asyncEventExecutor;

    // 롬북 충돌 때문에 수동 생성자를 선언하여 파라미터 앞에 직접 @Qualifier를 지정하기
    public NatsDeployResultListener(
            Connection natsConnection,
            DeployResultUseCase deployResultUseCase,
            JsonMapper jsonMapper,
            @Qualifier("asyncEventExecutor") AsyncTaskExecutor asyncEventExecutor
    ) {
        this.natsConnection = natsConnection;
        this.deployResultUseCase = deployResultUseCase;
        this.jsonMapper = jsonMapper;
        this.asyncEventExecutor = asyncEventExecutor;
    }

        @PostConstruct
    public void initSubscription() {
        try {
            Dispatcher dispatcher = natsConnection.createDispatcher(msg -> {
                String payload = new String(msg.getData(), StandardCharsets.UTF_8);
                try {
                    JsonNode jsonNode = jsonMapper.readTree(payload);

                    // JSON 필드 존재 여부 1차 체크 (NPE 방지)
                    JsonNode trackingNode = jsonNode.get("trackingId");
                    JsonNode statusNode = jsonNode.get("status");
                    JsonNode messageNode = jsonNode.get("message");

                    if (trackingNode == null || trackingNode.asString().isBlank() ||
                            statusNode == null || statusNode.asString().isBlank()) {
                        log.warn("[NATS Listener] 필수 데이터(trackingId/status)가 누락되어 무시합니다. Payload: {}", payload);
                        return;
                    }

                    String trackingId = trackingNode.asString();
                    String statusStr = statusNode.asString();
                    String message = (messageNode != null) ? messageNode.asString() : "";

                    DeploymentStatus status = DeploymentStatus.valueOf(statusStr.toUpperCase());
                    DeployResult result = new DeployResult(trackingId, status, message);

                    // 2. submit() 대신 execute()를 사용하여 비동기 스레드 내부의 예외를 명시적으로 catch해 로깅합니다.
                    asyncEventExecutor.execute(() -> {
                        try {
                            deployResultUseCase.handleDeployResult(result);
                        } catch (Exception e) {
                            log.error("[NATS Listener] 가상 스레드 비동기 결과 처리 실패. TrackingID: {}",
                                    result.trackingId(), e);
                        }
                    });

                } catch (Exception e) {
                    log.error("[NATS Listener] 메시지 처리 실패 (포맷 에러 등)", e);
                }
            });
            dispatcher.subscribe("deploy.result");
        } catch (Exception e) {
            log.error("[NATS Listener] 구독 실패", e);
        }
    }
}
