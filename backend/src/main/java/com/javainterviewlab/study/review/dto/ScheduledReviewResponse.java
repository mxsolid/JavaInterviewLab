package com.javainterviewlab.study.review.dto;

import java.time.Instant;

/** 答题提交后返回的当前待复习任务，不承担题目列表展示字段。 */
public record ScheduledReviewResponse(
        Long id,
        Long questionId,
        Instant dueAt,
        String status,
        String statusDescription
) {
}
