package com.mcp.mcp_pilot.knowledge.adapter.in.mcp;

import com.mcp.mcp_pilot.common.ToolExecutor;
import com.mcp.mcp_pilot.common.dto.ToolResponse;
import com.mcp.mcp_pilot.knowledge.adapter.in.web.dto.KnowledgeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeMcpAdapter implements ToolExecutor<KnowledgeRequest, ToolResponse<Long>> {

    @Override
    public ToolResponse<Long> execute(KnowledgeRequest request) {
        log.info("지식 저장 MCP 어댑터 (현재 비활성화)");
        return ToolResponse.success("비활성 상태", 0L);
    }
}
