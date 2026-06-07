package com.mcp.mcp_pilot.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record KnowledgeRequest(

        @NotBlank(message = "제목(title)은 필수입니다.")
        String title,

        @NotBlank(message = "원문 내용(rawContent)은 필수입니다.")
        String rawContent,

        @NotBlank(message = "요약 내용(summarizedContent)은 필수입니다.")
        String summarizedContent,

        @NotNull(message = "태그 리스트는 필수입니다. (빈 배열 가능)")
        List<String> tags,

        @NotNull(message = "출처 URL 리스트는 필수입니다. (빈 배열 가능)")
        List<String> sourceUrls
) {
}
