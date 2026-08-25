package com.mcp.mcp_pilot.knowledge.application.chunker;

import com.mcp.mcp_pilot.ai.vector.dto.RawChunk;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MarkdownChunker {

    // Spring AI 2.0.0+ 빌더 사용 (500 토큰 단위)
    private final TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
            .withChunkSize(500)
            .withKeepSeparator(true)
            .build();

    // 마크다운 헤딩 (#, ##, ###) 정규식
    private static final Pattern HEADING_PATTERN = Pattern.compile("(?m)^(#{1,3})\\s+(.+)$");

    public List<RawChunk> chunk(String docTitle, String markdownContent) {
        if (markdownContent == null || markdownContent.isBlank()) {
            return List.of();
        }

        List<RawChunk> result = new ArrayList<>();
        Matcher matcher = HEADING_PATTERN.matcher(markdownContent);

        List<Integer> headingIndices = new ArrayList<>();
        List<String> headings = new ArrayList<>();

        while (matcher.find()) {
            headingIndices.add(matcher.start());
            headings.add(matcher.group(2).trim());
        }

        // 헤딩이 없는 일반 텍스트는 전체를 토큰 분할
        if (headingIndices.isEmpty()) {
            return splitByToken(docTitle, "본문", markdownContent, 0);
        }

        // 헤딩 이전의 서론(Intro)이 있는 경우 처리
        if (headingIndices.get(0) > 0) {
            String introContent = markdownContent.substring(0, headingIndices.get(0)).trim();
            if (!introContent.isBlank()) {
                result.addAll(splitByToken(docTitle, docTitle + " > 개요", introContent, 0));
            }
        }

        // 헤딩 단위로 섹션을 잘라서 청킹
        int chunkIndex = result.size();
        for (int i = 0; i < headingIndices.size(); i++) {
            int start = headingIndices.get(i);
            int end = (i + 1 < headingIndices.size()) ? headingIndices.get(i + 1) : markdownContent.length();
            String sectionContent = markdownContent.substring(start, end).trim();
            String headingPath = docTitle + " > " + headings.get(i);

            List<RawChunk> sectionChunks = splitByToken(docTitle, headingPath, sectionContent, chunkIndex);
            result.addAll(sectionChunks);
            chunkIndex += sectionChunks.size();
        }

        return result;
    }

    private List<RawChunk> splitByToken(String title, String headingPath, String content, int startIndex) {
        Document doc = new Document(content, Map.of(
                "title", title,
                "headingPath", headingPath
        ));

        // 토큰 크기가 500 이하이면 1개로 유지되고, 500을 넘으면 2차로 쪼개짐
        List<Document> splitDocs = tokenTextSplitter.apply(List.of(doc));
        List<RawChunk> chunks = new ArrayList<>();

        for (int i = 0; i < splitDocs.size(); i++) {
            Document d = splitDocs.get(i);
            Map<String, Object> meta = new HashMap<>(d.getMetadata());
            meta.put("headingPath", headingPath);

            chunks.add(RawChunk.of(startIndex + i, d.getText(), meta));
        }

        return chunks;
    }
}