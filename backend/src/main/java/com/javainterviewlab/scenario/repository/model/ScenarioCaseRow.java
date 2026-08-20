package com.javainterviewlab.scenario.repository.model;

/** 场景案例数据库投影，expectedAnalysisJson 由 Service 解析。 */
public record ScenarioCaseRow(
        Long id,
        Long scenarioId,
        String code,
        String title,
        String rootCause,
        String prompt,
        String expectedAnalysisJson,
        Integer sortOrder
) {
}
