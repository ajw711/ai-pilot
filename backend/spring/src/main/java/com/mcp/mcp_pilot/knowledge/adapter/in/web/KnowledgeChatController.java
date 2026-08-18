package com.mcp.mcp_pilot.knowledge.adapter.in.web;

import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;
import com.mcp.mcp_pilot.common.security.CustomUserPrincipal;
import com.mcp.mcp_pilot.knowledge.port.in.KnowledgeChatUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Tag(name = "Knowledge Chat", description = "지식 기반 RAG 채팅")
@Slf4j
@RestController
@RequestMapping("/api/{version}/knowledge")
@RequiredArgsConstructor
public class KnowledgeChatController {

    private final KnowledgeChatUseCase knowledgeChatUseCase;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestParam String message, @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        log.info("[KnowledgeChatController] RAG 스트리밍 요청");
        Long userId = customUserPrincipal.getId();
        return knowledgeChatUseCase.stream(message, userId)
                .map(token -> ServerSentEvent.<String>builder()
                        .event("TOKEN")
                        .data(token)
                        .build())
                .concatWith(Flux.just(
                        ServerSentEvent.<String>builder()
                                .event("COMPLETE")
                                .data("")
                                .build()
                ));
    }
}
