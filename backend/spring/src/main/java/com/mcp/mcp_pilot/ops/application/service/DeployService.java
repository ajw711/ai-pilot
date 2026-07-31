package com.mcp.mcp_pilot.ops.application.service;

import com.mcp.mcp_pilot.knowledge.port.out.NotionPublishPort;
import com.mcp.mcp_pilot.ops.application.event.DeploymentRequestedEvent;
import com.mcp.mcp_pilot.ops.application.model.DeploySpec;
import com.mcp.mcp_pilot.ops.application.policy.DeploymentPolicy;
import com.mcp.mcp_pilot.ops.exception.DeployPersistenceException;
import com.mcp.mcp_pilot.ops.port.in.DeployResultUseCase;
import com.mcp.mcp_pilot.ops.port.in.DeployUseCase;
import com.mcp.mcp_pilot.ops.port.in.OpsNotificationSubscribeUseCase;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResponse;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResult;
import com.mcp.mcp_pilot.ops.port.out.OpsNotificationPort;
import com.mcp.mcp_pilot.ops.port.out.DeployPersistencePort;
import com.mcp.mcp_pilot.ops.port.out.OpsNotificationEvent;
import com.mcp.mcp_pilot.ops.adapter.out.notification.dto.OperationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

import com.mcp.mcp_pilot.ops.port.out.UserAuthorizationPort;
import com.mcp.mcp_pilot.user.exception.UserException;
import com.mcp.mcp_pilot.common.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeployService implements DeployUseCase, DeployResultUseCase, OpsNotificationSubscribeUseCase {

    private final DeployPersistencePort deployPersistencePort;
    private final OpsNotificationPort notificationPort;
    private final DeploymentPolicy deploymentPolicy;
    private final UserAuthorizationPort userAuthorizationPort;

    @Override
    public DeployResponse deploy(DeployCommand command, Long requestedBy) {
        // 실시간 권한 재검증 가드 (위험 API DB 재조회)
        if (!userAuthorizationPort.canDeploy(requestedBy)) {
            log.warn("[DeployService] 인프라 배포 권한 거부. UserId: {}", requestedBy);
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }

        // 비동기 작업 추적용 고유ID 생성
        String trackingId = "DEPLOY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if (command.appName() == null || command.appName().isBlank() ||
                command.image() == null || command.image().isBlank()) {
            return DeployResponse.fail(trackingId, "배포 실패: 애플리케이션 이름과 컨테이너 이미지는 필수 입력값입니다.");
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

        DeploymentRequestedEvent event = DeploymentRequestedEvent.create(spec, requestedBy);
        try {
            // 비즈니스 레벨에서는 JSON 직렬화에 대해 전혀 모름
            // DB + Outbox 단일 트랜잭션 저장 호출
            deployPersistencePort.save(event);

            return DeployResponse.success(
                    trackingId,
                    "배포 요청이 정상적으로 접수되었습니다. (비동기 처리 대기 중)"
            );
        } catch (DeployPersistenceException e) {
            log.error("[DeployService] 배포 요청 저장 실패. TrackingID: {}", trackingId, e);
            return DeployResponse.fail(
                    trackingId,
                    e.getErrorCode().getMessage()
            );
        }
    }

    @Override
    public void handleDeployResult(DeployResult result){
        log.info("[DeployService] 결과 처리 시작. 스레드명: {}, 가상 스레드 여부: {}",
                Thread.currentThread().getName(),
                Thread.currentThread().isVirtual());

        // deployPersistencePort(주입된 필드명) 호출 및 결과 전달
        deployPersistencePort.updateDeployResult(result);

        notificationPort.sendToUser(new OpsNotificationEvent(
                result.requestedBy(),
                OperationType.DEPLOY,
                result.trackingId(),
                "nginx",
                result.status().name(),
                result.message()
        ));
    }

    @Override
    public SseEmitter subscribe(Long userId) {
        return notificationPort.subscribe(userId);
    }
}
