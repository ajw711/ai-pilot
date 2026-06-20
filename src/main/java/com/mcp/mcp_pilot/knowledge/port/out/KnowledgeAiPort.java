package com.mcp.mcp_pilot.knowledge.port.out;

import com.mcp.mcp_pilot.knowledge.domain.vo.VerificationReport;

public interface KnowledgeAiPort {
    VerificationReport verify(String rawContent);
    String format(String rawContent);
}
