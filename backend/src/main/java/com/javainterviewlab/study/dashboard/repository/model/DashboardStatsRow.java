package com.javainterviewlab.study.dashboard.repository.model;

/** Dashboard 总体统计投影；聚合留在 PostgreSQL，避免加载全部题目到 Java 再计数。 */
public record DashboardStatsRow(
        Long totalQuestionCount,
        Long touchedQuestionCount,
        Long solidQuestionCount,
        Long masteredQuestionCount,
        Long totalFiveStarQuestionCount,
        Long solidFiveStarQuestionCount,
        Long activeWrongQuestionCount,
        Long favoriteQuestionCount
) {
}
