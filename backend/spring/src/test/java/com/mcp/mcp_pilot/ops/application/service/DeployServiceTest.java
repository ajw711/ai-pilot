package com.mcp.mcp_pilot.ops.application.service;

import com.mcp.mcp_pilot.ops.application.event.DeploymentRequestedEvent;
import com.mcp.mcp_pilot.ops.application.policy.DeploymentPolicy;
import com.mcp.mcp_pilot.ops.exception.DeployPersistenceException;
import com.mcp.mcp_pilot.ops.exception.DeployPublishException;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResult;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResultStatus;
import com.mcp.mcp_pilot.ops.port.out.DeployPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeployServiceTest {

    @Mock
    private DeployPersistencePort deployPersistencePort;  // 외부 시스템(NATS) MOCK 사용

    private DeployService deployService;

    @BeforeEach
    void setUp() {
        DeploymentPolicy deploymentPolicy = new DeploymentPolicy("500m", "512Mi");
        deployService = new DeployService(deployPersistencePort, deploymentPolicy);
    }

    @Test
    @DisplayName("정상적인 배포 커맨드가 입력되면  DeploymentRequestedEvent가 생성되고 DB에 저장 성공 결과를 반환한다.")
    void deploySuccess() {
        // Given
        DeployCommand command = new DeployCommand("my-app", "nginx", "1.21", 3, "production");
        ArgumentCaptor<DeploymentRequestedEvent> eventCaptor = ArgumentCaptor.forClass(DeploymentRequestedEvent.class);

        // When
        DeployResult result = deployService.deploy(command);

        // Then
        assertNotNull(result);
        assertTrue(result.trackingId().startsWith("DEPLOY-"));
        assertEquals(DeployResultStatus.ACCEPTED, result.deployStatus());

        // Port가 1회 호출되었는지 검증
        verify(deployPersistencePort, times(1)).save(eventCaptor.capture());

        // DeploySpec 객체를 가져와 내부 필드들이 잘 맵핑되었는지 정밀 검증
        DeploymentRequestedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(result.trackingId(), capturedEvent.trackingId());
        assertEquals("my-app", capturedEvent.appName());
        assertEquals("nginx", capturedEvent.image());
        assertEquals("1.21", capturedEvent.tag());
        assertEquals(3, capturedEvent.replicas());
        assertEquals("production", capturedEvent.namespace());

        //  실제 주입한 policy 인스턴스의 정책이 올바르게 맵핑되었는지 확인
        assertEquals("500m", capturedEvent.cpuLimit());
        assertEquals("512Mi", capturedEvent.memoryLimit());
    }

    @Test
    @DisplayName("필수 값인 appName 또는 image가 누락되면 전송을 포기하고 FAILED 결과를 반환")
    void deployFail() {
        // Given
        DeployCommand command = new DeployCommand(null, "nginx", "latest", 1, "default");

        // When
        DeployResult result = deployService.deploy(command);

        // Then
        assertNotNull(result);
        assertEquals(DeployResultStatus.FAILED, result.deployStatus());
        assertTrue(result.message().contains("필수 입력값입니다"));
        // 포트가 단 한 번도 호출되지 않았음을 보장
        verify(deployPersistencePort, never()).save(any());
    }

    @Test
    @DisplayName("영속성 저장 아댑터에서 DeployPersistenceException이 발생하면 FAILED 결과를 반환하고 트래킹은 유지한다")
    void deployFailPersistenceError() {
        // Given
        DeployCommand command = new DeployCommand("my-app", "nginx", "latest", 1, "default");

        // DB 저장 시뮬레이션 중 예외 발생 상황 모킹 (save 메서드 타겟팅)
        doThrow(new DeployPersistenceException(new RuntimeException("DB Connection Lost")))
                .when(deployPersistencePort).save(any(DeploymentRequestedEvent.class));

        // When
        DeployResult result = deployService.deploy(command);

        // Then
        assertNotNull(result);
        assertEquals(DeployResultStatus.FAILED, result.deployStatus());

        // 에러 코드 메시지("배포 요청 데이터 저장에 실패했습니다.")가 제대로 출력되는지 확인
        assertTrue(result.message().contains("배포 요청 데이터 저장에 실패했습니다"));

        verify(deployPersistencePort, times(1)).save(any(DeploymentRequestedEvent.class));
    }

}
