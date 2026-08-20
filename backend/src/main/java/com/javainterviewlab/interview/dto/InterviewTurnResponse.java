package com.javainterviewlab.interview.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** 单轮规则评分与下一问。 */
public record InterviewTurnResponse(
        Long id,
        UUID clientTurnId,
        Integer sequenceNo,
        String prompt,
        double totalScore,
        List<InterviewDimensionResponse> dimensions,
        String nextPrompt,
        boolean duplicated,
        Instant createdAt
) {
}
