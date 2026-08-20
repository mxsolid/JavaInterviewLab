package com.javainterviewlab.study.dashboard.dto;

import java.time.Instant;

/** Dashboard 最近学习项目响应。 */
public record RecentStudyItemResponse(
        Long questionId,
        String title,
        Integer starLevel,
        String masteryLevel,
        Instant lastStudiedAt
) {
}
