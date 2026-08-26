package com.mcp.mcp_pilot.document.application.service;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.document.adapter.in.web.dto.DocumentResponse;
import com.mcp.mcp_pilot.document.adapter.in.web.mapper.DocumentWebMapper;
import com.mcp.mcp_pilot.document.domain.DocumentFile;
import com.mcp.mcp_pilot.document.exception.FileException;
import com.mcp.mcp_pilot.document.port.in.DocumentQueryUseCase;
import com.mcp.mcp_pilot.document.port.in.dto.DocumentDownloadResult;
import com.mcp.mcp_pilot.document.port.in.dto.DocumentResult;
import com.mcp.mcp_pilot.document.port.out.DocumentFileRepositoryPort;
import com.mcp.mcp_pilot.document.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentQueryService implements DocumentQueryUseCase {

    private final DocumentFileRepositoryPort documentFileRepositoryPort;
    private final FileStoragePort fileStoragePort;

    @Override
    public List<DocumentResult> findAll() {
        log.info("[DocumentQueryService] 전체 문서 목록 조회");
        return documentFileRepositoryPort.findAll().stream()
                .map(DocumentResult::from).toList();
    }

    @Override
    public DocumentResult findById(Long id) {
        log.info("[DocumentQueryService] 문서 단건 조회. id={}", id);
        return documentFileRepositoryPort.findById(id)
                .map(DocumentResult::from)
                .orElseThrow(() -> new FileException(ErrorCode.FILE_NOT_FOUND));
    }

    @Override
    public DocumentDownloadResult download(Long id) {
        log.info("[DocumentQueryService] 문서 다운로드 요청. id={}", id);
        DocumentFile documentFile = documentFileRepositoryPort.findById(id)
                .orElseThrow(() -> new FileException(ErrorCode.FILE_NOT_FOUND));
        InputStream inputStream = fileStoragePort.download(documentFile.getR2Key());
        return DocumentDownloadResult.from(documentFile, inputStream);
    }
}
