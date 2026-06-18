package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.dto.AiRequest;
import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeSource;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeTag;
import com.mcp.mcp_pilot.knowledge.port.in.SaveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import io.micrometer.core.instrument.MeterRegistry;
import io.prometheus.metrics.core.metrics.Gauge;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSaveService implements SaveKnowledgeUseCase {

    private final KnowledgePersistencePort persistencePort;
    private final AIClientFactory aiClientFactory;
    private final ApplicationEventPublisher eventPublisher;
    private final ExecutorService wikiExecutor;
    private final TransactionTemplate transactionTemplate;
    private final Semaphore apiThrottle = new Semaphore(2);
    private final MeterRegistry meterRegistry;
    private static final String WIKI_PROMPT = """
            다음 내용을 개발자 위키 문서 형식으로 정리해줘.

            규칙:
            - 핵심 개념 요약
            - 주요 내용 정리
            - 코드 예제 유지
            - 불필요한 중복 제거
            - Markdown 형식 사용
            - 모든 요약 문장의 끝에는 반드시 해당 정보의 근거가 되는 원문의 행 번호를 표기할 것 (예: [L15], [L20-L22])
            - 문서 마지막에는 대화의 핵심 키워드를 나타내는 태그를 3개 이상 추가할 것 (예: #Java #VirtualThreads)

            원문:
            """;

    @PostConstruct
    public void registerMetrics(){
        // AI 호출 가용 허가증 수
        meterRegistry.gauge("ai_throttle_available_permits", apiThrottle, Semaphore::availablePermits);
        // AI 호출 대기열 길이
        meterRegistry.gauge("ai_throttle_queue_length", apiThrottle, Semaphore::getQueueLength);
        log.info("[SaveService] AI Throttle 메트릭 등록 완료");
    }

    @Override
    @Transactional
    public KnowledgeLog saveKnowledge(SaveKnowledgeCommand command) {
        // 파이프라인 시작 카운트
        meterRegistry.counter("knowledge_save_total").increment();
        log.info("지식 저장 프로세스 시작 (Application Service): {}", command.title());
        KnowledgeLog savedLog = persistencePort.save(
                KnowledgeLog.create(
                        command.title(),
                        command.rawContent(),
                        command.summarizedContent()
                )
        );
        Long knowledgeId = savedLog.getId();

        if (command.sourceUrls() != null && !command.sourceUrls().isEmpty()) {
            List<KnowledgeSource> sources = command.sourceUrls().stream()
                    .map( url -> KnowledgeSource.create(knowledgeId, url))
                    .toList();
            persistencePort.saveSources(sources);
        }

        // 요약본이 없다면 비동기 가공 프로세스 가동
        if (command.summarizedContent() == null || command.summarizedContent().isBlank()) {
            wikiExecutor.submit(() -> processWikiAsync(knowledgeId, command.rawContent()));
        }
        log.info("[SaveService] 지식 1차 저장 완료 ID: {}", knowledgeId);
        return savedLog;
    }

    private void processWikiAsync(long knowledgeId, String rawContent) {
        boolean acquired = false;
        try {
            // Throttling 남은 허가증이 없으면 여기서 대기 (다른 스레드에게 양보)
            // Semaphore의 permit(허가증)을 하나 획득
            apiThrottle.acquire();
            acquired = true;
            log.info("[SaveService] Wiki 백그라운드 가공 시작 (ID: {})", knowledgeId);
            // 1. 라인 인덱싱 적용
            String indexedContent = addLineNumbers(rawContent);
            log.debug("Wiki 인덱싱된 원문 미리보기:\n{}", indexedContent.substring(0,
                    Math.min(indexedContent.length(), 200)) + "...");

            // 2. 프롬프트 결합 및 AI 호출
            String prompt = WIKI_PROMPT + "\n\n" + indexedContent;
            AiRequest aiRequest = AiRequest.of(prompt, AIModel.GEMINI, List.of());
            AiClientStrategy strategy = aiClientFactory.get(aiRequest.model());
            String summarized = strategy.call(aiRequest);

            // 3. 가공된 결과 업데이트
            updateAndPublishTransactional(knowledgeId, summarized);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 인터럽트 상태 복구
            log.warn(
                    "Wiki 가공 작업 중단 (ID: {})",
                    knowledgeId
            );
        } catch (Exception e) {
            log.error(
                    "Wiki 가공 실패 (ID: {})",
                    knowledgeId,
                    e
            );
        } finally {
            // [Throttling] 작업이 끝나면 반드시 허가증을 반납해야 다른 스레드가 진입 가
            if(acquired) {
                apiThrottle.release();
            }
        }
    }

    // DB 업데이트와 이벤트를 묶는 트랜잭션 메서드
    protected void updateAndPublishTransactional(Long knowledgeId, String summarized) {

        transactionTemplate.executeWithoutResult(status -> {
            // 1. 요약본 업데이트
            persistencePort.updateSummary(knowledgeId, summarized);

            // 2. 태그 추출 및 저장 (향후 TagListener로 분리 가능)
            List<String> extractedTags = extractTags(summarized);
            if (!extractedTags.isEmpty()) {
                List<KnowledgeTag> tagList = extractedTags.stream()
                        .map(tagName -> KnowledgeTag.create(knowledgeId, tagName))
                        .toList();
                persistencePort.saveTags(tagList);
                log.info("[SaveService] Wiki 태그 자동 추출 및 저장 완료 ({}개) - ID: {}",
                        extractedTags.size(), knowledgeId);
            }


            log.info("[SaveService] Wiki 요약본 업데이트 완료 (ID: {})", knowledgeId);

            // 이벤트 발행
            eventPublisher.publishEvent(KnowledgeProcessedEvent.of(knowledgeId));
            log.info("[SaveService] 가공 완료 이벤트 발행됨 (ID: {})", knowledgeId);
        });
    }

    private String addLineNumbers(String rawContent) {
        if (rawContent == null || rawContent.isBlank()) {
            return "";
        }

        String[] lines = rawContent.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        int lineNumber = 1;

        for (String line : lines) {
            // 빈 줄이라도 원본 형태를 유지 다만 번호는 공백이 아닌 실제 내용이 있는 줄 기준으로 카운트
            if (!line.trim().isEmpty()) {
                sb.append(String.format("[L%d] %s\n", lineNumber++, line));
            } else {
                sb.append("\n"); // 원본의 문단 구분(빈 줄) 유지
            }
        }
        return sb.toString();
    }

    private List<String> extractTags(String aiResponse) {
        List<String> tags = new ArrayList<>();
        // #으로 시작하고 공백이나 줄바꿈으로 끝나는 단어 추출 정규식
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(aiResponse);

        while (matcher.find()) {
            tags.add(matcher.group(1)); // #을 제외한 태그명만 추출
        }
        return tags;
    }
}
