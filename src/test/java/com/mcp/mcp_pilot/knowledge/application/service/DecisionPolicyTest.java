package com.mcp.mcp_pilot.knowledge.application.service;

import com.mcp.mcp_pilot.knowledge.domain.policy.DecisionPolicy;
import com.mcp.mcp_pilot.knowledge.domain.vo.Issue;
import com.mcp.mcp_pilot.knowledge.domain.vo.KnowledgeStatus;
import com.mcp.mcp_pilot.knowledge.domain.vo.Severity;
import com.mcp.mcp_pilot.knowledge.domain.vo.VerificationReport;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class DecisionPolicyTest {

    @Test
    @DisplayName("이슈가 없는 리포트인 경우 점수는 100점")
    void calculateScoreReturns100() {
        // Given
        VerificationReport report = VerificationReport.empty();

        // When
        int score = DecisionPolicy.calculateScore(report);

        // Then
        assertThat(score).isEqualTo(100);
    }

    @Test
    @DisplayName("WARNING 이슈는 건당 10점씩 감점")
    void calculateScoreWaring() {
        // Given
        List<Issue> list = List.of(
                new Issue(Severity.WARNING, "대상1", "경고1"),
                new Issue(Severity.WARNING, "대상2", "경고2")
        );
        VerificationReport report = new VerificationReport(list);

        // When
        int score = DecisionPolicy.calculateScore(report);

        // Then
        assertThat(score).isEqualTo(80);
    }

    @Test
    @DisplayName("CRITICAL 이슈는 건당 40점씩")
    void calculateScoreCriticalIssues() {
        // Given
        List<Issue> list = List.of(
                new Issue(Severity.CRITICAL, "대상1", "치명적1"),
                new Issue(Severity.CRITICAL, "대상2", "치명적2")
        );
        VerificationReport report = new VerificationReport(list);

        // When
        int score = DecisionPolicy.calculateScore(report);

        // Then
        assertThat(score).isEqualTo(20);
    }

    @Test
    @DisplayName("WARNING과 CRITICAL 이슈가 혼합된 경우 각각의 감점 폭이 합산되어야 한다")
    void calculateScore_MixedIssues_Deductions() {
        // Given
        VerificationReport report = new VerificationReport(List.of(
                new Issue(Severity.WARNING, "대상1", "경고"),
                new Issue(Severity.CRITICAL, "대상2", "심각오류")
        ));

        // When
        int score = DecisionPolicy.calculateScore(report);

        // Then
        assertThat(score).isEqualTo(50);
    }

    @Test
    @DisplayName("점수의 하한선은 0점이어야 하며 음수가 될 수 없다")
    void calculateScoreMinScoreIsZero() {
        // Given
        VerificationReport report = new VerificationReport(List.of(
                new Issue(Severity.CRITICAL, "대상1", "심각오류1"),
                new Issue(Severity.CRITICAL, "대상2", "심각오류2"),
                new Issue(Severity.CRITICAL, "대상3", "심각오류3")
        ));

        // When
        int score = DecisionPolicy.calculateScore(report);

        // Then
        assertThat(score).isEqualTo(0);
    }

    @Test
    @DisplayName("어떤 검증 리포트이든 최종 지식 상태는 수동 검토 대기(REVIEW_READY)여야 한다")
    void decideAlwaysReturnsReviewReady() {
        // Given
        VerificationReport report = new VerificationReport(List.of(
                new Issue(Severity.CRITICAL, "대상1", "심각오류1")
        ));

        // When
        KnowledgeStatus status = DecisionPolicy.decide(report);

        // Then
        assertThat(status).isEqualTo(KnowledgeStatus.REVIEW_READY);
    }

}
