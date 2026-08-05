package com.mcp.mcp_pilot.ops.adapter.in.ai;

import com.mcp.mcp_pilot.ai.annotation.AiTool;
import com.mcp.mcp_pilot.common.enums.ToolType;
import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.ops.port.in.K8sDiagnoseUseCase;
import com.mcp.mcp_pilot.ops.port.in.dto.DiagnoseCommand;
import com.mcp.mcp_pilot.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

@Slf4j
@AiTool(ToolType.K8S_DIAGNOSTIC)
@RequiredArgsConstructor
public class K8sDiagnosticTool {

    private final K8sDiagnoseUseCase k8sDiagnoseUseCase;

    @Tool(description = "Kubernetes 파드의 에러 로그와 Warning 이벤트를 수집하여 클러스터 상태 및 장애 원인을 진단합니다.")
    public String inspectCluster(DiagnoseCommand command, ToolContext toolContext) throws UserException {
        log.info("[K8sDiagnosticTool] AI 진단 툴 호출. Namespace: {}, PodName: {}", command.namespace(), command.podName());
        if (true) throw new RuntimeException("테스트용 강제 예외 발생!");
        Thread t = Thread.currentThread();
        log.info("[THREAD-CHECK] Tool: name={}, isVirtual={}", t.getName(), t.isVirtual());
        
        if (toolContext == null || toolContext.getContext().get("userId") == null) {
            log.warn("[K8sDiagnosticTool] 인증 컨텍스트(userId) 부재.");
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }

        Long userId = ((Number) toolContext.getContext().get("userId")).longValue();

        return k8sDiagnoseUseCase.diagnose(command, userId);
    }
}
