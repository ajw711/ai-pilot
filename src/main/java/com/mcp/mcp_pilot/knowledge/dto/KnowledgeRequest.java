package com.mcp.mcp_pilot.knowledge.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record KnowledgeRequest(
        @NotBlank(message = "원문 내용(rawContent)은 필수입니다.")
        String rawContent,
        @NotBlank(message = "요약 내용(summarizedContent)은 필수입니다.")
        String summarizedContent,
        List<String> tags,
        List<String> sourceUrls
) {
}
