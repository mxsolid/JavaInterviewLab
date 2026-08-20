package com.javainterviewlab.interview.repository.model;

/** 当前会话题目及本地规则评分依据。 */
public record InterviewQuestionRow(
        Long id,
        String title,
        String topicCode,
        String rubricText
) {
}
