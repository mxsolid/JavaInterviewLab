package com.javainterviewlab.interview.repository.model;

import java.time.Instant;

/** 面试会话持久化行。 */
public record InterviewSessionRow(
        Long id,
        Long profileId,
        Long questionId,
        String mode,
        String topicCode,
        String status,
        Double totalScore,
        Instant startedAt,
        Instant finishedAt
) {
}
