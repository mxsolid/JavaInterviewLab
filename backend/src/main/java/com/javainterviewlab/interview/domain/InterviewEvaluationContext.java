package com.javainterviewlab.interview.domain;

/** 面试评估器输入；Provider 只依赖该稳定上下文。 */
public record InterviewEvaluationContext(
        Long sessionId,
        Integer sequenceNo,
        String prompt,
        String answerText,
        String rubricText
) {
}
