package com.mcp.mcp_pilot.document.adapter.out;

import com.mcp.mcp_pilot.ai.vector.dto.RawChunk;
import com.mcp.mcp_pilot.document.port.out.DocumentParserPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;


import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentParserAdapter implements DocumentParserPort {

    /**
     *     Spring AI 2.0.0+ 표준 빌더 방식
     *     DocumentTransformer 구현체
     *     private final TokenTextSplitter textSplitter = TokenTextSplitter.builder()
     *             .withChunkSize(500)               // 목표 청크 크기 (토큰 수)
     *             .withMinChunkSizeChars(50)        // 청크 최소 문자 수
     *             .withMinChunkLengthToEmbed(5)     // 임베딩할 최소 길이
     *             .withMaxNumChunks(10000)          // 최대 청크 수
     *             .withKeepSeparator(true)          // 구분자(줄바꿈 등) 보존
     *             .build();
     */
    private final TokenTextSplitter textSplitter = TokenTextSplitter.builder()
            .withChunkSize(500)
            .withKeepSeparator(true)
            .build();


    @Override
    public List<RawChunk> parse(InputStream inputStream, String fileName) {
        log.info("[DocumentParserAdapter] 파일 텍스트 추출 및 청킹 시작. fileName={}", fileName);
        try {
            // Tika로 문서에서 텍스트 및 기본 메타데이터 추출
            TikaDocumentReader reader = new TikaDocumentReader(new InputStreamResource(inputStream));
            List<Document> documents = reader.read();

            if (documents.isEmpty()) {
                log.warn("[DocumentParserAdapter] 파일에서 추출된 텍스트가 없습니다. fileName={}", fileName);
                return List.of();
            }

            // 토큰 크기 기반으로 적절히 청킹
            List<Document> chunkedDocs = textSplitter.apply(documents);

            // 공통 경계 DTO인 List<RawChunk>로 변환하여 반환
            return IntStream.range(0, chunkedDocs.size())
                    .mapToObj(index -> {
                        Document chunkDoc = chunkedDocs.get(index);

                        Map<String, Object> metadata = new HashMap<>(chunkDoc.getMetadata());
                        metadata.put("fileName", fileName);

                        return RawChunk.of(index, chunkDoc.getText(), metadata);
                    })
                    .toList();

        } catch (Exception e) {
            log.error("[DocumentParserAdapter] 파일 파싱 중 에러 발생. fileName={}", fileName, e);
            throw new RuntimeException("문서 파일 파싱에 실패했습니다: " + fileName, e);
        }
    }
}
