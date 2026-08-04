package com.mcp.mcp_pilot.ops.adapter.out.nats;

import com.mcp.mcp_pilot.ops.exception.DeployPublishException;
import com.mcp.mcp_pilot.ops.port.out.DeployPort;
import io.nats.client.Connection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class NatsDeployAdapter implements DeployPort {

    private final Connection natsConnection; // ★ 직접 커넥션 주입받기

    @Override
    public void publish(String eventPayload) {
        log.info("[NatsDeployAdapter] NATS 브로커 전송 시작.");
        try {
            // 이중 직렬화 우회: 이미 완성된 JSON String의 바이트를 그대로 릴레이
            byte[] data = eventPayload.getBytes(StandardCharsets.UTF_8);
            natsConnection.publish("ops.deploy.request", data);

            log.info("[NatsDeployAdapter] NATS 브로커 전송 성공.");
        } catch (Exception e) {
            log.error("[NatsDeployAdapter] NATS 브로커 전송 실패.", e);
            throw new DeployPublishException(e);
        }
    }
}
