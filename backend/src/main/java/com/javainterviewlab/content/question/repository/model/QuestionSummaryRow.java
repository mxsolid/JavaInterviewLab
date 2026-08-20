package com.javainterviewlab.content.question.repository.model;

/**
 * 题目列表的只读 JOIN 投影。
 *
 * <p>列表刻意不查询答案正文，避免题库翻页时传输和映射无关的大字段。</p>
 */
public record QuestionSummaryRow(
        Long id,
        Long topicId,
        String topicName,
        Long categoryId,
        String categoryName,
        String title,
        Integer starLevel,
        String difficulty,
        String frequencyLevel,
        String status,
        String oneLiner,
        Long version
) {
}
