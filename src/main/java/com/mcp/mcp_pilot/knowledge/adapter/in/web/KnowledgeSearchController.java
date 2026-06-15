package com.mcp.mcp_pilot.knowledge.adapter.in.web;

import com.mcp.mcp_pilot.ai.vector.entity.VectorStoreEntity;
import com.mcp.mcp_pilot.ai.vector.repository.VectorStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class KnowledgeSearchController {

    private final VectorStoreRepository vectorStoreRepository;

    @GetMapping("/test")
    public String test() {
       VectorStoreEntity vectorStoreEntity = vectorStoreRepository.findById(1L).orElse(null);
       float[] vector = vectorStoreEntity.getEmbeddingVector();
       System.out.println(" 복구된 벡터 첫 3개: " + vector[0] + ", " + vector[1] + ", " + vector[2]);
       System.out.println(" 벡터 전체 길이: " + vector.length);
       return null;
    }
}

