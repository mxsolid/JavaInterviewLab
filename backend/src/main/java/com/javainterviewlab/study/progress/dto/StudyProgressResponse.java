package com.javainterviewlab.study.progress.dto;

import java.time.Instant;

/** 当前学习进度响应，不替代 append-only 答题历史。 */
public record StudyProgressResponse(
        Long questionId,
        String stage,
        String stageDescription,
        String masteryLevel,
        String masteryDescription,
        Integer attemptCount,
        Integer wrongCount,
        boolean wrongBookActive,
        Instant lastStudiedAt,
        Long version
) {
}
