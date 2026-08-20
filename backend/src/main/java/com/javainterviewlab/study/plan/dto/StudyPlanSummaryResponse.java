package com.javainterviewlab.study.plan.dto;

/** 学习路线列表只返回选择路线所需的摘要，避免首页预先读取全部每日任务。 */
public record StudyPlanSummaryResponse(
        Long id,
        String code,
        String name,
        Integer durationDays,
        String description
) {
}
