package com.mcp.mcp_pilot.knowledge.adapter.out.notion.mapper;

import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.NotionPageRequest;
import com.mcp.mcp_pilot.knowledge.adapter.out.notion.dto.Parent;
import com.mcp.mcp_pilot.knowledge.domain.entity.KnowledgeLog;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class NotionMapper {

    public NotionPageRequest toRequest(KnowledgeLog log, String databaseId) {
        // Properties (데이터베이스 컬럼 매핑)
        // 노션 DB 컬럼명에 따라 "Name" 또는 "Title" 등으로 조정 필요
        Map<String, Object> properties = Map.of(
                "Name", Map.of(
                        "title", List.of(createTextInput(log.getTitle()))
                )
        );

        // 본문(Children) 생성 - Markdown-ish 요약본을 블록으로 변환
        List<Map<String, Object>> children = parseContentToBlocks(log.getSummarizedContent());

        return new NotionPageRequest(
                new Parent(databaseId),
                properties,
                children
        );
    }

    private List<Map<String, Object>> parseContentToBlocks(String content) {
        List<Map<String, Object>> blocks = new ArrayList<>();
        if (content == null || content.isBlank()) {
            return blocks;
        }

        String[] lines = content.split("\\r?\\n");
        StringBuilder codeBuffer = new StringBuilder();
        boolean inCodeBlock = false;
        String codeLanguage = "plain text";

        for (String line : lines) {
            String trimmedLine = line.trim();

            // 코드 블록 처리
            if (trimmedLine.startsWith("```")) {
                if (inCodeBlock) {
                    blocks.add(createCodeBlock(codeBuffer.toString(), codeLanguage));
                    codeBuffer.setLength(0);
                    inCodeBlock = false;
                } else {
                    inCodeBlock = true;
                    codeLanguage = trimmedLine.length() > 3 ? trimmedLine.substring(3).toLowerCase() : "java";
                }
                continue;
            }

            if (inCodeBlock) {
                codeBuffer.append(line).append("\n");
                continue;
            }

            // 헤더 처리
            if (trimmedLine.startsWith("### ")) {
                blocks.add(createHeadingBlock(3, trimmedLine.substring(4)));
            } else if (trimmedLine.startsWith("## ")) {
                blocks.add(createHeadingBlock(2, trimmedLine.substring(3)));
            } else if (trimmedLine.startsWith("# ")) {
                blocks.add(createHeadingBlock(1, trimmedLine.substring(2)));
            } 
            // 리스트 처리
            else if (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")) {
                blocks.add(createListItemBlock(trimmedLine.substring(2)));
            }
            // 빈 줄 처리
            else if (trimmedLine.isEmpty()) {
                // 노션에서 빈 줄은 생략하거나 빈 단락으로 추가 가능
            }
            // 일반 텍스트 처리
            else {
                blocks.add(createParagraphBlock(line));
            }
        }

        // 마지막에 태그나 출처 정보를 위한 구분선 추가 가능
        blocks.add(Map.of("object", "block", "type", "divider", "divider", Map.of()));

        return blocks;
    }

    private Map<String, Object> createHeadingBlock(int level, String text) {
        String type = "heading_" + level;
        return Map.of(
                "object", "block",
                "type", type,
                type, Map.of("rich_text", List.of(createTextInput(text)))
        );
    }

    private Map<String, Object> createParagraphBlock(String text) {
        return Map.of(
                "object", "block",
                "type", "paragraph",
                "paragraph", Map.of("rich_text", List.of(createTextInput(text)))
        );
    }

    private Map<String, Object> createListItemBlock(String text) {
        return Map.of(
                "object", "block",
                "type", "bulleted_list_item",
                "bulleted_list_item", Map.of("rich_text", List.of(createTextInput(text)))
        );
    }

    private Map<String, Object> createCodeBlock(String code, String language) {
        // 노션 API에서 지원하는 언어 이름으로 매핑
        if (language.equals("js")) language = "javascript";
        if (language.equals("yml")) language = "yaml";
        
        return Map.of(
                "object", "block",
                "type", "code",
                "code", Map.of(
                        "rich_text", List.of(createTextInput(code)),
                        "language", language
                )
        );
    }

    private Map<String, Object> createTextInput(String text) {
        return Map.of(
                "type", "text",
                "text", Map.of("content", text != null ? text : "")
        );
    }
}
