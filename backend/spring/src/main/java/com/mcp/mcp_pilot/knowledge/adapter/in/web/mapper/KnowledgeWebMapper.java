package com.mcp.mcp_pilot.knowledge.adapter.in.web.mapper;

import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.ApproveRequest;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.KnowledgeRequest;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.SaveKnowledgeResponse;
import com.mcp.mcp_pilot.knowledge.port.in.dto.ApproveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;

/**
 * Web Mapper: Request DTO -> Port Command
 */
public class KnowledgeWebMapper {

    public static SaveKnowledgeCommand toCommand(KnowledgeRequest request) {
        return new SaveKnowledgeCommand(
                request.title(),
                request.rawContent(),
                request.formattedContent(),
                request.tags(),
                request.sourceUrls()
        );
    }

    public static SaveKnowledgeResponse toResponse(Long knowledgeId) {
        return new SaveKnowledgeResponse(knowledgeId);
    }

    public static ApproveKnowledgeCommand toCommand(ApproveRequest request) {
        return new ApproveKnowledgeCommand(
                request.knowledgeId(),
                request.finalFormattedContent()
        );
    }
}
