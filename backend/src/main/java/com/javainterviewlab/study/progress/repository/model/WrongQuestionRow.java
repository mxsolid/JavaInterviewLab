package com.javainterviewlab.study.progress.repository.model;

import java.time.Instant;

/** 错题列表所需的题目摘要与进度投影，不包含题目正文。 */
public record WrongQuestionRow(
        Long questionId,
        String title,
        Integer starLevel,
        String masteryLevel,
        Integer attemptCount,
        Integer wrongCount,
        Instant lastStudiedAt
) {
}
