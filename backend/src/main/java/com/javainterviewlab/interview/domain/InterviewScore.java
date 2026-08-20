package com.javainterviewlab.interview.domain;

/** 单轮面试评分结果；四维上限固定，保证总分口径稳定且可解释。 */
public record InterviewScore(
        double accuracy,
        double completeness,
        double depth,
        double expression,
        String accuracyReason,
        String completenessReason,
        String depthReason,
        String expressionReason
) {

    public double total() {
        return accuracy + completeness + depth + expression;
    }
}
