package com.javainterviewlab.content.question.dto;

/** 数据库主表行不包含一对多子项，避免 MyBatis 将聚合列表误当作查询列映射。 */
public record QuestionDetailRow(Long id, Long topicId, String topicName, Long categoryId, String categoryName,
                                String title, String questionType, Integer starLevel, String difficulty,
                                String frequencyLevel, String originType, String status, String oneLiner,
                                String plainExplanation, String designReason, String commonMistakes,
                                String scorePoints, Long version) {
}
