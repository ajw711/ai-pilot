package com.mcp.mcp_pilot.document.application.service;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.document.domain.DocumentFile;
import com.mcp.mcp_pilot.document.domain.vo.DocumentStatus;
import com.mcp.mcp_pilot.document.exception.FileException;
import com.mcp.mcp_pilot.document.port.in.dto.DocumentDownloadResult;
import com.mcp.mcp_pilot.document.port.in.dto.DocumentResult;
import com.mcp.mcp_pilot.document.port.out.DocumentFileRepositoryPort;
import com.mcp.mcp_pilot.document.port.out.FileStoragePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentQueryServiceTest {

    @Mock
    private DocumentFileRepositoryPort documentFileRepositoryPort;

    @Mock
    private FileStoragePort fileStoragePort;

    @InjectMocks
    private DocumentQueryService documentQueryService;

    @Test
    @DisplayName("전체 문서 목록을 조회하여 DocumentResult 리스트로 반환한다")
    void findAll_success() {
        // given
        DocumentFile doc1 = new DocumentFile(1L, "k8s.pdf", "application/pdf", 100L, "key-1", DocumentStatus.UPLOADED, LocalDateTime.now(), null, 1L, null);
        DocumentFile doc2 = new DocumentFile(2L, "nginx.md", "text/markdown", 200L, "key-2", DocumentStatus.UPLOADED, LocalDateTime.now(), null, 1L, null);

        when(documentFileRepositoryPort.findAll()).thenReturn(List.of(doc1, doc2));

        // when
        List<DocumentResult> results = documentQueryService.findAll();

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).fileName()).isEqualTo("k8s.pdf");
        assertThat(results.get(1).fileName()).isEqualTo("nginx.md");
        verify(documentFileRepositoryPort, times(1)).findAll();
    }

    @Test
    @DisplayName("단건 문서 ID로 상세 정보를 조회한다")
    void findById_success() {
        // given
        Long id = 1L;
        DocumentFile doc = new DocumentFile(id, "k8s.pdf", "application/pdf", 100L, "key-1", DocumentStatus.UPLOADED, LocalDateTime.now(), null, 1L, null);
        when(documentFileRepositoryPort.findById(id)).thenReturn(Optional.of(doc));

        // when
        DocumentResult result = documentQueryService.findById(id);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fileName()).isEqualTo("k8s.pdf");
        verify(documentFileRepositoryPort, times(1)).findById(id);
    }

    @Test
    @DisplayName("존재하지 않는 문서 ID 조회 시 FILE_NOT_FOUND 예외가 발생한다")
    void findById_notFound_throwsException() {
        // given
        Long id = 999L;
        when(documentFileRepositoryPort.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> documentQueryService.findById(id))
                .isInstanceOf(FileException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);
    }

    @Test
    @DisplayName("문서 ID로 R2 원본 파일 스트림을 획득하여 다운로드 결과를 반환한다")
    void download_success() {
        // given
        Long id = 1L;
        DocumentFile doc = new DocumentFile(id, "k8s.pdf", "application/pdf", 100L, "documents/uuid-1/k8s.pdf", DocumentStatus.UPLOADED, LocalDateTime.now(), null, 1L, null);
        InputStream mockStream = new ByteArrayInputStream("file content".getBytes());

        when(documentFileRepositoryPort.findById(id)).thenReturn(Optional.of(doc));
        when(fileStoragePort.download("documents/uuid-1/k8s.pdf")).thenReturn(mockStream);

        // when
        DocumentDownloadResult result = documentQueryService.download(id);

        // then
        assertThat(result).isNotNull();
        assertThat(result.fileName()).isEqualTo("k8s.pdf");
        assertThat(result.contentType()).isEqualTo("application/pdf");
        assertThat(result.fileSize()).isEqualTo(100L);
        assertThat(result.inputStream()).isNotNull();

        verify(documentFileRepositoryPort, times(1)).findById(id);
        verify(fileStoragePort, times(1)).download("documents/uuid-1/k8s.pdf");
    }

    @Test
    @DisplayName("다운로드할 문서가 DB에 존재하지 않으면 FILE_NOT_FOUND 예외가 발생한다")
    void download_notFound_throwsException() {
        // given
        Long id = 999L;
        when(documentFileRepositoryPort.findById(id)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> documentQueryService.download(id))
                .isInstanceOf(FileException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_NOT_FOUND);

        verifyNoInteractions(fileStoragePort);
    }
}
