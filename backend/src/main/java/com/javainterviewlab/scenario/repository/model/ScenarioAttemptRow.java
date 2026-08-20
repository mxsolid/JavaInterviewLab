package com.javainterviewlab.scenario.repository.model;

import java.time.Instant;
import java.util.UUID;

/** 场景答题历史数据库投影。 */
public record ScenarioAttemptRow(
        Long id,
        Long profileId,
        Long scenarioId,
        Long caseId,
        UUID clientAttemptId,
        Integer selfRating,
        String resultType,
        Integer durationSeconds,
        Instant createdAt
) {
}
