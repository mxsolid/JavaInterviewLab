package com.javainterviewlab.study.review.domain;

import com.javainterviewlab.study.attempt.domain.AttemptResultType;
import com.javainterviewlab.study.progress.domain.MasteryLevel;
import com.javainterviewlab.study.review.config.ReviewIntervalProperties;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/** 固定 Clock 时刻下验证 V0.2 简单复习间隔，防止配置或规则改动漂移。 */
class SimpleIntervalReviewPolicyTest {

    private final SimpleIntervalReviewPolicy policy = new SimpleIntervalReviewPolicy(new ReviewIntervalProperties());
    private final Instant now = Instant.parse("2026-08-20T00:00:00Z");

    @Test
    void shouldUseConfiguredIntervalsForMasteryLevels() {
        assertThat(policy.calculateNextReviewTime(MasteryLevel.UNKNOWN, AttemptResultType.NOT_ANSWERED, now)).isEqualTo(now.plusSeconds(86_400));
        assertThat(policy.calculateNextReviewTime(MasteryLevel.SEEN, AttemptResultType.CORRECT, now)).isEqualTo(now.plusSeconds(3 * 86_400));
        assertThat(policy.calculateNextReviewTime(MasteryLevel.BASIC, AttemptResultType.CORRECT, now)).isEqualTo(now.plusSeconds(7 * 86_400));
        assertThat(policy.calculateNextReviewTime(MasteryLevel.SOLID, AttemptResultType.CORRECT, now)).isEqualTo(now.plusSeconds(14 * 86_400));
        assertThat(policy.calculateNextReviewTime(MasteryLevel.MASTERED, AttemptResultType.CORRECT, now)).isEqualTo(now.plusSeconds(30 * 86_400));
    }

    @Test
    void shouldPrioritizeOneDayIntervalForWrongAnswer() {
        assertThat(policy.calculateNextReviewTime(MasteryLevel.MASTERED, AttemptResultType.WRONG, now))
                .isEqualTo(now.plusSeconds(86_400));
    }
}
