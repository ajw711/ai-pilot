package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.exception.InvalidKnowledgeStatusException;
import com.mcp.mcp_pilot.knowledge.exception.KnowledgeNotFoundException;
import com.mcp.mcp_pilot.knowledge.port.in.dto.ApproveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KnowledgeApproveServiceTest {

    @Mock
    private KnowledgePersistencePort persistencePort;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private KnowledgeApproveService knowledgeApproveService;


    @ParameterizedTest
    @EnumSource(
            value = KnowledgeStatus.class,
            names = {
                    "FAILED_AT_NOTION_PUBLISH",
                    "FAILED_AT_VECTOR_INDEX"
            }
    )
    void retry_preservesContentAndStatus_andPublishesEvent(
            KnowledgeStatus failedStatus
    ) {
        Long id = 1L;
        KnowledgeLog target = new KnowledgeLog(
                id, "제목", "원문", "저장된 본문",
                null, null, 80, "{}",
                failedStatus, 0, null
        );

        when(persistencePort.findById(id))
                .thenReturn(Optional.of(target));

        var result = knowledgeApproveService.approve(
                new ApproveKnowledgeCommand(id, null)
        );

        // 재시도 요청만 접수했으므로 완료 상태로 바꾸지 않음
        assertEquals(failedStatus, target.getStatus());
        assertEquals(failedStatus, result.status());
        assertEquals("저장된 본문", target.getFormattedContent());

        verify(persistencePort, never()).save(any());
        verify(persistencePort, never()).updateStatus(any(), any());

        var eventCaptor =
                ArgumentCaptor.forClass(KnowledgeProcessedEvent.class);

        verify(applicationEventPublisher)
                .publishEvent(eventCaptor.capture());

        assertEquals(id, eventCaptor.getValue().knowledgeId());
    }


    @Test
    @DisplayName("REVIEW_READY 상태의 지식을 승인하면 status가 APPROVED로 변경되고 save가 호출되며 발행 이벤트가 발송된다")
    void approve_success() {
        // Given
        Long id = 1L;
        ApproveKnowledgeCommand command = new ApproveKnowledgeCommand(id, "최종 포맷된 내용");
        KnowledgeLog target = new KnowledgeLog(
                id, "제목", "원문", "기존 포맷",
                null, null, 80, "{}",
                KnowledgeStatus.REVIEW_READY, 0, null
        );

        when(persistencePort.findById(id)).thenReturn(Optional.of(target));
        when(persistencePort.save(any(KnowledgeLog.class))).thenReturn(target);

        // When
        knowledgeApproveService.approve(command);

        // Then
        assertEquals(KnowledgeStatus.REVIEW_APPROVED, target.getStatus());
        assertEquals("최종 포맷된 내용", target.getFormattedContent());

        verify(persistencePort).findById(id);
        verify(persistencePort).save(target);
        verify(applicationEventPublisher).publishEvent(any(KnowledgeProcessedEvent.class));
    }

    @Test
    @DisplayName("지식이 존재하지 않으면 KnowledgeNotFoundException이 발생한다")
    void approve_notFound() {
        // Given
        Long id = 999L;
        ApproveKnowledgeCommand command = new ApproveKnowledgeCommand(id, "내용");

        when(persistencePort.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(KnowledgeNotFoundException.class, () -> knowledgeApproveService.approve(command));
        verify(persistencePort, never()).save(any());
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    @DisplayName("REVIEW_READY 상태가 아닌 지식을 승인하려고 하면 InvalidKnowledgeStatusException이 발생한다")
    void approve_invalidStatus() {
        // Given
        Long id = 1L;
        ApproveKnowledgeCommand command = new ApproveKnowledgeCommand(id, "내용");
        KnowledgeLog target = new KnowledgeLog(
                id, "제목", "원문", "기존 포맷",
                null, null, 80, "{}",
                KnowledgeStatus.VERIFYING, 0, null
        );

        when(persistencePort.findById(id)).thenReturn(Optional.of(target));

        // When & Then
        assertThrows(InvalidKnowledgeStatusException.class, () -> knowledgeApproveService.approve(command));
        verify(persistencePort, never()).save(any());
        verifyNoInteractions(applicationEventPublisher);
    }
}
