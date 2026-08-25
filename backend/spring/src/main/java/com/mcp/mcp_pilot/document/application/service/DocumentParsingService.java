package com.mcp.mcp_pilot.document.application.service;

import com.mcp.mcp_pilot.ai.constant.VectorTargetType;
import com.mcp.mcp_pilot.ai.vector.dto.RawChunk;
import com.mcp.mcp_pilot.ai.vector.port.VectorIndexingUseCase;
import com.mcp.mcp_pilot.document.application.event.DocumentUploadedEvent;
import com.mcp.mcp_pilot.document.application.event.UploadedDocument;
import com.mcp.mcp_pilot.document.port.in.DocumentParsingUseCase;
import com.mcp.mcp_pilot.document.port.out.DocumentParserPort;
import com.mcp.mcp_pilot.document.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

/**
 * 1. 어떤 파일을 처리할지 확인
 * 2. R2에서 원본 가져오기
 * 3. 파일 형식에 맞는 Parser 호출
 * 4. Parser가 추출한 내용을 Chunking
 * 5. RawChunk 생성
 * 6. VectorIndexingUseCase에 전달
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentParsingService implements DocumentParsingUseCase {

    private final FileStoragePort fileStoragePort;
    private final DocumentParserPort documentParserPort;
    private final VectorIndexingUseCase vectorIndexingUseCase;


    @Override
    public void execute(DocumentUploadedEvent event) {
        log.info("[DocumentParsingService] 문서 청크 수: {}", event.documents().size());
        for (UploadedDocument document : event.documents()) {
            try {
                InputStream inputStream =
                        fileStoragePort.download(document.r2Key());

                List<RawChunk> chunks =
                        documentParserPort.parse(
                                inputStream,
                                document.fileName()
                        );

                vectorIndexingUseCase.indexChunks(
                        VectorTargetType.FILE_DOCUMENT,
                        document.documentFileId(),
                        chunks
                );

            } catch (Exception e) {
                log.error(
                        "[DocumentParsingService] 파일 처리 실패. documentFileId={}",
                        document.documentFileId(),
                        e
                );
            }
        }
    }
}
