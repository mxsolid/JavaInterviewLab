package com.javainterviewlab.study.plan.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 学习路线自然时间进度算法。
 *
 * <p>时间进度只反映路线开始后的自然日，不把停学或完成度混入计算；学习完成情况由题目掌握度快照单独表示。</p>
 */
public final class StudyPlanTimeProgressCalculator {

    private StudyPlanTimeProgressCalculator() {
    }

    /**
     * 计算当前应展示的 Day N。
     *
     * <p>开始时间晚于当前时仍返回 Day 1，避免时钟校准或跨时区数据导致出现 Day 0；超过路线时长时封顶。</p>
     */
    public static int calculate(Instant startedAt, int durationDays, Clock clock) {
        LocalDate startedDate = startedAt.atZone(clock.getZone()).toLocalDate();
        LocalDate today = LocalDate.now(clock);
        long elapsedDays = Math.max(0, ChronoUnit.DAYS.between(startedDate, today));
        return (int) Math.min(durationDays, elapsedDays + 1);
    }
}
