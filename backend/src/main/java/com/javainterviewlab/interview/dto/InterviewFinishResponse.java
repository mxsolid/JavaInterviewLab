package com.javainterviewlab.interview.dto;

import java.time.Instant;
import java.util.List;

/** 会话结束后的多轮平均分。 */
public record InterviewFinishResponse(
        Long sessionId,
        String status,
        double totalScore,
        List<InterviewDimensionResponse> dimensions,
        int turnCount,
        String summary,
        Instant finishedAt
) {
}
