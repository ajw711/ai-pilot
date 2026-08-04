package com.mcp.mcp_pilot.ops.port.in.dto;

import org.springframework.security.core.parameters.P;

public record DiagnoseCommand(
        @P(description = "진단할 네임스페이스 (예: default, mcp-apps)")
        String namespace,

        @P(description = "진단할 파드 이름 (선택사항, 없을 경우 AI가 고장 파드 자동 추출)")
        String podName
) {}
