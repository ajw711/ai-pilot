package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.knowledge.application.event.KnowledgeProcessedEvent;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgeEventPublishPort;
import com.mcp.mcp_pilot.knowledge.port.out.KnowledgePersistencePort;
import com.mcp.mcp_pilot.knowledge.port.out.NotionPublishPort;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgePublishServiceTest {

    @Mock
    private KnowledgePersistencePort persistencePort;

    @Mock
    private KnowledgeEventPublishPort eventPublishPort;

    @Mock
    private NotionPublishPort notionPublishPort;

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @AfterEach
    void tearDown() {
        meterRegistry.close();
    }

    @Test
    @DisplayName("이미 발행된 Notion은 외부 발행과 상태 변경을 생략한다")
    void alreadyPublished_skipsNotionPublication() {
        Long id = 1L;
        when(persistencePort.isPublished(id)).thenReturn(true);

        KnowledgePublishService service = new KnowledgePublishService(
                persistencePort,
                eventPublishPort,
                notionPublishPort,
                meterRegistry
        );

        service.execute(KnowledgeProcessedEvent.of(id));

        verify(persistencePort).isPublished(id);
        verifyNoMoreInteractions(persistencePort);
        verifyNoInteractions(notionPublishPort, eventPublishPort);
    }
}
