package com.javainterviewlab.study.dashboard.repository.model;

import java.time.Instant;

/** 最近学习项目摘要投影。 */
public record RecentStudyRow(Long questionId, String title, Integer starLevel, String masteryLevel, Instant lastStudiedAt) {
}
