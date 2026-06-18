package com.mcp.mcp_pilot.knowledge.port.in.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 지식 저장 명령 (Command DTO)
 * Port 계층에서 사용하며 입력 유효성 검증을 포함함.
 */
public record SaveKnowledgeCommand(
        @NotBlank String title,
        @NotBlank String rawContent,
        String summarizedContent,
        List<String> tags,
        List<String> sourceUrls
) {
}
