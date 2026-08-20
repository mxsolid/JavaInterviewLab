package com.javainterviewlab.scenario.repository.model;

/** 场景列表数据库投影。 */
public record ScenarioSummaryRow(
        Long id,
        String externalKey,
        String title,
        String summary,
        Integer starLevel,
        Long caseCount,
        Long attemptCount
) {
}
