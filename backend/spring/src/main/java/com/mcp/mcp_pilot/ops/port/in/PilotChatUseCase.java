package com.mcp.mcp_pilot.ops.port.in;

import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;

public interface PilotChatUseCase {
    ChatResponse chat(ChatRequest chatRequest);
}
