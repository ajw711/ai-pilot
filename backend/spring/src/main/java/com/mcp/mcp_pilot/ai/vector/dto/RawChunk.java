package com.mcp.mcp_pilot.ai.vector.dto;

import java.util.Map;

public record RawChunk(
        int chunkIndex,                 // 청크 순번 (0, 1, 2...)
        String content,                 // 실제 텍스트 본문 (임베딩 및 LLM 주입 대상)
        Map<String, Object> metadata    // 출처 메타데이터 (파일명, 페이지, 헤딩 경로 등)
) {

    public static RawChunk of(int chunkIndex, String content, Map<String, Object> metadata) {
        return new RawChunk(
                chunkIndex,
                content,
                metadata != null ? Map.copyOf(metadata) : Map.of()
        );
    }
}
