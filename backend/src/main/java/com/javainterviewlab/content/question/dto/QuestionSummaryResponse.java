package com.javainterviewlab.content.question.dto;

/** 题目列表响应，刻意不包含答案正文等大字段。 */
public record QuestionSummaryResponse(Long id, Long topicId, String topicName, Long categoryId, String categoryName,
                                      String title, Integer starLevel, String difficulty, String frequencyLevel,
                                      String status, String oneLiner, Long version) {
}
