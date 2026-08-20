package com.javainterviewlab.study.plan.dto;

/** 当前路线的当日任务；未选择路线时接口返回 null。 */
public record TodayStudyResponse(
        CurrentPlanResponse currentPlan,
        StudyPlanDayResponse day
) {
}
