package com.javainterviewlab.content.question.repository.model;

/**
 * 题目主表及所属专题、分类的只读 JOIN 投影。
 *
 * <p>一对多答案、追问和标签由独立查询读取，避免 MyBatis 把聚合列表误当作单行列映射。</p>
 */
public record QuestionDetailRow(
        Long id,
        Long topicId,
        String topicName,
        Long categoryId,
        String categoryName,
        String title,
        String questionType,
        Integer starLevel,
        String difficulty,
        String frequencyLevel,
        String originType,
        String status,
        String oneLiner,
        String plainExplanation,
        String designReason,
        String commonMistakes,
        String scorePoints,
        String sourceVersion,
        Long version
) {
}
