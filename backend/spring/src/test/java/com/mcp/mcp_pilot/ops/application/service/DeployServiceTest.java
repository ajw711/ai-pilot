package com.mcp.mcp_pilot.ops.application.service;

import com.mcp.mcp_pilot.ops.application.model.DeploySpec;
import com.mcp.mcp_pilot.ops.application.policy.DeploymentPolicy;
import com.mcp.mcp_pilot.ops.exception.DeployPublishException;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResult;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployStatus;
import com.mcp.mcp_pilot.ops.port.out.DeployPort;
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
    private DeployPort deployPort; // 외부 시스템(NATS) MOCK 사용

    private DeploymentPolicy deploymentPolicy;
    private DeployService deployService;

    @BeforeEach
    void setUp() {
        deploymentPolicy = new DeploymentPolicy("500m", "512Mi");
        deployService = new DeployService(deployPort, deploymentPolicy);
    }

    @Test
    @DisplayName("정상적인 배포 커맨드가 입력되면 배포 스펙이 생성되고 NATS 발행 성공 결과를 반환한다")
    void deploySuccess() {
        // Given
        DeployCommand command = new DeployCommand("my-app", "nginx", "1.21", 3, "production");
        ArgumentCaptor<DeploySpec> specCaptor = ArgumentCaptor.forClass(DeploySpec.class);

        // When
        DeployResult result = deployService.deploy(command);

        // Then
        assertNotNull(result);
        assertTrue(result.trackingId().startsWith("DEPLOY-"));
        assertEquals(DeployStatus.ACCEPTED, result.deployStatus());

        // Port가 1회 호출되었는지 검증
        verify(deployPort, times(1)).publishDeploy(specCaptor.capture());

        // DeploySpec 객체를 가져와 내부 필드들이 잘 맵핑되었는지 정밀 검증
        DeploySpec capturedSpec = specCaptor.getValue();
        assertEquals(result.trackingId(), capturedSpec.getTrackingId());
        assertEquals("my-app", capturedSpec.getAppName());
        assertEquals("nginx", capturedSpec.getImage());
        assertEquals("1.21", capturedSpec.getTag());
        assertEquals(3, capturedSpec.getReplicas());
        assertEquals("production", capturedSpec.getNamespace());

        //  실제 주입한 policy 인스턴스의 정책이 올바르게 맵핑되었는지 확인
        assertEquals("500m", capturedSpec.getCpuLimit());
        assertEquals("512Mi", capturedSpec.getMemoryLimit());
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
        assertEquals(DeployStatus.FAILED, result.deployStatus());
        assertTrue(result.message().contains("필수 입력값입니다"));
        verify(deployPort, never()).publishDeploy(any());
    }

    @Test
    @DisplayName("NATS 전송 아댑터에서 DeployPublishException이 발생하면 FAILED 결과를 반환하고 트래킹을 유지")
    void deployFailPublishError() {
        // 트래킹 유지를 하는 이유
        // 로그 추적성 (Traceability)을 위해
        // 사용자(고객) 지원을 위해
        // 추적 번호를 시스템 관리자에게 보여주면서 문의를 넣을 수 있고, 관리자는 즉시 해당 번호의 상세 장애 로그를 조회해 볼 수 있다

        // Given
        DeployCommand command = new DeployCommand("my-app", "nginx", "latest", 1, "default");

        // Out Port 호출 시 강제 예외 발생 시뮬레이션
        doThrow(new DeployPublishException(new RuntimeException("NATS Connection Lost")))
                .when(deployPort).publishDeploy(any(DeploySpec.class));

        // When
        DeployResult result = deployService.deploy(command);

        // Then
        assertNotNull(result);
        assertEquals(DeployStatus.FAILED, result.deployStatus());
        assertTrue(result.message().contains("배포 요청 전송에 실패했습니다"));

        verify(deployPort, times(1)).publishDeploy(any(DeploySpec.class));
    }

}
