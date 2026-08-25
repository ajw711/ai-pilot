package com.mcp.mcp_pilot.document.application.service;

import com.mcp.mcp_pilot.common.exception.ErrorCode;
import com.mcp.mcp_pilot.document.application.event.DocumentUploadedEvent;
import com.mcp.mcp_pilot.document.domain.DocumentFile;
import com.mcp.mcp_pilot.document.domain.vo.DocumentStatus;
import com.mcp.mcp_pilot.document.exception.FileException;
import com.mcp.mcp_pilot.document.port.in.dto.UploadDocumentCommand;
import com.mcp.mcp_pilot.document.port.in.dto.UploadDocumentResult;
import com.mcp.mcp_pilot.document.port.in.dto.UploadFileCommand;
import com.mcp.mcp_pilot.document.port.out.DocumentFileRepositoryPort;
import com.mcp.mcp_pilot.document.port.out.FileStoragePort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentUploadServiceTest {

    @Mock
    private FileStoragePort fileStoragePort;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private DocumentFileRepositoryPort documentFileRepositoryPort;

    @InjectMocks
    private DocumentUploadService documentUploadService;

    @Test
    @DisplayName("다중 파일 업로드 시 각 파일을 R2에 저장하고 DB 메타데이터를 UPLOADED 상태로 일괄 저장한다")
    void uploadDocument_success() {
        // given
        UploadFileCommand file1 = new UploadFileCommand(
                "k8s-guide.pdf",
                "application/pdf",
                1024L,
                new ByteArrayInputStream("dummy-pdf-content".getBytes()),
                1L
        );
        UploadFileCommand file2 = new UploadFileCommand(
                "nginx-troubleshooting.md",
                "text/markdown",
                512L,
                new ByteArrayInputStream("dummy-md-content".getBytes()),
                1L
        );
        UploadDocumentCommand command = new UploadDocumentCommand(List.of(file1, file2));

        when(fileStoragePort.upload(file1)).thenReturn("documents/uuid-1/k8s-guide.pdf");
        when(fileStoragePort.upload(file2)).thenReturn("documents/uuid-2/nginx-troubleshooting.md");
        when(documentFileRepositoryPort.saveAll(anyList())).thenAnswer(invocation -> invocation.
                getArgument(0));

        // when
        UploadDocumentResult result = documentUploadService.uploadDocument(command);

        // then
        assertThat(result).isNotNull();

        verify(fileStoragePort, times(1)).upload(file1);
        verify(fileStoragePort, times(1)).upload(file2);
        verify(applicationEventPublisher, times(1)).publishEvent(any(DocumentUploadedEvent.class));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<DocumentFile>> captor = ArgumentCaptor.forClass(List.class);
        verify(documentFileRepositoryPort, times(1)).saveAll(captor.capture());

        List<DocumentFile> savedDocuments = captor.getValue();
        assertThat(savedDocuments).hasSize(2);

        DocumentFile doc1 = savedDocuments.get(0);
        assertThat(doc1.getFileName()).isEqualTo("k8s-guide.pdf");
        assertThat(doc1.getContentType()).isEqualTo("application/pdf");
        assertThat(doc1.getFileSize()).isEqualTo(1024L);
        assertThat(doc1.getR2Key()).isEqualTo("documents/uuid-1/k8s-guide.pdf");
        assertThat(doc1.getDocumentStatus()).isEqualTo(DocumentStatus.UPLOADED);
        assertThat(doc1.getUploadedBy()).isEqualTo(1L);

        DocumentFile doc2 = savedDocuments.get(1);
        assertThat(doc2.getFileName()).isEqualTo("nginx-troubleshooting.md");
        assertThat(doc2.getContentType()).isEqualTo("text/markdown");
        assertThat(doc2.getFileSize()).isEqualTo(512L);
        assertThat(doc2.getR2Key()).isEqualTo("documents/uuid-2/nginx-troubleshooting.md");
        assertThat(doc2.getDocumentStatus()).isEqualTo(DocumentStatus.UPLOADED);
        assertThat(doc2.getUploadedBy()).isEqualTo(1L);
    }

    @Test
    @DisplayName("파일 목록이 비어있는 경우 FILE_REQUEST 예외가 발생한다")
    void uploadDocument_emptyFileList_throwsException() {
        // given
        UploadDocumentCommand command = new UploadDocumentCommand(List.of());

        // when & then
        assertThatThrownBy(() -> documentUploadService.uploadDocument(command))
                .isInstanceOf(FileException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_REQUEST);

        verifyNoInteractions(fileStoragePort);
        verifyNoInteractions(documentFileRepositoryPort);
    }

    @Test
    @DisplayName("R2 스토리지 업로드 중 예외 발생 시 DB 저장이 호출되지 않는다")
    void uploadDocument_storageFailure_abortsDbSave() {
        // given
        UploadFileCommand file = new UploadFileCommand(
                "error.pdf",
                "application/pdf",
                1024L,
                new ByteArrayInputStream("error".getBytes()),
                1L
        );
        UploadDocumentCommand command = new UploadDocumentCommand(List.of(file));

        when(fileStoragePort.upload(any())).thenThrow(new FileException(ErrorCode.FILE_UPLOAD));

        // when & then
        assertThatThrownBy(() -> documentUploadService.uploadDocument(command))
                .isInstanceOf(FileException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FILE_UPLOAD);

        verifyNoInteractions(documentFileRepositoryPort);
    }
}
