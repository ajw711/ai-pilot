package com.mcp.mcp_pilot.knowledge.service;

import com.mcp.mcp_pilot.common.ToolExecutor;
import com.mcp.mcp_pilot.common.dto.ToolResponse;
import com.mcp.mcp_pilot.knowledge.dto.KnowledgeRequest;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeLogRepository;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeSourceRepository;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KnowledgeToolService implements ToolExecutor<KnowledgeRequest, ToolResponse<Long>> {

    private final KnowledgeLogRepository logRepository;
    private final KnowledgeSourceRepository sourceRepository;
    private final KnowledgeTagRepository tagRepository;

    @Override
    public ToolResponse<Long> execute(KnowledgeRequest request) {

        Long knowledgeId = 1L;

        return ToolResponse.success(
                "지식 저장 완료",
                knowledgeId
        );
    }
}
