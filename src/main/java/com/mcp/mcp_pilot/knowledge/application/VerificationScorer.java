package com.mcp.mcp_pilot.knowledge.application;

import com.mcp.mcp_pilot.knowledge.domain.vo.VerificationResponse;
import org.springframework.stereotype.Component;

@Component
public class VerificationScorer {

    /**
     * 객체(원소) 하나가 곧 오류 1개
     * 사실 오류 ( FactIssue ): -20점 (지식으로서 가치가 훼손되는 가장 치명적인 오류이므로 가장 큰 감점)
     * 근거 부족 ( UnsupportedClaim ): -10점 (기술 문서의 신뢰도를 떨어뜨리므로 중간 수준 감점)
     * 모호한 표현 ( AmbiguousExpression ): -5점 (문장의 명확성 문제이므로 가벼운 수준 감점)
     * @return 점수
     */
    public int calculate(VerificationResponse verifyResponse) {
        int score = 100;
        if (verifyResponse != null) {
            if (verifyResponse.factIssues() != null) {
                score -= verifyResponse.factIssues().size() * 20;
            }
            if (verifyResponse.ambiguities() != null) {
                score -= verifyResponse.ambiguities().size() * 5;
            }
            if (verifyResponse.unsupportedClaims() != null) {
                score -= verifyResponse.unsupportedClaims().size() * 10;
            }
        }
        // 음수 방지
        return Math.max(0, score);
    }
}
