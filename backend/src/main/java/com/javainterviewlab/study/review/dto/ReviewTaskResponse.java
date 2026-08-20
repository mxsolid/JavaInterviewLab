package com.javainterviewlab.study.review.dto;

import java.time.Instant;

/** 复习任务响应，题目正文按需通过题目详情接口读取。 */
public record ReviewTaskResponse(
        Long id,
        Long questionId,
        String title,
        Integer starLevel,
        Instant dueAt,
        String status,
        boolean overdue
) {
}
