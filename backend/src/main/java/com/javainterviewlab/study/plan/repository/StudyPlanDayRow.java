package com.javainterviewlab.study.plan.repository;

/** Mapper 的扁平查询行；Service 读取关联项后再组装 API 响应。 */
public record StudyPlanDayRow(
        Long id,
        Integer dayNumber,
        String title,
        String description
) {
}
