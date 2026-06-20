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

    private static final List<String> NOTION_LANGUAGES = List.of(
            "abap", "arduino", "bash", "basic", "c", "clojure", "coffeescript", "c++", "c#", "css", "dart", "diff", 
            "docker", "elixir", "elm", "erlang", "flow", "fortran", "f#", "gherkin", "glsl", "go", "graphql", 
            "groovy", "haskell", "html", "java", "javascript", "json", "julia", "kotlin", "latex", "less", 
            "lisp", "livescript", "lua", "makefile", "markdown", "markup", "matlab", "nix", "objective-c", 
            "ocaml", "pascal", "perl", "php", "plain text", "powershell", "prolog", "protobuf", "python", 
            "r", "reason", "ruby", "rust", "sass", "scala", "scheme", "scss", "shell", "sql", "swift", 
            "typescript", "vb.net", "verilog", "vhdl", "visual basic", "webassembly", "xml", "yaml"
    );

    private String sanitizeLanguage(String lang) {
        if (lang == null) {
            return "plain text";
        }
        lang = lang.trim().toLowerCase();
        if (lang.equals("js")) return "javascript";
        if (lang.equals("ts")) return "typescript";
        if (lang.equals("yml")) return "yaml";
        if (lang.equals("sh")) return "shell";
        if (lang.equals("dockerfile")) return "docker";
        if (lang.equals("md")) return "markdown";
        
        if (NOTION_LANGUAGES.contains(lang)) {
            return lang;
        }
        return "plain text";
    }

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
            // Notion API block count limit protection (max 100 blocks, cap at 97 to leave room)
            if (blocks.size() >= 97) {
                blocks.add(createParagraphBlock("... (Notion API 블록 개수 제한으로 인해 본문이 생략되었습니다. 전체 내용은 데이터베이스를 확인해 주세요.)"));
                break;
            }

            String trimmedLine = line.trim();

            // 코드 블록 처리
            if (trimmedLine.startsWith("```")) {
                if (inCodeBlock) {
                    blocks.add(createCodeBlock(codeBuffer.toString(), codeLanguage));
                    codeBuffer.setLength(0);
                    inCodeBlock = false;
                } else {
                    inCodeBlock = true;
                    // 언어명 뒤의 공백 제거 및 trim 수행
                    codeLanguage = trimmedLine.length() > 3 ? trimmedLine.substring(3).trim().toLowerCase() : "java";
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

        // if code block was not closed by LLM, close it automatically to prevent missing content
        if (inCodeBlock && codeBuffer.length() > 0 && blocks.size() < 98) {
            blocks.add(createCodeBlock(codeBuffer.toString(), codeLanguage));
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
        String sanitizedLanguage = sanitizeLanguage(language);
        List<Map<String, Object>> richTextList = new ArrayList<>();
        
        if (code != null) {
            int length = code.length();
            int offset = 0;
            while (offset < length) {
                int end = Math.min(offset + 2000, length);
                richTextList.add(createPlainRichTextItem(code.substring(offset, end)));
                offset = end;
            }
        }
        
        return Map.of(
                "object", "block",
                "type", "code",
                "code", Map.of(
                        "rich_text", richTextList,
                        "language", sanitizedLanguage
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
                addChunkedStyledItems(richTextList, text.substring(lastEnd, matcher.start()), false, false, "default");
            }

            if (matcher.group(1) != null) {
                // **굵은 글씨** 처리
                addChunkedStyledItems(richTextList, matcher.group(1), true, false, "default");
            } else if (matcher.group(2) != null) {
                // [L1] 인용 표시 처리 (회색, 이탤릭으로 차별화)
                addChunkedStyledItems(richTextList, "[" + matcher.group(2) + "]", false, true, "gray");
            }
            
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) {
            addChunkedStyledItems(richTextList, text.substring(lastEnd), false, false, "default");
        }

        return richTextList;
    }

    private void addChunkedStyledItems(List<Map<String, Object>> richTextList, String content, boolean bold, boolean italic, String color) {
        if (content == null || content.isEmpty()) return;
        int length = content.length();
        int offset = 0;
        while (offset < length) {
            int end = Math.min(offset + 2000, length);
            richTextList.add(createStyledRichTextItem(content.substring(offset, end), bold, italic, color));
            offset = end;
        }
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

