package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.ai.vector.port.VectorIndexingUseCase;
import com.mcp.mcp_pilot.knowledge.application.chunker.MarkdownChunker;
import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeEventPublishPort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeVectorPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeVectorServiceTest {

    @Mock
    private VectorIndexingUseCase vectorIndexingUseCase;

    @Mock
    private MarkdownChunker markdownChunker;

    @Mock
    private KnowledgeEventPublishPort eventPublishPort;

    @Mock
    private KnowledgePersistencePort persistencePort;

    @Mock
    private KnowledgeVectorPort vectorPort;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @AfterEach
    void tearDown() {
        meterRegistry.close();
    }

    @Test
    @DisplayName("이미 저장된 벡터는 인덱싱을 생략하고 집계용 결과 이벤트만 발행한다")
    void alreadyStored_skipsIndexingAndPublishesResultEvent() {
        Long id = 1L;
        when(vectorPort.isVectorStored(id)).thenReturn(true);

        KnowledgeVectorService service = new KnowledgeVectorService(
                vectorIndexingUseCase,
                markdownChunker,
                eventPublishPort,
                persistencePort,
                vectorPort,
                meterRegistry
        );

        service.execute(KnowledgeProcessedEvent.of(id));

        verify(vectorPort).isVectorStored(id);
        verify(eventPublishPort).publish("knowledge.vector.indexed", id);
        verifyNoMoreInteractions(vectorPort, eventPublishPort);
        verifyNoInteractions(
                vectorIndexingUseCase,
                markdownChunker,
                persistencePort
        );
    }
}
