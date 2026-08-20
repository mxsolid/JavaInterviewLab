package com.javainterviewlab.scenario.dto;

import java.time.Instant;
import java.util.UUID;

/** 已追加保存的场景答题历史。 */
public record ScenarioAttemptResponse(
        Long id,
        UUID clientAttemptId,
        Long scenarioId,
        Long caseId,
        Integer selfRating,
        String resultType,
        String resultDescription,
        Integer durationSeconds,
        Instant createdAt,
        boolean duplicated
) {
}
