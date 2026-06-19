package com.mcp.mcp_pilot.knowledge.adapter.out.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mcp.mcp_pilot.knowledge.domain.vo.VerificationResponse;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeAiPort;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiAdapter implements KnowledgeAiPort {

    private final ChatClient chatClient;
    private final MeterRegistry meterRegistry;
    private final JsonMapper jsonMapper;

    // AI API 동시 요청수 조절
    private final Semaphore apiThrottle = new Semaphore(2);

    private static final String VERIFY_PROMPT = """
            당신은 소프트웨어 엔지니어링 지식을 검수하는 전문 편집자입니다.
            제공된 개발자 원문을 분석하여 다음 3가지 항목을 작성해 주세요.
            
            1. 사실 관계 오류 (factIssues): 원문의 내용 중 기술적으로 명백히 잘못된 사실이 있다면 원문 내용(originalText), 이유(reason), 심각도(severity: HIGH/MEDIUM/LOW)를 명시해 주세요.
            2. 애매한 표현 (ambiguities): 설명이 불충분하거나 모호하여 오해를 일으킬 수 있는 문장(text)과 개선 제안(suggestion)을 적어주세요.
            3. 근거 부족 (unsupportedClaims): 명확한 기술적 근거 없이 단정적으로 선언된 문장(text)과 그 이유(reason)를 적어주세요.
            
            반드시 제시된 형식(JSON)에 맞춰 응답해 주세요. 외부 지식을 억지로 추가하여 문장을 새로 작성하지 마십시오.
            """;

    private static final String FORMAT_PROMPT = """
            당신은 개발자 기술 문서 전문 포맷터입니다.
            제공된 개발자 원문의 핵심 뜻과 주관적 뉘앙스(예: '~라고 이해했다', '~인 것 같다' 등 저자의 생각)를 '절대' 수정하거나 삭제하지 말고 그대로 보존하십시오.
            문장을 교과서적으로 다시 쓰거나 재작성하지 마십시오.
            오직 다음 가이드라인에 따라 마크다운 가독성 포맷팅만 수행하십시오.
            
            가이드라인:
            - 제목과 소제목(##, ###)을 사용하여 문서를 읽기 쉽게 문단으로 구획하십시오.
            - 코드 예제는 원문 그대로 완벽히 보존하고, 가급적 포맷을 정돈하여 표시하십시오.
            - 불필요한 반복이 있는 단락은 제거하되, 문장의 원래 의도는 해치지 마십시오.
            - 문서 마지막에 원문의 키워드를 나타내는 태그를 3개 이상 '#' 기호와 함께 추가하십시오.
            """;

    @PostConstruct
    public void registerMetrics() {
        meterRegistry.gauge("ai_throttle_available_permits", apiThrottle, Semaphore::availablePermits);
        meterRegistry.gauge("ai_throttle_queue_length", apiThrottle, Semaphore::getQueueLength);
        log.info("[SpringAiAdapter] AI 가용성 제어 메트릭 수집 바인딩 완료");
    }


    @Override
    public VerificationResponse verify(String rawContent) {
        return executeWithThrottle(() -> {
            log.info("[SpringAiAdapter] AI 검수 요청");
            String userPrompt = "원문:\n" + rawContent;
            return chatClient.prompt()
                    .system(FORMAT_PROMPT)
                    .user(userPrompt)
                    .call()
                    .entity(VerificationResponse.class);
        });
    }

    @Override
    public String format(String rawContent) {
        return executeWithThrottle(() -> {
            log.info("[SpringAiAdapter] AI 포맷팅 요청");
            String indexedContent = addLineNumbers(rawContent);
            String userPrompt = "원문:\n" + indexedContent;
            return chatClient.prompt()
                    .system(FORMAT_PROMPT)
                    .user(userPrompt)
                    .call()
                    .content();
        });
    }

    private <T> T executeWithThrottle(Callable<T> task) {
        try {
            apiThrottle.acquire();
            return task.call();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("AI API 호출 대기 중 인터럽트 발생", e);
        } catch (Exception e) {
            throw new RuntimeException("AI API 호출 중 예외 발생", e);
        } finally {
            apiThrottle.release();
        }
    }

    private String addLineNumbers(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return "";
        }

        String[] lines = rawContent.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        int lineNumber = 1;

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                sb.append(String.format("[L%d] %s\n", lineNumber++, line));
            } else {
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
