package com.mcp.mcp_pilot.ops.application.service;

import com.mcp.mcp_pilot.ops.application.model.DeploySpec;
import com.mcp.mcp_pilot.ops.application.policy.DeploymentPolicy;
import com.mcp.mcp_pilot.ops.exception.DeployPublishException;
import com.mcp.mcp_pilot.ops.port.in.DeployUseCase;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResult;
import com.mcp.mcp_pilot.ops.port.out.DeployPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeployService implements DeployUseCase {

    private final DeployPort deployPort;
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

        // Out Port로 이벤트 전달 및 안전성 처리
        try {
            deployPort.publishDeploy(spec);
            return DeployResult.success(
                    trackingId,
                    "쿠버네티스 배포 이벤트가 비동기로 정상 발행되었습니다."
            );
        } catch (DeployPublishException e) {
            log.error("[DeployService] 배포 이벤트 전송 실패. TrackingID: {}", trackingId, e);
            return DeployResult.fail(
                    trackingId,
                    "배포 연동 과정에서 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }
}
