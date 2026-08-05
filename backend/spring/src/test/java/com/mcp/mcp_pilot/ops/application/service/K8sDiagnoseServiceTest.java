package com.mcp.mcp_pilot.ops.application.service;

import com.mcp.mcp_pilot.ops.adapter.out.nats.dto.DiagnoseRequest;
import com.mcp.mcp_pilot.ops.port.in.dto.DiagnoseCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DiagnoseResult;
import com.mcp.mcp_pilot.ops.port.out.DiagnosePort;
import com.mcp.mcp_pilot.ops.port.out.UserAuthorizationPort;
import com.mcp.mcp_pilot.user.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class K8sDiagnoseServiceTest {

    @Mock
    private DiagnosePort diagnosePort;

    @Mock
    private UserAuthorizationPort userAuthorizationPort;

    @InjectMocks
    private K8sDiagnoseService k8sDiagnoseService;

    private Long validUserId;
    private Long invalidUserId;

    @BeforeEach
    void setUp() {
        validUserId = 1L;
        invalidUserId = 99L;
    }

    @Test
    @DisplayName("진단 권한이 있는 사용자가 진단 요청 시 K8s 텔레메트리 텍스트 프롬프트가 정상 생성된다.")
    void diagnose_Success() {
        // given
        DiagnoseCommand command = new DiagnoseCommand("default", "my-pod");
        given(userAuthorizationPort.canDeploy(validUserId)).willReturn(true);

        DiagnoseResult mockResult = new DiagnoseResult(
                "DIAGNOSE-12345",
                "default",
                List.of("my-pod"),
                List.of("[2026-08-05] Reason: OOMKilled | Message: Heap space memory limit exceeded"),
                "java.lang.OutOfMemoryError: Java heap space",
                "SUCCESS",
                null
        );
        given(diagnosePort.requestDiagnose(any(DiagnoseRequest.class))).willReturn(mockResult);

        // when
        String resultPrompt = k8sDiagnoseService.diagnose(command, validUserId);

        // then
        assertThat(resultPrompt).contains("DIAGNOSE-12345");
        assertThat(resultPrompt).contains("OOMKilled");
        assertThat(resultPrompt).contains("java.lang.OutOfMemoryError");
        assertThat(resultPrompt).contains("확인된 사실");
    }

    @Test
    @DisplayName("진단 권한이 없는 사용자가 요청하면 UserException 예외가 발생한다.")
    void diagnose_Unauthorized_ThrowsException() {
        // given
        DiagnoseCommand command = new DiagnoseCommand("default", "");
        given(userAuthorizationPort.canDeploy(invalidUserId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> k8sDiagnoseService.diagnose(command, invalidUserId))
                .isInstanceOf(UserException.class);
    }
}
