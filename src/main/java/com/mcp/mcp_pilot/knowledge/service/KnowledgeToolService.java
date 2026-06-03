package com.mcp.mcp_pilot.knowledge.service;

import com.mcp.mcp_pilot.common.ToolExecutor;
import com.mcp.mcp_pilot.common.dto.ToolResponse;
import com.mcp.mcp_pilot.knowledge.dto.KnowledgeRequest;
import com.mcp.mcp_pilot.knowledge.entity.KnowledgeLogEntity;
import com.mcp.mcp_pilot.knowledge.entity.KnowledgeSourceEntity;
import com.mcp.mcp_pilot.knowledge.entity.KnowledgeTagEntity;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeLogRepository;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeSourceRepository;
import com.mcp.mcp_pilot.knowledge.repository.Knowledge.KnowledgeTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeToolService implements ToolExecutor<KnowledgeRequest, ToolResponse<Long>> {

    private final KnowledgeLogRepository logRepository;
    private final KnowledgeSourceRepository sourceRepository;
    private final KnowledgeTagRepository tagRepository;

    @Override
    @Transactional
    public ToolResponse<Long> execute(KnowledgeRequest request) {
        log.info("지식 저장 프로세스 시작: {}", Arrays.toString(request.tags().toArray()));



        // 1. 지식 로그 저장
        KnowledgeLogEntity logEntity = KnowledgeLogEntity.createLog(
                request.rawContent(),
                request.summarizedContent(), // Notion PageId 향후
                null);
        KnowledgeLogEntity saveLog = logRepository.save(logEntity);
        Long knowledgeId = saveLog.getId();

        // 2. source 저장
        if (!request.sourceUrls().isEmpty()) {
            for (String url : request.sourceUrls()) {
                sourceRepository.save(KnowledgeSourceEntity.createSource(knowledgeId, url));
            }
        }

        // 3. tag 태그 저장
        if (!request.tags().isEmpty()) {
            for (String tagName : request.tags()) {
                tagRepository.save(KnowledgeTagEntity.createTag(knowledgeId, tagName));
            }
        }

        return ToolResponse.success(
                "지식 저장 완료",
                knowledgeId
        );
    }
}
