package com.javainterviewlab.study.dashboard.dto;

import com.javainterviewlab.study.plan.dto.CurrentPlanResponse;

import java.util.List;

/** 首页真实学习统计响应；时间进度和学习完成数量刻意分开，防止日期推进被误解为已经学完。 */
public record DashboardResponse(
        CurrentPlanResponse currentPlan,
        Integer timeProgressDay,
        Integer planDurationDays,
        Integer todayPlanItemCount,
        Long todayReviewCount,
        Long dueReviewCount,
        Long totalQuestionCount,
        Long touchedQuestionCount,
        Long solidQuestionCount,
        Long masteredQuestionCount,
        Double fiveStarMasteryRate,
        Long activeWrongQuestionCount,
        Long favoriteQuestionCount,
        List<RecentStudyItemResponse> recentStudyItems
) {
}
