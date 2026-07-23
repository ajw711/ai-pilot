package com.mcp.mcp_pilot.ops.application.adapter.out.nats;

import com.mcp.mcp_pilot.knowledge.adapter.out.messaging.NatsPublisher;
import com.mcp.mcp_pilot.ops.application.adapter.out.nats.event.DeployRequestedEvent;
import com.mcp.mcp_pilot.ops.application.adapter.out.nats.mapper.DeployEventMapper;
import com.mcp.mcp_pilot.ops.application.model.DeploySpec;
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
    private final DeployEventMapper mapper;

    @Override
    public void publishDeploy(DeploySpec spec) {
        log.info("[NatsDeployAdapter] NATS 이벤트 발행. TrackingID: {}", spec.getTrackingId());
        try {
            // 내부 배포 스펙 모델을 전송요 이벤트 객체로 변환
            DeployRequestedEvent event = mapper.toEvent(spec);
            // NATS Subject 'ops.deploy.request'로 직렬화하여 전송
            natsPublisher.publish("ops.deploy.request", event);
        } catch (Exception e) {
            // 감싸서 던짐
            throw new DeployPublishException(e);
        }
    }
}
