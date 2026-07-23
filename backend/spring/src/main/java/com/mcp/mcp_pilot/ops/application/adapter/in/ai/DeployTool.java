package com.mcp.mcp_pilot.ops.application.adapter.in.ai;

import com.mcp.mcp_pilot.ai.annotation.AiTool;
import com.mcp.mcp_pilot.common.enums.ToolType;
import com.mcp.mcp_pilot.ops.port.in.DeployUseCase;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployCommand;
import com.mcp.mcp_pilot.ops.port.in.dto.DeployResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Slf4j
@AiTool(ToolType.DEPLOY_APP)
@RequiredArgsConstructor
public class DeployTool {

    private final DeployUseCase deployUseCase;

    @Tool(description = "Kubernetes 클러스터에 애플리케이션(Deployment)을 신규 배포하거나 업데이트합니다.")
    public String deploy(DeployCommand command) {
        log.info("[SpringAiDeployTool] AI로부터 배포 요청 Command 수신. AppName: {}", command.appName());
        // UseCase 호출하여 배포 결과 DTO 수신
        DeployResult result = deployUseCase.deploy(command);

        return String.format(
                "배포 요청 처리 완료. [상태: %s], [추적 ID: %s], [상세 메시지: %s] " +
                        "이 결과를 바탕으로 사용자에게 접수 상태를 친절히 설명하세요.",
                result.deployStatus(),
                result.trackingId(),
                result.message()
        );
    }
}
