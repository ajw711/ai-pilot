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

    public NotionPageRequest toRequest(KnowledgeLog log, String databaseId) {
        // Properties (페이지 제목 매핑)
        Map<String, Object> properties = Map.of(
                "title", Map.of(
                        "title", createRichTextList(log.getTitle())
                )
        );

        // 본문(Children) 생성 - Markdown-ish 요약본을 블록으로 변환
        List<Map<String, Object>> children = parseContentToBlocks(log.getFormattedContent());

        return new NotionPageRequest(
                new Parent(databaseId),
                properties,
                children
        );
    }

    private List<Map<String, Object>> parseContentToBlocks(String content) {
        List<NotionBlock> rootBlocks = new ArrayList<>();
        List<NotionBlock> parentStack = new ArrayList<>();

        if (content == null || content.isBlank()) {
            return List.of();
        }

        String[] lines = content.split("\\r?\\n");
        StringBuilder codeBuffer = new StringBuilder();
        boolean inCodeBlock = false;
        String codeLanguage = "plain text";

        for (String line : lines) {
            // Notion API block count limit protection (max 100 blocks, cap at 97 to leave room)
            if (rootBlocks.size() >= 97) {
                rootBlocks.add(new NotionBlock("paragraph", Map.of("rich_text", createRichTextList("... (Notion API 블록 개수 제한으로 인해 본문이 생략되었습니다. 전체 내용은 데이터베이스를 확인해 주세요.)")), 0));
                break;
            }

            String trimmedLine = line.trim();

            // 코드 블록 처리
            if (trimmedLine.startsWith("```")) {
                if (inCodeBlock) {
                    rootBlocks.add(createCodeNotionBlock(codeBuffer.toString(), codeLanguage));
                    codeBuffer.setLength(0);
                    inCodeBlock = false;
                } else {
                    inCodeBlock = true;
                    // 언어명 뒤의 공백 제거 및 trim 수행
                    codeLanguage = trimmedLine.length() > 3 ? trimmedLine.substring(3).trim().toLowerCase() : "java";
                }
                parentStack.clear();
                continue;
            }

            if (inCodeBlock) {
                codeBuffer.append(line).append("\n");
                continue;
            }

            // 이미지 처리
            if (trimmedLine.startsWith("![") && trimmedLine.endsWith(")")) {
                int altCloseIdx = trimmedLine.indexOf("](");
                if (altCloseIdx > 2) {
                    String imageUrl = trimmedLine.substring(altCloseIdx + 2, trimmedLine.length() - 1);
                    Map<String, Object> imageDetails = Map.of(
                            "type", "external",
                            "external", Map.of("url", imageUrl)
                    );
                    rootBlocks.add(new NotionBlock("image", imageDetails, 0));
                    parentStack.clear();
                    continue;
                }
            }

            // 헤더 처리
            if (trimmedLine.startsWith("### ")) {
                rootBlocks.add(new NotionBlock("heading_3", Map.of("rich_text", createRichTextList(trimmedLine.substring(4))), 0));
                parentStack.clear();
            } else if (trimmedLine.startsWith("## ")) {
                rootBlocks.add(new NotionBlock("heading_2", Map.of("rich_text", createRichTextList(trimmedLine.substring(3))), 0));
                parentStack.clear();
            } else if (trimmedLine.startsWith("# ")) {
                rootBlocks.add(new NotionBlock("heading_1", Map.of("rich_text", createRichTextList(trimmedLine.substring(2))), 0));
                parentStack.clear();
            } 
            // 리스트 처리
            else if (trimmedLine.startsWith("- ") || trimmedLine.startsWith("* ")) {
                int leadingSpaces = countLeadingSpaces(line);
                String itemText = trimmedLine.substring(2);
                NotionBlock newBlock = new NotionBlock("bulleted_list_item", Map.of("rich_text", createRichTextList(itemText)), leadingSpaces);

                NotionBlock parent = null;
                while (!parentStack.isEmpty()) {
                    NotionBlock top = parentStack.get(parentStack.size() - 1);
                    if (top.indentLevel < leadingSpaces) {
                        parent = top;
                        break;
                    } else {
                        parentStack.remove(parentStack.size() - 1);
                    }
                }

                if (parent != null) {
                    parent.children.add(newBlock);
                } else {
                    rootBlocks.add(newBlock);
                }
                parentStack.add(newBlock);
            }
            // 일반 텍스트 처리
            else if (!trimmedLine.isEmpty()) {
                rootBlocks.add(new NotionBlock("paragraph", Map.of("rich_text", createRichTextList(line)), 0));
                parentStack.clear();
            }
        }

        // if code block was not closed by LLM, close it automatically to prevent missing content
        if (inCodeBlock && codeBuffer.length() > 0 && rootBlocks.size() < 98) {
            rootBlocks.add(createCodeNotionBlock(codeBuffer.toString(), codeLanguage));
        }

        rootBlocks.add(new NotionBlock("divider", Map.of(), 0));

        // Convert to nested list of maps
        List<Map<String, Object>> result = new ArrayList<>();
        for (NotionBlock root : rootBlocks) {
            result.add(root.toMap());
        }
        return result;
    }

    private int countLeadingSpaces(String line) {
        int count = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ') {
                count++;
            } else if (c == '\t') {
                count += 4;
            } else {
                break;
            }
        }
        return count;
    }

    private NotionBlock createCodeNotionBlock(String code, String language) {
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
        
        return new NotionBlock("code", Map.of(
                "rich_text", richTextList,
                "language", sanitizedLanguage
        ), 0);
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

    private static class NotionBlock {
        private final String type;
        private final Map<String, Object> details;
        private final List<NotionBlock> children = new ArrayList<>();
        private final int indentLevel;

        public NotionBlock(String type, Map<String, Object> details, int indentLevel) {
            this.type = type;
            this.details = details;
            this.indentLevel = indentLevel;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("object", "block");
            map.put("type", type);
            
            // Create a mutable copy of the details map
            Map<String, Object> detailsCopy = new java.util.HashMap<>(details);
            
            if (!children.isEmpty()) {
                List<Map<String, Object>> childMaps = new ArrayList<>();
                for (NotionBlock child : children) {
                    childMaps.add(child.toMap());
                }
                // Add children inside the block details instead of root level
                detailsCopy.put("children", childMaps);
            }
            
            map.put(type, detailsCopy);
            return map;
        }
    }
}

