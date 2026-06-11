package com.mcp.mcp_pilot.knowledge.port.in;

import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;

public interface KnowledgeChatUseCase {
    ChatResponse chat(ChatRequest chatRequest);
}
