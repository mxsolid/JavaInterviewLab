package com.javainterviewlab.study.plan.dto;

/** 每日学习项统一保留目标类型，为 V0.3 接入场景学习项预留同一展示契约。 */
public record StudyPlanItemResponse(
        Long id,
        String targetType,
        Long targetId,
        String targetTitle,
        Integer sortOrder
) {
}
