package com.javainterviewlab.interview.repository.model;

import java.time.Instant;
import java.util.UUID;

/** 已持久化的单轮面试评分。 */
public record InterviewTurnRow(
        Long id,
        Long sessionId,
        UUID clientTurnId,
        Integer sequenceNo,
        String prompt,
        Double score,
        String feedback,
        Double accuracyScore,
        Double completenessScore,
        Double depthScore,
        Double expressionScore,
        Instant createdAt
) {
}
