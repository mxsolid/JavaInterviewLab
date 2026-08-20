package com.javainterviewlab.interview.dto;

import java.time.Instant;

/** 新建面试会话响应；首题来自数据库启用题库。 */
public record InterviewSessionResponse(
        Long id,
        String mode,
        String topicCode,
        String status,
        Long questionId,
        Integer sequenceNo,
        String prompt,
        String provider,
        boolean providerEnabled,
        Instant startedAt
) {
}
