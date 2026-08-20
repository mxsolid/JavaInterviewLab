package com.javainterviewlab.study.plan.repository.model;

/** 学习路线列表的只读投影，避免 Repository 返回接口响应对象。 */
public record StudyPlanRow(Long id, String code, String name, Integer durationDays, String description) {
}
