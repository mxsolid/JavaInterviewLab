package com.javainterviewlab.study.plan.dto;

import java.util.List;

/** 学习路线详情，由路线元信息和按天排序的内容组成。 */
public record StudyPlanDetailResponse(
        Long id,
        String code,
        String name,
        Integer durationDays,
        String description,
        List<StudyPlanDayResponse> days
) {
}
