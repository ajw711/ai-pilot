package com.mcp.mcp_pilot.document.application.service;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.document.application.event.DocumentUploadedEvent;
import com.mcp.mcp_pilot.document.domain.DocumentFile;
import com.mcp.mcp_pilot.document.domain.vo.DocumentStatus;
import com.mcp.mcp_pilot.document.exception.FileException;
import com.mcp.mcp_pilot.document.port.in.UploadDocumentUseCase;
import com.mcp.mcp_pilot.document.port.in.dto.UploadDocumentCommand;
import com.mcp.mcp_pilot.document.port.in.dto.UploadDocumentResult;
import com.mcp.mcp_pilot.document.port.out.DocumentFileRepositoryPort;
import com.mcp.mcp_pilot.document.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentUploadService implements UploadDocumentUseCase {

    private final FileStoragePort fileStoragePort;
    private final DocumentFileRepositoryPort documentFileRepositoryPort;
    private final ApplicationEventPublisher applicationEventPublisher;


    @Override
    @Transactional
    public UploadDocumentResult uploadDocument(UploadDocumentCommand command) {
        log.info("[DocumentUploadService] 파일 업로드 시작. 파일 수: {}", command.files().size());

        if (command.files().isEmpty()) {
            log.warn("[uploadDocument] 파일이 없습니다.");
            throw new FileException(ErrorCode.FILE_REQUEST);
        }

        // R2 업로드
        List<DocumentFile> documents = command.files().stream()
                .map(file -> {
                    String r2Key = fileStoragePort.upload(file);
                    return DocumentFile.create(
                            file.fileName(),
                            file.contentType(),
                            file.fileSize(),
                            r2Key,
                            DocumentStatus.UPLOADED,
                            file.uploadBy());

                }).toList();
        // 메타 데이터 DB에 저장
        List<DocumentFile> documentFiles = documentFileRepositoryPort.saveAll(documents);
        log.info("[DocumentUploadService] 파일 업로드 완료. 파일 수: {}", command.files().size());

        applicationEventPublisher.publishEvent(DocumentUploadedEvent.of(documentFiles));
        log.info("[DocumentUploadService] 파일 청크 이벤트 발행");

        return UploadDocumentResult.from(documentFiles);
    }
}
