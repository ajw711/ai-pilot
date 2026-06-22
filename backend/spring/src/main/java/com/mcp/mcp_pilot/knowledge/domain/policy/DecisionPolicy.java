package com.mcp.mcp_pilot.knowledge.domain.policy;

import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.domain.vo.Severity;
import com.mcp.mcp_pilot.knowledge.domain.vo.VerificationReport;

/**
 * 검증 결과 리포트를 기반으로 지식의 점수와 저장 상태(Status)를 결정하는 비즈니스 정책 클래스
 */
public class DecisionPolicy {

    public static KnowledgeStatus decide(VerificationReport report) {
        // AI 검증 결과 심각한 오류가 있든 없든, 사용자가 에디터에서 최종 확인하도록 REVIEW_READY 상태로 설정
        return KnowledgeStatus.REVIEW_READY;
    }

    public static int calculateScore(VerificationReport report) {
        if (report == null || report.issues() == null) {
            return 100;
        }

        int score = 100;
        for (var issue : report.issues()) {
            if (issue.severity() == Severity.CRITICAL) {
                score -= 40;
            } else if (issue.severity() == Severity.WARNING) {
                score -= 10;
            }
        }
        return Math.max(0, score);
    }
}
