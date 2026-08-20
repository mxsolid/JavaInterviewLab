package com.javainterviewlab.study.plan.dto;

import java.util.List;

/** 一天的学习主题和其关联的题目或专题。 */
public record StudyPlanDayResponse(
        Long id,
        Integer dayNumber,
        String title,
        String description,
        List<StudyPlanItemResponse> items
) {
}
