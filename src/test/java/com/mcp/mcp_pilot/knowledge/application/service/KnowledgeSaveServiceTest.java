package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.enums.AIModel;
import com.mcp.mcp_pilot.ai.factory.AIClientFactory;
import com.mcp.mcp_pilot.ai.strategy.AiClientStrategy;
import com.mcp.mcp_pilot.knowledge.application.TagExtractor;
import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import com.mcp.mcp_pilot.knowledge.domain.policy.DecisionPolicy;
import com.mcp.mcp_pilot.knowledge.domain.vo.Issue;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.domain.vo.Severity;
import com.mcp.mcp_pilot.knowledge.domain.vo.VerificationReport;
import com.mcp.mcp_pilot.knowledge.port.in.dto.SaveKnowledgeCommand;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeAiPort;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KnowledgeSaveServiceTest {

    @Mock private KnowledgePersistencePort persistencePort;
    @Mock private KnowledgeAiPort knowledgeAiPort;
    @Mock private TagExtractor tagExtractor;
    @Mock private ExecutorService wikiExecutor;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter counter;

    @InjectMocks
    private KnowledgeSaveService knowledgeSaveService;

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
    }


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
        verify(counter).increment(); // 메트릭 검증
        verify(wikiExecutor).submit(any(Runnable.class));
        verifyNoInteractions(knowledgeAiPort);

    }


    @Test
    @DisplayName("비동기 가공 메서드 호출 시 AI 요약 및 DB 업데이트 흐름을 검증한다")
    void processWikiAsync_Logic_Test() {
        // Given
        long id = 1L;
        String rawContent = "원본 내용";

        VerificationReport mockReport = new VerificationReport(
                List.of(new Issue(Severity.WARNING, "대상 문구", "경고 메시지"))
        );
        String formattedContent = "AI 요약 결과 #Java #Spring";

        when(knowledgeAiPort.verify(rawContent)).thenReturn(mockReport);
        when(knowledgeAiPort.format(rawContent)).thenReturn(formattedContent);
        when(tagExtractor.extractTags(anyString())).thenReturn(List.of("Java", "Spring"));

        doAnswer(invocationOnMock -> {
            Consumer<TransactionStatus> cb = invocationOnMock.getArgument(0);
            cb.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        // 정책 결과를 동적으로 받아 매직 넘버 하드코딩 제거
        int expectedScore = DecisionPolicy.calculateScore(mockReport);
        KnowledgeStatus expectedStatus = DecisionPolicy.decide(mockReport);

        // When
        ReflectionTestUtils.invokeMethod(knowledgeSaveService, "processWikiAsync", id, rawContent);

        // Then
        verify(persistencePort).updateStatus(id, KnowledgeStatus.VERIFYING);
        verify(knowledgeAiPort).verify(rawContent);
        verify(persistencePort).updateStatus(id, KnowledgeStatus.PUBLISHED);
        verify(knowledgeAiPort).format(rawContent);
        verify(persistencePort).updateVerificationAndSummary(
                eq(id),
                eq(formattedContent),
                eq(expectedScore),
                eq(mockReport),
                eq(expectedStatus)
        );
        verify(persistencePort).saveTags(
                argThat(tags ->
                        tags.size() == 2 &&
                                tags.stream().anyMatch(t -> t.getTagName().equalsIgnoreCase("Java")) &&
                                tags.stream().anyMatch(t -> t.getTagName().equalsIgnoreCase("Spring"))
                )
        );


    }
}
