package com.javainterviewlab.study.attempt.dto;

import java.time.Instant;
import java.util.UUID;

/** 答题历史响应，返回实际保存或幂等命中的同一条事实记录。 */
public record QuestionAttemptResponse(
        Long id,
        Long questionId,
        UUID clientAttemptId,
        boolean viewedAnswer,
        Integer selfRating,
        String resultType,
        Long elapsedMs,
        Instant createdAt
) {
}
