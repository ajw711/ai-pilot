package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.exception.InvalidKnowledgeStatusException;
import com.mcp.mcp_pilot.knowledge.exception.KnowledgeNotFoundException;
import com.mcp.mcp_pilot.knowledge.port.in.dto.ApproveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.ApproveKnowledgeUseCase;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
        assertEquals(KnowledgeStatus.APPROVED, target.getStatus());
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
