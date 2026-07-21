package com.mcp.mcp_pilot.knowledge.adapter.out.ai;

import com.google.genai.errors.ApiException;
import com.mcp.mcp_pilot.knowledge.domain.vo.VerificationReport;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeAiPort;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiAdapter implements KnowledgeAiPort {

    private final ChatClient chatClient;
    private final MeterRegistry meterRegistry;

    // AI API 동시 요청수 조절
    private final Semaphore apiThrottle = new Semaphore(2);
    private final Random random = new Random();

    private static final String VERIFY_PROMPT = """
            당신은 소프트웨어 엔지니어링 및 컴퓨터 과학 기술 문서를 검수하는 전문 에디터(Knowledge Guardian)입니다.
            사용자가 여러 자료를 공부하여 정리해 둔 지식 원문을 기술적으로 꼼꼼히 대조하고 검토해 주세요.
            
            발견된 이슈는 다음 세 가지 심각도(severity) 레벨로 분류하여 issues 배열 형식(JSON)으로 응답해 주세요.
            
            1. CRITICAL: 기술적 스펙, 공식 사양, 상속 및 포함 관계(예: 상위/하위 개념의 혼동), 핵심 개념 정의를 명백히 잘못 설명하여 나중에 면접이나 실무에서 사용하면 치명적인 결함이 되는 사실적 오류
            2. WARNING: 특정 옵션이나 환경에 따라 다름에도 무조건 단정적으로 작성하여 오해의 소지가 있거나, 예외 조건 누락, 문맥상 헷갈리기 쉬운 서술
            3. SUGGESTION: 본문의 문장은 문제없으나, 관련해서 추가적으로 공부하면 좋은 연관 핵심 키워드, 디자인 패턴, 혹은 심화 설명 제안 (이 항목은 감점이나 오류 대상이 아닌 정보 보완성 추천입니다)
            
            중요 지침:
            - 사내 기술 블로그나 엔지니어링 위키 성격의 문서이므로, "가장 널리 쓰인다", "거의 항상 사용한다" 등의 실무적 대중성 표현은 트집 잡지(지적하지) 마십시오.
            - 외부 지식을 임의로 본문에 길게 추가하거나 자의적으로 해석하여 수정본을 창작하지 말고, 오직 원문에 존재하는 팩트의 오류 검수와 제안에만 집중하십시오.
            
            반드시 제시된 형식(JSON)에 맞춰 응답해 주세요. 외부 지식을 억지로 추가하여 문장을 새로 작성하지 마십시오.
            """;

    private static final String FORMAT_PROMPT = """
            당신은 개발자 기술 문서 전문 포맷터입니다.
            제공된 원문의 텍스트 내용(설명글, 예제 코드 내의 주석 및 주관적 생각 등)을 단 한 문장이나 단어조차도 임의로 생략, 요약, 수정 또는 삭제하지 말고 100% 그대로 보존하십시오.
            이 작업은 요약 작업이 아닙니다. 원문 글을 글자 그대로 보존하면서, 오직 가독성을 위해 마크다운 포맷(##, ###, ```java, * 불릿 등)과 적절한 줄바꿈만 추가하는 작업입니다.
            
            가이드라인:
            - 제목과 소제목(##, ###)을 추가하여 문단을 가독성 있게 구획하십시오.
              * 중요: 모든 제목(예: ##, ###)은 반드시 독립된 행(Line)에 작성되어야 하며, 제목 뒤에는 반드시 줄바꿈 문자(\\n)를 넣어 본문 내용이 제목과 같은 줄에 연속되어 위치하지 않도록 완전히 격리하십시오.
            - 예제 코드는 주석을 포함하여 원문 그대로 완전히 보존하고, 가급적 포맷을 정돈하여 표시하십시오.
            - 원문의 모든 설명글과 문단은 절대 생략하거나 생축하지 말고 원본 문장을 100% 다 기입해 주십시오.
            - 문서 마지막에 원문의 키워드를 나타내는 태그를 3개 이상 '#' 기호와 함께 추가하십시오.
            """;

    @PostConstruct
    public void registerMetrics() {
        meterRegistry.gauge("ai_throttle_available_permits", apiThrottle, Semaphore::availablePermits);
        meterRegistry.gauge("ai_throttle_queue_length", apiThrottle, Semaphore::getQueueLength);
        log.info("[SpringAiAdapter] AI 가용성 제어 메트릭 수집 바인딩 완료");
    }


    @Override
    public VerificationReport verify(String rawContent) {
        return executeWithThrottle(() -> {
            log.info("[SpringAiAdapter] AI 검수 요청");
            String userPrompt = "원문:\n" + rawContent;
            return chatClient.prompt()
                    .system(VERIFY_PROMPT)
                    .user(userPrompt)
                    .call()
                    .entity(VerificationReport.class);
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
        int maxAttempts = 3;
        long baseDelayMs = 2000; // 2초

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            // 개별 호출 시점마다 락 획득
            try {
                apiThrottle.acquire();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("AI API 호출 대기 중 인터럽트 발생", e);
            }

            try {
                return task.call();
            } catch (Exception e) {
                // 429 Too Many Requests 감지 시 즉시 실패 (할당량 이슈)
                if (isRateLimit(e)) {
                    log.error("[SpringAiAdapter] Rate Limit (429) 감지. 리트라이 없이 즉시 실패 처리합니다.");
                    throw new RuntimeException("AI Rate Limit (429) 발생", e);
                }
                // 503, Timeout, I/O 등 허용된 복구 가능 예외가 아닌 경우 즉시 실패
                if (!isRetryable(e)) {
                    log.error("[SpringAiAdapter] 리트라이 대상이 아닌 일반 예외 발생. 즉시 실패 처리합니다. 에러: {}", e.getMessage());
                    throw new RuntimeException("AI API 호출 즉시 실패 예외", e);
                }

                log.warn("[SpringAiAdapter] AI API 호출 실패 (시도 횟수: {}/{}), 에러: {}",
                        attempt, maxAttempts, e.getMessage());

                if (attempt == maxAttempts) {
                    throw new RuntimeException("AI API 호출 최대 시도 횟수 초과", e);
                }
            } finally {
                // 호출 종료(성공 또는 에러) 후 즉시 락 반환 ➔ Sleep 시간 동안 다른 요청 처리 가능
                apiThrottle.release();
            }

            // 락을 해제한 상태에서 Exponential Backoff + Random Jitter 적용 대기 (2s -> 4s -> 8s)
            long backoff = (long) (baseDelayMs * Math.pow(2, attempt-1));
            long jitter = random.nextInt(500);
            long sleepTime = backoff + jitter;

            log.info("[SpringAiAdapter] {}ms 후 재시도를 실행합니다. (락 반환 완료)", sleepTime);

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt(); // 인터럽트 상태 복구
                throw new RuntimeException("재시도 대기 중 인터럽트 발생으로 리트라이 중단", ie);
            }
        }
        throw new RuntimeException("AI API 호출 실패");
    }


    /**
     * 재귀적으로 원인(Cause) 예외를 추적하여 리트라이 대상인지 판별합니다.
     */
    private boolean isRetryable(Throwable t) {
        if (t == null) {
            return false;
        }

        if (t instanceof ApiException apiException) {
            int statusCode = apiException.code();
            // 5xx 계열 (500, 502, 503, 504 등) 서버 과부화 상황에서만 retry 작동
            return statusCode >= 500 && statusCode < 600;
        }

        // 네트워크 및 I/O 타임아웃, 커넥션 유실 판별
        if (t instanceof SocketTimeoutException ||
                t instanceof ConnectException ||
                t instanceof SocketException ||
                t instanceof IOException
        ) {
            return true;
        }

        // 재귀적으로 원인 추적
        return isRetryable(t.getCause());
    }

    /**
     * 429 Too Many Requests 인지 재귀적으로 검증.
     */
    private boolean isRateLimit(Throwable t) {
        if (t == null) {
            return false;
        }
        if (t instanceof ApiException apiException) {
            return apiException.code() == 429;
        }
        return isRateLimit(t.getCause());
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
