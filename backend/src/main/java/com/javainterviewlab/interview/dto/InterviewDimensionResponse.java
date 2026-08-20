package com.javainterviewlab.interview.dto;

/** 可解释的单个评分维度。 */
public record InterviewDimensionResponse(
        String code,
        String label,
        double score,
        double maxScore,
        String reason
) {
}
