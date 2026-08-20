package com.javainterviewlab.scenario.repository.model;

/** 场景主记录投影。 */
public record ScenarioRow(
        Long id,
        String externalKey,
        String title,
        String summary,
        Integer starLevel,
        String originType,
        String sourceVersion
) {
}
