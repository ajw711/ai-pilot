package com.mcp.mcp_pilot.knowledge.adapter.in.web.mapper;

import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.ApproveRequest;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.KnowledgeRequest;
import com.mcp.mcp_pilot.knowledge.port.in.dto.ApproveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;

import java.util.List;

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

    public static ApproveKnowledgeCommand toCommand(ApproveRequest request) {
        return new ApproveKnowledgeCommand(
                request.knowledgeId(),
                request.finalFormattedContent()
        );
    }
}
