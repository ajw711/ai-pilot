package com.mcp.mcp_pilot.ops.adapter.in.web;

import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;
import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.common.security.CustomUserPrincipal;
import com.mcp.mcp_pilot.ops.adapter.in.web.dto.ChatEvent;
import com.mcp.mcp_pilot.ops.port.in.PilotChatUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.concurrent.Executors;

@Slf4j
@RestController
@RequestMapping("/api/{version}/pilot")
@RequiredArgsConstructor
public class PilotChatController {

    private final PilotChatUseCase pilotChatUseCase;

    /**
     * produces = MediaType.TEXT_EVENT_STREAM_VALUE 설정을 통해
     * 브라우저에 Server-Sent Events(SSE) 스트리밍 형식으로 전송할 것임을 명시
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<ChatEvent> streamChar(@RequestBody ChatRequest chatRequest, @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        log.info("[PilotChatController] 실시간 스트리밍 요청 수신");
        Thread t = Thread.currentThread();
        log.info("[THREAD-CHECK] Controller: name={}, isVirtual={}", t.getName(), t.isVirtual());

        Long userId = customUserPrincipal.getId();
        return pilotChatUseCase.streamChat(chatRequest, userId)
                .doOnSubscribe(s ->log.info("stream start"))
                .doOnComplete(() -> log.info("stream complete"))
                .doOnError(e -> log.error("stream error", e));
    }

    @PostMapping("/chat")
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest chatRequest,
                                          @AuthenticationPrincipal CustomUserPrincipal customUserPrincipal) {
        log.info("[PilotChatController] 운영 비서 챗 요청 수신");
        Long userId = customUserPrincipal.getId();
        return ApiResponse.success(pilotChatUseCase.chat(chatRequest, userId));
    }

    @GetMapping(value = "/test/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter();

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                emitter.send("1");
                Thread.sleep(1000);

                emitter.send("2");
                Thread.sleep(1000);

                emitter.send("3");

                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
