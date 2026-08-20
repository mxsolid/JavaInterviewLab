package com.javainterviewlab.study.review.repository.model;

import java.time.Instant;

/** 复习列表题目摘要投影，避免读取题目详情的大字段。 */
public record ReviewTaskRow(Long id, Long questionId, String title, Integer starLevel, Instant dueAt, String status) {
}
