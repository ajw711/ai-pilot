package com.mcp.mcp_pilot.knowledge.port.in;

import reactor.core.publisher.Flux;

public interface KnowledgeChatUseCase {
    Flux<String> stream(String message, Long userId);
}
