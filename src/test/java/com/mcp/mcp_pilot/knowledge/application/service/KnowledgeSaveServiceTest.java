package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KnowledgeSaveServiceTest {

    @Mock private KnowledgePersistencePort persistencePort;
    @Mock private AIClientFactory aiClientFactory;
    @Mock private AiClientStrategy aiClientStrategy;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private MeterRegistry meterRegistry;
    @Mock private ExecutorService executorService;
    @Mock private Counter counter;

    @InjectMocks
    private KnowledgeSaveService knowledgeSaveService;

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
    }

    /**
     * Layer 1: Entry Point (Synchronous)
     * - 저장이 성공하고, 비동기 작업이 "제출"되었는가? (현실 왜곡 없이 트리거만 확인)
     */
    @Test
    @DisplayName("지식 저장 시 동기 로직이 실행되고 비동기 가공 프로세스가 제출되어야 한다")
    void saveKnowledge_EntryPoint_Test() {
        // Given
        SaveKnowledgeCommand cmd = new SaveKnowledgeCommand("제목", "내용", null, List.of(), List.of());
        KnowledgeLog log = KnowledgeLog.create("제목", "내용", null);
        ReflectionTestUtils.setField(log, "id", 1L);

        when(persistencePort.save(any())).thenReturn(log);

        // When
        knowledgeSaveService.saveKnowledge(cmd);

        // Then
        verify(persistencePort).save(any());
        verify(eventPublisher).publishEvent(any(KnowledgeProcessedEvent.class));
        // 비동기 작업이 실제로 제출되었는지 확인 (실행은 하지 않음)
        verify(executorService).submit(any(Runnable.class));
        
        // 동기 구간에서는 AI 관련 동작이 없어야 함
        verifyNoInteractions(aiClientFactory);
    }

    /**
     * Layer 2: Async Pipeline (Internal Logic)
     * - 비동기 메서드를 직접 호출하여 내부 가공 로직만 정밀 검증
     */
    @Test
    @DisplayName("비동기 가공 메서드 호출 시 AI 요약 및 DB 업데이트 흐름을 검증한다")
    void processWikiAsync_Logic_Test() {
        // Given
        long id = 1L;
        String rawContent = "원본 내용";
        
        when(aiClientFactory.get(AIModel.GEMINI)).thenReturn(aiClientStrategy);
        when(aiClientStrategy.call(any())).thenReturn("AI 요약 결과 #Java");

        // TransactionTemplate 즉시 실행 모킹 (정석적인 방법)
        doAnswer(inv -> {
            TransactionCallbackWithoutResult cb = inv.getArgument(0);
            cb.doInTransaction(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        // When
        // 비동기 로직 메서드를 직접 호출하여 동기적으로 검증
        //knowledgeSaveService.processWikiAsync(id, rawContent);

        // Then
        verify(aiClientStrategy).call(any());
        verify(persistencePort).updateSummary(eq(id), eq("AI 요약 결과 #Java"));
        verify(persistencePort).saveTags(argThat(tags -> tags.get(0).getTagName().equals("Java")));
        // 가공 완료 후 발행되는 두 번째 이벤트 확인
        verify(eventPublisher).publishEvent(any(KnowledgeProcessedEvent.class));
    }
}
