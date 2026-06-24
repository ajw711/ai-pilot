package com.mcp.mcp_pilot.knowledge.adapter.in.web.mapper;

import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.ApproveRequest;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.KnowledgeRequest;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.ListKnowledgeResponse;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.SaveKnowledgeResponse;
import com.mcp.mcp_pilot.knowledge.port.in.dto.ApproveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.in.dto.KnowledgeSummary;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeResult;

import java.util.List;

/**
 * Web Mapper: Request DTO -> Port Command
 */
public class KnowledgeWebMapper {

    public static ListKnowledgeResponse toResponse(List<KnowledgeSummary> summaryList) {
        return new ListKnowledgeResponse(summaryList);
    }

    public static SaveKnowledgeCommand toCommand(KnowledgeRequest request) {
        return new SaveKnowledgeCommand(
                request.title(),
                request.rawContent(),
                request.formattedContent(),
                request.tags(),
                request.sourceUrls()
        );
    }

    public static ApproveKnowledgeCommand toCommand(ApproveRequest request) {
        return new ApproveKnowledgeCommand(
                request.knowledgeId(),
                request.finalFormattedContent()
        );
    }
}
