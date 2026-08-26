package com.mcp.mcp_pilot.document.adapter.in.web;

import com.mcp.mcp_pilot.common.dto.ApiResponse;
import com.mcp.mcp_pilot.common.security.CustomUserPrincipal;
import com.mcp.mcp_pilot.document.adapter.in.web.dto.UploadDocumentResponse;
import com.mcp.mcp_pilot.document.adapter.in.web.mapper.DocumentWebMapper;
import com.mcp.mcp_pilot.document.port.in.UploadDocumentUseCase;
import com.mcp.mcp_pilot.document.port.in.dto.UploadDocumentCommand;
import com.mcp.mcp_pilot.document.port.in.dto.UploadDocumentResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Document Upload", description = "파일 업로드")
@Slf4j
@RestController
@RequestMapping("/api/{version}/document")
@RequiredArgsConstructor
public class DocumentUploadController {

    private final UploadDocumentUseCase uploadDocumentUseCase;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, version = "v1")
    public ApiResponse<UploadDocumentResponse> upload(@RequestPart("files") List<MultipartFile> files, @AuthenticationPrincipal CustomUserPrincipal principal) {
        log.info("파일 업로드 요청 수신 (Web Adapter): {}", files.size());
        // 외부 데이터를 내부 command로 변환
        UploadDocumentCommand command = DocumentWebMapper.toCommand(files, principal.getId());
        UploadDocumentResult result = uploadDocumentUseCase.uploadDocument(command);
        return ApiResponse.success(UploadDocumentResponse.from(result));
    }
}
