package com.mcp.mcp_pilot.ops.application.service;

import com.mcp.mcp_pilot.ops.application.event.DeploymentRequestedEvent;
import com.mcp.mcp_pilot.ops.application.model.DeploySpec;
import com.mcp.mcp_pilot.ops.application.policy.DeploymentPolicy;
import com.mcp.mcp_pilot.ops.exception.DeployPersistenceException;
import com.mcp.mcp_pilot.ops.port.in.DeployUseCase;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResult;
import com.mcp.mcp_pilot.ops.port.out.DeployPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeployService implements DeployUseCase {

    private final DeployPersistencePort deployPersistencePort;
    private final DeploymentPolicy deploymentPolicy;

    @Override
    public DeployResult deploy(DeployCommand command) {
        // 비동기 작업 추적용 고유ID 생성
        String trackingId = "DEPLOY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (command.appName() == null || command.appName().isBlank() ||
                command.image() == null || command.image().isBlank()) {
            return DeployResult.fail(trackingId, "배포 실패: 애플리케이션 이름과 컨테이너 이미지는 필수 입력값입니다.");
        }

        log.info("[DeployService] 배포 프로세스 가동. TrackingID: {}, AppName: {}", trackingId, command.appName());

        String tag = (command.tag() == null || command.tag().isBlank()) ? "latest" : command.tag();
        String namespace = (command.namespace() == null || command.namespace().isBlank()) ? "default" : command.namespace();
        int replicas = (command.replicas() <=0) ? 1 : command.replicas();

        DeploySpec spec = DeploySpec.builder()
                .trackingId(trackingId)
                .appName(command.appName())
                .image(command.image())
                .tag(tag)
                .replicas(replicas)
                .namespace(namespace)
                .cpuLimit(deploymentPolicy.cpuLimit()) // Record getter 호출
                .memoryLimit(deploymentPolicy.memoryLimit()) // Record getter 호출
                .build();

        DeploymentRequestedEvent event = DeploymentRequestedEvent.from(spec);
        try {
            // 비즈니스 레벨에서는 JSON 직렬화에 대해 전혀 모름
            // DB + Outbox 단일 트랜잭션 저장 호출
            deployPersistencePort.save(event);

            return DeployResult.success(
                    trackingId,
                    "배포 요청이 정상적으로 접수되었습니다. (비동기 처리 대기 중)"
            );
        } catch (DeployPersistenceException e) {
            log.error("[DeployService] 배포 요청 저장 실패. TrackingID: {}", trackingId, e);
            return DeployResult.fail(
                    trackingId,
                    e.getErrorCode().getMessage()
            );
        }
    }
}
