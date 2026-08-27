package com.mcp.mcp_pilot.document.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.port.VectorIndexingUseCase;
import com.mcp.mcp_pilot.document.domain.DocumentFile;
import com.mcp.mcp_pilot.document.domain.vo.DocumentStatus;
import com.mcp.mcp_pilot.document.port.out.DocumentFileRepositoryPort;
import com.mcp.mcp_pilot.document.port.out.FileStoragePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentDeleteServiceTest {

    @Mock
    private DocumentFileRepositoryPort documentFileRepositoryPort;

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private VectorIndexingUseCase vectorIndexingUseCase;

    @InjectMocks
    private DocumentDeleteService documentDeleteService;

    @Test
    @DisplayName("문서 ID 목록을 전달받아 R2 파일, Vector Store 청크, DB 레코드를 연쇄 일괄 삭제한다")
    void deleteDocuments_success() {
        // given
        DocumentFile doc1 = new DocumentFile(1L, "k8s.pdf", "application/pdf", 100L, "documents/uuid-1/k8s.pdf", DocumentStatus.UPLOADED, LocalDateTime.now(), null, 1L, null);
        DocumentFile doc2 = new DocumentFile(2L, "nginx.md", "text/markdown", 200L, "documents/uuid-2/nginx.md", DocumentStatus.UPLOADED, LocalDateTime.now(), null, 1L, null);

        List<DocumentFile> documents = List.of(doc1, doc2);
        List<Long> ids = List.of(1L, 2L);
        when(documentFileRepositoryPort.findAllById(ids)).thenReturn(List.of(doc1, doc2));

        // when
        documentDeleteService.deleteDocument(ids);

        // then
        // R2 스토리지 파일 삭제 검증
        verify(fileStoragePort, times(1)).delete("documents/uuid-1/k8s.pdf");
        verify(fileStoragePort, times(1)).delete("documents/uuid-2/nginx.md");

        // Vector Store 벡터 청크 삭제 검증
        verify(vectorIndexingUseCase, times(1)).deleteIndex(VectorTargetType.FILE_DOCUMENT, 1L);
        verify(vectorIndexingUseCase, times(1)).deleteIndex(VectorTargetType.FILE_DOCUMENT, 2L);

        // PostgreSQL DB 레코드 일괄 삭제 검증
        verify(documentFileRepositoryPort, times(1)).saveAll(documents);
    }

    @Test
    @DisplayName("삭제할 ID 목록이 비어있거나 null인 경우 아무 작업도 하지 않는다")
    void deleteDocuments_emptyList_doesNothing() {
        // when
        documentDeleteService.deleteDocument(List.of());
        documentDeleteService.deleteDocument(null);

        // then
        verifyNoInteractions(documentFileRepositoryPort);
        verifyNoInteractions(fileStoragePort);
        verifyNoInteractions(vectorIndexingUseCase);
    }

    @Test
    @DisplayName("전달받은 ID에 해당하는 문서가 DB에 없으면 삭제 처리를 진행하지 않는다")
    void deleteDocuments_nonExistentIds_doesNothing() {
        // given
        List<Long> ids = List.of(999L);
        when(documentFileRepositoryPort.findAllById(ids)).thenReturn(List.of());

        // when
        documentDeleteService.deleteDocument(ids);

        // then
        verify(documentFileRepositoryPort, times(1)).findAllById(ids);
        verifyNoInteractions(fileStoragePort);
        verifyNoInteractions(vectorIndexingUseCase);
        verify(documentFileRepositoryPort, never()).saveAll(any());
    }
}
