package com.mcp.mcp_pilot.document.adapter.in.event;

import com.mcp.mcp_pilot.document.application.event.DocumentUploadedEvent;
import com.mcp.mcp_pilot.document.port.in.DocumentParsingUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentUploadedEventListener {

    private final DocumentParsingUseCase documentParsingUseCase;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDocumentChuckProcessed(DocumentUploadedEvent event) {
        log.info(
                "[Document-Consumer] 파일 파싱 시작 - 파일 수: {}",
                event.documents().size()
        );

        try {
            documentParsingUseCase.execute(event);
        } catch (Exception e) {
            log.error(
                    "[Document-Consumer] 파일 파싱 실패 - 파일 수: {}",
                    event.documents().size(),
                    e
            );
        }
    }
}
