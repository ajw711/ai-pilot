package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.dto.AiRequest;
import com.mcp.mcp_pilot.ai.dto.ChatRequest;
import com.mcp.mcp_pilot.ai.dto.ChatResponse;
import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import com.mcp.mcp_pilot.ai.vector.constant.SimilarityMetric;
import com.mcp.mcp_pilot.ai.vector.port.VectorSearchPort;
import com.mcp.mcp_pilot.common.enums.ToolType;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeTag;
import com.mcp.mcp_pilot.knowledge.port.in.KnowledgeChatUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.SaveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.SearchKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeSearchPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService implements SaveKnowledgeUseCase, SearchKnowledgeUseCase, KnowledgeChatUseCase {

    private final KnowledgePersistencePort persistencePort;
    private final KnowledgeSearchPort searchPort;
    private final VectorSearchPort vectorSearchPort;
    private final AIClientFactory aiClientFactory;
    private final ExecutorService wikiExecutor;

    // 무료 API 보호: 동시에 최대 2개의 스레드만 AI API 호출 허용
    private final Semaphore apiThrottle = new Semaphore(2);
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

    @Override
    @Transactional
    public KnowledgeLog saveKnowledge(SaveKnowledgeCommand command) {
        log.info("지식 저장 프로세스 시작 (Application Service): {}", command.title());

        // 요약본이 없더라도 일단 원문을 즉시 DB에 저장 (데이터 유실 방지)
        KnowledgeLog knowledgeLog = KnowledgeLog.create(
                command.title(),
                command.rawContent(),
                null,
                null
        );
        KnowledgeLog savedLog = persistencePort.save(knowledgeLog);
        Long knowledgeId = savedLog.getId();

        // 요약본이 없다면 비동기 가공 프로세스
        if (command.summarizedContent() == null || command.summarizedContent().isBlank()) {
            wikiExecutor.submit( () -> processWiki(knowledgeId, command.rawContent()));
        }
        log.info("지식 1차 저장 완료 ID: {}", knowledgeId);
        return savedLog;
    }


    @Override
    public String searchWiki(String query) {
        log.info("[Wiki-Search] 검색 요청 (Application Service): {}", query);

        // 1. 키워드 기반 검색 (Port 호출)
        List<KnowledgeLog> keywordResults = searchPort.search(query);
        if (!keywordResults.isEmpty()) {
            return formatListResponse("키워드 검색 결과", keywordResults);
        }

        // 2. 키워드 결과 없을 시 유사도 검색 호출
        List<Long> similarIds = vectorSearchPort.search(
                VectorTargetType.KNOWLEDGE,
                query,
                3,
                SimilarityMetric.COSINE
        );

        if (!similarIds.isEmpty()) {
            List<KnowledgeLog> vectorResults = similarIds.stream()
                    .map(persistencePort::findById)
                    .flatMap(Optional::stream)
                    .collect(Collectors.toList());
            return formatListResponse("유사 지식 검색 결과", vectorResults);
        }

        return "관련된 위키 내용을 찾을 수 없습니다.";
    }

    @Override
    public ChatResponse chat(ChatRequest chatRequest) {
        log.info("Knowledge chat request (Application Service)");

        AiRequest aiRequest = AiRequest.of(
                chatRequest.message(),
                AIModel.GEMINI,
                List.of(
                        ToolType.STORE_KNOWLEDGE_DATA,
                        ToolType.SEARCH_KNOWLEDGE
                )
        );
        AiClientStrategy strategy = aiClientFactory.get(aiRequest.model());

        String answer = strategy.call(aiRequest);
        return ChatResponse.of(answer);
    }

    private String formatListResponse(String type, List<KnowledgeLog> list) {
        String titles = list.stream()
                .map(k -> "- " + k.getTitle())
                .collect(Collectors.joining("\n"));
        return String.format("[%s]\n%s\n\n상세 내용이 궁금하시면 제목을 정확히 말씀해주세요.", type, titles);
    }

    // 비동기 지식 가공 메서드
    private void processWiki(Long knowledgeId, String rawContent) {

        boolean acquired = false;

        try {
            // Throttling 남은 허가증이 없으면 여기서 대기 (다른 스레드에게 양보)
            // Semaphore의 permit(허가증)을 하나 획득
            apiThrottle.acquire();
            acquired = true;
            log.info("Wiki 백그라운드 가공 시작 (ID: {})", knowledgeId);
            // 1. 라인 인덱싱 적용
            String indexedContent = addLineNumbers(rawContent);
            log.debug("Wiki 인덱싱된 원문 미리보기:\n{}", indexedContent.substring(0, Math.min(indexedContent.length(), 200)) + "...");

            // 2. 프롬프트 결합 및 AI 호출
            String prompt = WIKI_PROMPT + "\n\n" + indexedContent;
            AiRequest aiRequest = AiRequest.of(prompt, AIModel.GEMINI, List.of());
            AiClientStrategy strategy = aiClientFactory.get(aiRequest.model());
            String summarized = strategy.call(aiRequest);

            // 3. 가공된 결과 업데이트
            updateKnowledgeResult(knowledgeId, summarized);

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


    private void updateKnowledgeResult(Long knowledgeId, String summarized) {
        // 요약본 업데이트
        persistencePort.updateSummary(
                knowledgeId,
                summarized
        );
        // 태그 추출 및 저장
        List<String> extractedTags = extractTags(summarized);
        if (!extractedTags.isEmpty()) {
            List<KnowledgeTag> tagList = extractedTags.stream()
                    .map(tagName -> KnowledgeTag.create(knowledgeId, tagName))
                    .toList();
            persistencePort.saveTags(tagList);
            log.info("Wiki 태그 자동 추출 및 저장 완료 ({}개) - ID: {}", extractedTags.size(),
                    knowledgeId);
        }
        log.info("Wiki 성공적으로 요약본 업데이트 완료 (ID: {})", knowledgeId);
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
