package com.mcp.mcp_pilot.knowledge.adapter.in.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ApproveRequest(
        @NotNull
        Long knowledgeId,
        @NotEmpty
        String finalFormattedContent
) {
}
