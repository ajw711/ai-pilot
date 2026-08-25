package com.mcp.mcp_pilot.knowledge.adapter.out.vector;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.repository.VectorSourceRepository;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeVectorPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeVectorAdapter implements KnowledgeVectorPort {

    private final VectorSourceRepository vectorSourceRepository;

    @Override
    public boolean isVectorStored(Long knowledgeId) {
        return vectorSourceRepository
                .findBySourceTypeAndSourceId(VectorTargetType.KNOWLEDGE ,knowledgeId)
                .isPresent();
    }
}
