package com.mcp.mcp_pilot.service;

import com.mcp.mcp_pilot.common.ToolExecutor;
import com.mcp.mcp_pilot.common.dto.ToolResponse;
import com.mcp.mcp_pilot.dto.Knowledge.KnowledgeRequest;
import com.mcp.mcp_pilot.repository.Knowledge.KnowledgeLogRepository;
import com.mcp.mcp_pilot.repository.Knowledge.KnowledgeSourceRepository;
import com.mcp.mcp_pilot.repository.Knowledge.KnowledgeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgeToolService implements ToolExecutor<KnowledgeRequest, ToolResponse> {

    private final KnowledgeLogRepository logRepository;
    private final KnowledgeSourceRepository sourceRepository;
    private final KnowledgeTagRepository tagRepository;

    @Override
    public ToolResponse execute(KnowledgeRequest request) {
        return null;
    }
}
