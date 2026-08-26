package com.mcp.mcp_pilot.document.adapter.in.web;

import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.document.adapter.in.web.dto.DocumentResponse;
import com.mcp.mcp_pilot.document.port.in.DeleteDocumentUseCase;
import com.mcp.mcp_pilot.document.port.in.DocumentQueryUseCase;
import com.mcp.mcp_pilot.document.port.in.dto.DocumentDownloadResult;
import com.mcp.mcp_pilot.document.port.in.dto.DocumentResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Tag(name = "Document Management", description = "문서 조회 및 관리")
@Slf4j
@RestController
@RequestMapping("/api/{version}/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentQueryUseCase documentQueryUseCase;
    private final DeleteDocumentUseCase deleteDocumentUseCase;

    @Operation(summary = "업로드된 문서 전체 목록 조회")
    @GetMapping(version = "v1")
    public ApiResponse<List<DocumentResponse>> findAll() {
        log.info("[DocumentController] 전체 문서 목록 조회 요청 수신 (Web Adapter)");
        List<DocumentResult> documentResults = documentQueryUseCase.findAll();
        log.info("[DocumentController] 전체 문서 목록 조회 응답 반환 - 총 {}건", documentResults.size());
        return ApiResponse.success(documentResults.stream().map(DocumentResponse::from).toList());
    }

    @Operation(summary = "문서 단건 상세 조회")
    @GetMapping(path = "/{id}", version = "v1")
    public ApiResponse<DocumentResponse> findById(@PathVariable Long id) {
        log.info("[DocumentController] 문서 단건 상세 조회 요청 수신 (Web Adapter) - id: {}", id);
        DocumentResult documentResult = documentQueryUseCase.findById(id);
        log.info("[DocumentController] 문서 단건 상세 조회 응답 반환 - id: {}, fileName: {}", id, documentResult.fileName());
        return ApiResponse.success(DocumentResponse.from(documentResult));
    }

    @Operation(summary = "문서 원본 다운로드")
    @GetMapping(path = "/{id}/download", version = "v1")
    public ResponseEntity<Resource> download(@PathVariable("id") Long id) {
        log.info("[DocumentController] 문서 다운로드 요청 수신 (Web Adapter) - id: {}", id);
        DocumentDownloadResult result = documentQueryUseCase.download(id);
        String encodedFileName = URLEncoder.encode(result.fileName(), StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        log.info("[DocumentController] 문서 다운로드 스트림 응답 반환 - id: {}, fileName: {}, size: {} bytes",
                id, result.fileName(), result.fileSize());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType()))
                .contentLength(result.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
                .body(new InputStreamResource(result.inputStream()));
    }

    @Operation(summary = "문서 및 연관 벡터 연쇄 삭제 ([X] 버튼)")
    @DeleteMapping(version = "v1")
    public ApiResponse<Void> delete(@RequestParam List<Long> ids) {
        log.info("[DocumentController] 문서 일괄 삭제 요청 수신 (Web Adapter) - ids: {}", ids.size());
        deleteDocumentUseCase.deleteDocument(ids);
        log.info("[DocumentController] 문서 삭제 완료");
        return ApiResponse.success(null);
    }
}
