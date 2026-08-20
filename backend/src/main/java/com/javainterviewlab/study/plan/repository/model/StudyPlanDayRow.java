package com.javainterviewlab.study.plan.repository.model;

/** 学习路线中单日计划的只读投影，关联项由 Service 另行组装。 */
public record StudyPlanDayRow(Long id, Integer dayNumber, String title, String description) {
}
