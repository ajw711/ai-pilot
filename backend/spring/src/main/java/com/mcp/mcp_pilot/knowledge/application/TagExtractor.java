package com.mcp.mcp_pilot.knowledge.application;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TagExtractor {

    /**
     * 포맷 완료된 텍스트 본문에서 #키워드 패턴의 단어를 파싱
     */
    public List<String> extractTags(String aiResponse) {
        List<String> tags = new ArrayList<>();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(aiResponse);

        while (matcher.find()) {
            tags.add(matcher.group(1));
        }
        return tags;
    }
}
