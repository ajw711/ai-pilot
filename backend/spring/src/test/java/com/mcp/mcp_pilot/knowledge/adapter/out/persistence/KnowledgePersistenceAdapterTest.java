package com.mcp.mcp_pilot.knowledge.adapter.out.persistence;

import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.entity.KnowledgeLogJpaEntity;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeLogRepository;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeSourceRepository;
import com.mcp.mcp_pilot.knowledge.adapter.out.persistence.repository.KnowledgeTagRepository;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgePersistenceAdapterTest {

    @Mock
    private KnowledgeLogRepository logRepository;

    @Mock
    private KnowledgeSourceRepository sourceRepository;

    @Mock
    private KnowledgeTagRepository tagRepository;

    @Test
    @DisplayName("기존 지식 저장 시 도메인 변경분을 반영하고 Notion 페이지 ID와 URL을 보존한다")
    void saveExistingKnowledge_preservesNotionPublication() {
        Long id = 1L;
        String pageId = "existing-page-id";
        String pageUrl = "https://www.notion.so/existing-page-id";

        KnowledgeLogJpaEntity existing = KnowledgeLogJpaEntity.create(
                "제목",
                "원문",
                "기존 본문",
                pageId,
                pageUrl,
                80,
                "{}",
                KnowledgeStatus.FAILED_AT_VECTOR_INDEX,
                1,
                null
        );
        existing.setId(id);

        KnowledgeLog changed = new KnowledgeLog(
                id,
                "제목",
                "원문",
                "변경된 본문",
                null,
                null,
                80,
                "{}",
                KnowledgeStatus.REVIEW_APPROVED,
                1,
                null
        );

        when(logRepository.findById(id)).thenReturn(Optional.of(existing));
        when(logRepository.save(any(KnowledgeLogJpaEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        KnowledgePersistenceAdapter adapter = new KnowledgePersistenceAdapter(
                logRepository,
                sourceRepository,
                tagRepository,
                JsonMapper.builder().build()
        );

        KnowledgeLog result = adapter.save(changed);

        ArgumentCaptor<KnowledgeLogJpaEntity> captor =
                ArgumentCaptor.forClass(KnowledgeLogJpaEntity.class);
        verify(logRepository).save(captor.capture());
        KnowledgeLogJpaEntity saved = captor.getValue();

        assertAll(
                () -> assertEquals(id, saved.getId()),
                () -> assertEquals(pageId, saved.getNotionPageId()),
                () -> assertEquals(pageUrl, saved.getNotionPageUrl()),
                () -> assertEquals("변경된 본문", saved.getFormattedContent()),
                () -> assertEquals(KnowledgeStatus.REVIEW_APPROVED, saved.getStatus()),
                () -> assertEquals(id, result.getId()),
                () -> assertEquals("변경된 본문", result.getFormattedContent()),
                () -> assertEquals(KnowledgeStatus.REVIEW_APPROVED, result.getStatus())
        );
    }
}
