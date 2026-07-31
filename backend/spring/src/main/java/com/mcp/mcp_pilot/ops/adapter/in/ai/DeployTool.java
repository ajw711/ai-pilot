package com.mcp.mcp_pilot.ops.adapter.in.ai;

import com.mcp.mcp_pilot.ai.annotation.AiTool;
import com.mcp.mcp_pilot.common.enums.ToolType;
import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.ops.port.in.DeployUseCase;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResponse;
import com.mcp.mcp_pilot.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;

@Slf4j
@AiTool(ToolType.DEPLOY_APP)
@RequiredArgsConstructor
public class DeployTool {

    private final DeployUseCase deployUseCase;

    @Tool(description = "Kubernetes 클러스터에 애플리케이션(Deployment)을 신규 배포하거나 업데이트합니다.")
    public String deploy(DeployCommand command, ToolContext toolContext) throws UserException {
        log.info("[SpringAiDeployTool] AI로부터 배포 요청 Command 수신. AppName: {}", command.appName());

        if (toolContext == null) {
            log.warn("[SpringAiDeployTool] 전달된 보안 툴 컨텍스트가 존재하지 않습니다.");
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }
        Object rawUserId = toolContext.getContext().get("userId");
        if (rawUserId == null) {
            log.warn("[SpringAiDeployTool] 툴 컨텍스트 내 사용자 식별자(userId)가 부재합니다.");
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }
        Long userId = ((Number) rawUserId).longValue();

        // 인가(authorization)는 DeployService.deploy() 내부 userAuthorizationPort.canDeploy()에서 재검증됨
        DeployResponse result = deployUseCase.deploy(command, userId);

        return String.format(
                "배포 요청 처리 완료. [상태: %s], [추적 ID: %s], [상세 메시지: %s] " +
                        "이 결과를 바탕으로 사용자에게 접수 상태를 친절히 설명하세요.",
                result.deployStatus(),
                result.trackingId(),
                result.message()
        );
    }
}
