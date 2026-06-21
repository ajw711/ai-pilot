package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.knowledge.application.TagExtractor;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeSource;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeTag;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.domain.vo.VerificationReport;
import com.mcp.mcp_pilot.knowledge.domain.policy.DecisionPolicy;
import com.mcp.mcp_pilot.knowledge.port.in.SaveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeAiPort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeSaveService implements SaveKnowledgeUseCase {

    private final KnowledgePersistencePort persistencePort;
    private final KnowledgeAiPort knowledgeAiPort;
    private final TagExtractor tagExtractor;
    private final ExecutorService wikiExecutor;
    private final TransactionTemplate transactionTemplate;
    private final MeterRegistry meterRegistry;


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
                        command.formattedContent()
                )
        );
        Long knowledgeId = savedLog.getId();

        if (command.sourceUrls() != null && !command.sourceUrls().isEmpty()) {
            List<KnowledgeSource> sources = command.sourceUrls().stream()
                    .map( url -> KnowledgeSource.create(knowledgeId, url))
                    .toList();
            persistencePort.saveSources(sources);
        }

        // 사용자가 포맷팅 본문을 기입해 보낸 경우 -> AI 분석 생략하고 바로 수동 검토 대기(REVIEW_READY)로 진입
        if (command.formattedContent() != null && !command.formattedContent().isBlank()) {
            persistencePort.updateVerificationAndSummary(
                    knowledgeId,
                    command.formattedContent(),
                    100,
                    null,
                    KnowledgeStatus.REVIEW_READY
            );
            log.info("[SaveService] 사용자 작성 포맷팅본을 수동 검토 대기(REVIEW_READY) 상태로 저장 완료 - ID: {}", knowledgeId);
        } else {
            // 사용자가 원문만 보낸 경우 -> AI 파이프라인 비동기 트리거
            wikiExecutor.submit(() -> processWikiAsync(knowledgeId, command.rawContent()));
        }
        log.info("[SaveService] 지식 1차 저장 완료 ID: {}", knowledgeId);
        return savedLog;
    }

    private void processWikiAsync(long knowledgeId, String rawContent) {
        try {
            log.info("[SaveService] AI 검수 및 포맷팅 시작 (ID: {})", knowledgeId);
            persistencePort.updateStatus(knowledgeId, KnowledgeStatus.VERIFYING);

            // 1. AI Verifier 포트 호출 (외부 기술 격리)
            VerificationReport verifyReport = knowledgeAiPort.verify(rawContent);

            // 2. 의사결정 정책에 따른 점수 및 상태 결정
            int score = DecisionPolicy.calculateScore(verifyReport);
            KnowledgeStatus nextStatus = DecisionPolicy.decide(verifyReport);

            persistencePort.updateStatus(knowledgeId, KnowledgeStatus.FORMATTING);

            // 3. AI Formatter 포트 호출
            String formattedContent = knowledgeAiPort.format(rawContent);

            // 4. 가공 완료 후 의사결정 상태로 전환하여 유저 검토 유도
            completeProcessing(knowledgeId, formattedContent, score, verifyReport, nextStatus);

        } catch (Exception e) {
            log.error("Wiki 비동기 AI 가공 프로세스 실패 (ID: {})", knowledgeId, e);
            persistencePort.updateStatus(knowledgeId, KnowledgeStatus.FAILED);
        }
    }

    protected void completeProcessing(
            Long knowledgeId,
            String formatted,
            int score,
            VerificationReport verifyReport,
            KnowledgeStatus nextStatus) {
        transactionTemplate.executeWithoutResult(s -> {
            // 1. 결과 일괄 업데이트
            persistencePort.updateVerificationAndSummary(knowledgeId, formatted, score, verifyReport, nextStatus);

            // 2. 태그 추출 및 저장
            List<String> extractedTags = tagExtractor.extractTags(formatted);
            if (!extractedTags.isEmpty()) {
                List<KnowledgeTag> tagList = extractedTags.stream()
                        .map(tagName -> KnowledgeTag.create(knowledgeId, tagName))
                        .toList();
                persistencePort.saveTags(tagList);
                log.info("[SaveService] 태그 자동 추출 및 저장 완료 ({}개) - ID: {}",
                        extractedTags.size(), knowledgeId);
            }
            log.info("[SaveService] 지식 가공 데이터 업데이트 완료 (ID: {}, Score: {}, Status: {})", knowledgeId, score, nextStatus);

        });
    }
}
