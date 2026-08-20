package com.javainterviewlab.study.plan.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证自然日进度只依赖注入时钟，避免测试随运行当天变化。 */
class StudyPlanTimeProgressCalculatorTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Clock DAY_TWO_CLOCK = Clock.fixed(Instant.parse("2026-08-20T01:00:00Z"), ZONE);

    /** 路线开始当天始终显示 Day 1。 */
    @Test
    void shouldReturnDayOneOnStartDate() {
        assertThat(StudyPlanTimeProgressCalculator.calculate(
                Instant.parse("2026-08-20T00:00:00Z"), 15, DAY_TWO_CLOCK
        )).isEqualTo(1);
    }

    /** 跨过本地自然日后进入 Day 2。 */
    @Test
    void shouldAdvanceOnNextLocalDate() {
        assertThat(StudyPlanTimeProgressCalculator.calculate(
                Instant.parse("2026-08-19T02:00:00Z"), 15, DAY_TWO_CLOCK
        )).isEqualTo(2);
    }

    /** 超出路线时长或开始时间在未来时，不得展示非法 Day 0 或超出路线的天数。 */
    @Test
    void shouldCapDurationAndDefendFutureStartTime() {
        assertThat(StudyPlanTimeProgressCalculator.calculate(
                Instant.parse("2026-07-01T00:00:00Z"), 15, DAY_TWO_CLOCK
        )).isEqualTo(15);
        assertThat(StudyPlanTimeProgressCalculator.calculate(
                Instant.parse("2026-08-21T00:00:00Z"), 15, DAY_TWO_CLOCK
        )).isEqualTo(1);
    }
}
