package com.javainterviewlab.scenario.dto;

/** 场景库列表项，不携带案例和方案大文本。 */
public record ScenarioSummaryResponse(
        Long id,
        String externalKey,
        String title,
        String summary,
        Integer starLevel,
        Long caseCount,
        Long attemptCount
) {
}
