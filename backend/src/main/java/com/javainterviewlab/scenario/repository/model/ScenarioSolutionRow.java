package com.javainterviewlab.scenario.repository.model;

/** 场景候选方案数据库投影。 */
public record ScenarioSolutionRow(
        Long id,
        Long scenarioId,
        String code,
        String name,
        String principle,
        String pros,
        String cons,
        String boundary,
        Integer sortOrder
) {
}
