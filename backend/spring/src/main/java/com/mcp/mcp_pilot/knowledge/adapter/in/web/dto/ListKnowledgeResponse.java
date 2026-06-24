package com.mcp.mcp_pilot.knowledge.adapter.in.web.dto;

import com.mcp.mcp_pilot.knowledge.port.in.dto.KnowledgeSummary;

import java.util.List;

public record ListKnowledgeResponse(
        List<KnowledgeSummary> summaryList
) {
}
