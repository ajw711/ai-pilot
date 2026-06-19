package com.mcp.mcp_pilot.knowledge.port.out;

import com.mcp.mcp_pilot.knowledge.domain.vo.VerificationResponse;

public interface KnowledgeAiPort {
    VerificationResponse verify(String rawContent);
    String format(String rawContent);
}
