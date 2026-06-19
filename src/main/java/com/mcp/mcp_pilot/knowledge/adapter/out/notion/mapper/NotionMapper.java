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

    public NotionPageRequest toRequest(KnowledgeLog log, String pageId) {
        // Properties (페이지 제목 매핑)
        Map<String, Object> properties = Map.of(
                "title", Map.of(
                        "title", createRichTextList(log.getTitle())
                )
        );

        // 본문(Children) 생성 - Markdown-ish 요약본을 블록으로 변환
        List<Map<String, Object>> children = parseContentToBlocks(log.getFormattedContent());

        return new NotionPageRequest(
                new Parent(pageId),
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
            // 일반 텍스트 처리
            else if (!trimmedLine.isEmpty()) {
                blocks.add(createParagraphBlock(line));
            }
        }

        blocks.add(Map.of("object", "block", "type", "divider", "divider", Map.of()));
        return blocks;
    }

    private Map<String, Object> createHeadingBlock(int level, String text) {
        String type = "heading_" + level;
        return Map.of(
                "object", "block",
                "type", type,
                type, Map.of("rich_text", createRichTextList(text))
        );
    }

    private Map<String, Object> createParagraphBlock(String text) {
        return Map.of(
                "object", "block",
                "type", "paragraph",
                "paragraph", Map.of("rich_text", createRichTextList(text))
        );
    }

    private Map<String, Object> createListItemBlock(String text) {
        return Map.of(
                "object", "block",
                "type", "bulleted_list_item",
                "bulleted_list_item", Map.of("rich_text", createRichTextList(text))
        );
    }

    private Map<String, Object> createCodeBlock(String code, String language) {
        if (language.equals("js")) language = "javascript";
        if (language.equals("yml")) language = "yaml";
        
        return Map.of(
                "object", "block",
                "type", "code",
                "code", Map.of(
                        "rich_text", List.of(createPlainRichTextItem(code)),
                        "language", language
                )
        );
    }

    private List<Map<String, Object>> createRichTextList(String text) {
        List<Map<String, Object>> richTextList = new ArrayList<>();
        if (text == null || text.isBlank()) return richTextList;

        // **bold** 및 [L1] 인용 처리 정규식
        // 1번 그룹: 굵은 글씨 (**text**)
        // 2번 그룹: 인용 표시 ([L1])
        Pattern pattern = Pattern.compile("\\*\\*(.*?)\\*\\*|\\[(L\\d+)\\]");
        Matcher matcher = pattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            // 일치하는 부분 이전의 일반 텍스트
            if (matcher.start() > lastEnd) {
                richTextList.add(createPlainRichTextItem(text.substring(lastEnd, matcher.start())));
            }

            if (matcher.group(1) != null) {
                // **굵은 글씨** 처리
                richTextList.add(createStyledRichTextItem(matcher.group(1), true, false, "default"));
            } else if (matcher.group(2) != null) {
                // [L1] 인용 표시 처리 (회색, 이탤릭으로 차별화)
                richTextList.add(createStyledRichTextItem("[" + matcher.group(2) + "]", false, true, "gray"));
            }
            
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            richTextList.add(createPlainRichTextItem(text.substring(lastEnd)));
        }

        return richTextList;
    }

    private Map<String, Object> createPlainRichTextItem(String content) {
        return createStyledRichTextItem(content, false, false, "default");
    }

    private Map<String, Object> createStyledRichTextItem(String content, boolean bold, boolean italic, String color) {
        return Map.of(
                "type", "text",
                "text", Map.of("content", content != null ? content : ""),
                "annotations", Map.of(
                        "bold", bold,
                        "italic", italic,
                        "strikethrough", false,
                        "underline", false,
                        "code", false,
                        "color", color
                )
        );
    }
}

