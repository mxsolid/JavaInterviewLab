package com.javainterviewlab.study.plan.dto;

import java.time.Instant;

/** 当前选中的路线；timeProgressDay 只表示自然时间，不表示学习内容已经完成。 */
public record CurrentPlanResponse(
        Long planId,
        String planCode,
        String planName,
        Integer durationDays,
        Instant startedAt,
        Integer timeProgressDay
) {
}
