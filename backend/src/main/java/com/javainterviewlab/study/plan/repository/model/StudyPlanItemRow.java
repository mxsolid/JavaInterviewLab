package com.javainterviewlab.study.plan.repository.model;

import com.javainterviewlab.study.plan.domain.StudyPlanTargetType;

/** 学习路线项及其已解析目标标题的只读 JOIN 投影。 */
public record StudyPlanItemRow(
        Long id,
        StudyPlanTargetType targetType,
        Long targetId,
        String targetTitle,
        Integer sortOrder
) {
}
