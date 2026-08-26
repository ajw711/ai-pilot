package com.mcp.mcp_pilot.document.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.port.VectorIndexingUseCase;
import com.mcp.mcp_pilot.document.domain.DocumentFile;
import com.mcp.mcp_pilot.document.port.in.DeleteDocumentUseCase;
import com.mcp.mcp_pilot.document.port.out.DocumentFileRepositoryPort;
import com.mcp.mcp_pilot.document.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDeleteService implements DeleteDocumentUseCase {

    private final DocumentFileRepositoryPort documentFileRepositoryPort;
    private final FileStoragePort fileStoragePort;
    private final VectorIndexingUseCase vectorIndexingUseCase;

    @Override
    @Transactional
    public void deleteDocument(List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            log.warn("[DocumentDeleteService] 삭제할 문서 ID 목록이 비어있습니다.");
            return;
        }

        log.info("[DocumentDeleteService] 문서 일괄 연쇄 삭제 시작 - 요청 ID 수: {}, IDs: {}", documentIds.size(), documentIds);

        // 삭제 대상 문서들 일괄 조회
        List<DocumentFile> documents = documentFileRepositoryPort.findAllById(documentIds);
        if (documents.isEmpty()) {
            log.warn("[DocumentDeleteService] 삭제 대상 문서가 DB에 존재하지 않습니다.");
            return;
        }

        // R2 스토리지 원본 파일 및 Vector Store 청크 벡터 일괄 삭제
        for (DocumentFile document : documents) {
            log.info("[DocumentDeleteService] R2 파일 및 벡터 삭제 - id: {}, fileName: {}, r2Key: {}",
                    document.getId(), document.getFileName(), document.getR2Key());
            fileStoragePort.delete(document.getR2Key());
            vectorIndexingUseCase.deleteIndex(VectorTargetType.FILE_DOCUMENT, document.getId());
        }
        //  PostgreSQL document_file 레코드 일괄 삭제
        documentFileRepositoryPort.deleteAllById(documentIds);
        log.info("[DocumentDeleteService] 문서 일괄 연쇄 삭제 완료 - 총 {}건 삭제됨", documents.size());
    }
}
