package com.mcp.mcp_pilot.ops.port.in.dto;


import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record DiagnoseCommand(
        @JsonPropertyDescription("진단할 네임스페이스 (예: default, mcp-apps)")
        String namespace,

        @JsonPropertyDescription("진단할 파드 이름 (선택사항, 없을 경우 AI가 고장 파드 자동 추출)")
        String podName
) {}
