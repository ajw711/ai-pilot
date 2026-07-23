package com.mcp.mcp_pilot.ops.adapter.out.nats;

import com.mcp.mcp_pilot.knowledge.adapter.out.messaging.NatsPublisher;
import com.mcp.mcp_pilot.ops.exception.DeployPublishException;
import com.mcp.mcp_pilot.ops.port.out.DeployPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NatsDeployAdapter implements DeployPort {

    private final NatsPublisher natsPublisher;

    @Override
    public void publish(String eventPayload) {
        log.info("[NatsDeployAdapter] NATS 브로커 전송 시작.");
        try {
            // 변환 로직 없이, 이미 가공 완료되어 DB에 적재되었던 JSON raw string을 그대로 NATS로 릴레이
            natsPublisher.publish("ops.deploy.request", eventPayload);
            log.info("[NatsDeployAdapter] NATS 브로커 전송 성공.");
        } catch (Exception e) {
            log.error("[NatsDeployAdapter] NATS 브로커 전송 실패.", e);
            throw new DeployPublishException(e);
        }
    }
}
