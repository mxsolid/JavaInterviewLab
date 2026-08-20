package com.javainterviewlab.study.plan.repository.model;

import java.time.Instant;

/** 当前激活路线的只读 JOIN 投影，开始时间用于计算自然日进度。 */
public record CurrentPlanRow(
        Long planId,
        String planCode,
        String planName,
        Integer durationDays,
        Instant startedAt
) {
}
