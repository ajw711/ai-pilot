package com.mcp.mcp_pilot.ai.vector.dto;

import java.util.Map;

public record VectorSearchResult(
        Long sourceId,                  // 원본 Knowledge ID 또는 DocumentFile ID
        Long chunkId,                   // VectorStore 청크 PK
        String content,                 // 실제 매칭된 청크 텍스트 본문
        Map<String, Object> metadata,   // 파일명, 페이지, 헤딩 경로 등
        double score                    // 코사인 유사도 점수
) {}