package com.mcp.mcp_pilot.ops.port.in;

import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;
import com.mcp.mcp_pilot.ops.adapter.in.web.dto.ChatEvent;
import reactor.core.publisher.Flux;

public interface PilotChatUseCase {
    ChatResponse chat(ChatRequest chatRequest);
    Flux<ChatEvent> streamChat(ChatRequest chatRequest);
}
