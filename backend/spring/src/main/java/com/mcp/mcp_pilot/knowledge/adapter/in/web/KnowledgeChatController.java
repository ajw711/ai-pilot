package com.mcp.mcp_pilot.knowledge.adapter.in.web;

import com.mcp.mcp_pilot.common.security.CustomUserPrincipal;
import com.mcp.mcp_pilot.knowledge.port.in.KnowledgeChatUseCase;
import com.mcp.mcp_pilot.ops.adapter.in.web.dto.ChatEvent;
import com.mcp.mcp_pilot.ops.adapter.in.web.dto.EventType;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Tag(name = "Knowledge Chat", description = "지식 기반 RAG 채팅")
@Slf4j
@RestController
@RequestMapping("/api/{version}/knowledge")
@RequiredArgsConstructor
public class KnowledgeChatController {

    private final KnowledgeChatUseCase knowledgeChatUseCase;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChatEvent> chat(@RequestParam String message, @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        log.info("[KnowledgeChatController] RAG 스트리밍 요청");
        Long userId = customUserPrincipal.getId();
        return knowledgeChatUseCase.stream(message, userId)
                .map(ChatEvent::token)
                .concatWith(Flux.just(
                        new ChatEvent(EventType.COMPLETE, "")
                ));
    }
}
