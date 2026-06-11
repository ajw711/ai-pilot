package com.mcp.mcp_pilot.knowledge.adapter.in.mcp;

import com.mcp.mcp_pilot.common.ToolExecutor;
import com.mcp.mcp_pilot.common.dto.ToolResponse;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.mapper.KnowledgeWebMapper;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.KnowledgeRequest;
import com.mcp.mcp_pilot.knowledge.port.in.SaveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeMcpAdapter implements ToolExecutor<KnowledgeRequest, ToolResponse<Long>> {

    private final SaveKnowledgeUseCase saveKnowledgeUseCase;

    @Override
    public ToolResponse<Long> execute(KnowledgeRequest request) {
        log.info("지식 저장 MCP 어댑터 실행: {}", request.title());
        
        // 1. DTO -> Command 변환
        SaveKnowledgeCommand command = KnowledgeWebMapper.toCommand(request);
        
        // 2. UseCase 호출
        KnowledgeLog savedLog = saveKnowledgeUseCase.saveKnowledge(command);
        
        return ToolResponse.success(
                "지식 저장 완료",
                savedLog.getId()
        );
    }
}
