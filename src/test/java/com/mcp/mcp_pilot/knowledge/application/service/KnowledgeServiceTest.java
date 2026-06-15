package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


// 단위 테스트에 공통적으로 사용할 확장 기능을 선언해주는 역할
@ExtendWith(MockitoExtension.class)
class KnowledgeServiceTest {

    @Mock private KnowledgePersistencePort persistencePort;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private AIClientFactory aiClientFactory;
    @Mock private AiClientStrategy aiClientStrategy;
    @Mock private TransactionTemplate transactionTemplate;

    private KnowledgeSaveService knowledgeSaveService;

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate)
                .executeWithoutResult(any());

        ExecutorService executor =
                Executors.newSingleThreadExecutor();
        knowledgeSaveService = new KnowledgeSaveService(
                persistencePort, aiClientFactory, eventPublisher, executor, transactionTemplate
        );

    }

    @Test
    @DisplayName("지식 저장 시 즉시 DB에 저장되고 비동기로 AI 가공 후 업데이트")
    void saveKnowledge() {
        // Given
        SaveKnowledgeCommand command = new SaveKnowledgeCommand(
                "테스트 제목",
                "사용자 질문\nAI: 답변",
                null,
                List.of("java", "spring", "AI"),
                List.of("https://www.naver.com", "https://google.com", "https://github.com")
        );

        KnowledgeLog savedLog = KnowledgeLog.create(command.title(), command.rawContent(), null, null);
        // DB 저장 시 ID가 부여된 객체가 반환된다고 가정
        ReflectionTestUtils.setField(savedLog, "id", 1L);

        when(persistencePort.save(any(KnowledgeLog.class)))
                .thenReturn(savedLog);
                
        // 비동기 작업 시 NullPointerException 방지를 위한 Mock 설정
        when(aiClientFactory.get(any()))
                .thenReturn(aiClientStrategy);
        when(aiClientStrategy.call(any()))
                .thenReturn("AI 요약 결과");

        // When
        KnowledgeLog result =
                knowledgeSaveService.saveKnowledge(command);

        // Then
        // 사용자에게 대기 없이 ID가 바로 반환되는가?
        assertEquals(1L, result.getId());
        verify(persistencePort, times(1)).save(any(KnowledgeLog.class));

        // (최대 2초 대기) 가상 스레드가 백그라운드에서 AI를 찔렀는가?
        // timeout(2000) -> 2초 안에 실행되면 바로 통과, 아니면 실패 처리
        verify(aiClientFactory, timeout(2000).times(1)).get(any());
        verify(aiClientStrategy, timeout(2000).times(1)).call(any());

        // 가공된 결과로 DB 업데이트가 호출되었는가?
        // KnowledgeService의 updateSummary 메서드가 잘 호출되었는지 확인
        verify(persistencePort, timeout(2000).times(1)).updateSummary(eq(1L), eq("AI 요약 결과"));

        // 이벤트 발행 여부
        verify(eventPublisher, timeout(2000).times(1)).publishEvent(new KnowledgeProcessedEvent(1L));
    }

}