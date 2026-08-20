package com.javainterviewlab.content.question.dto;

public record QuestionSummaryResponse(Long id, Long topicId, String topicName, Long categoryId, String categoryName,
                                      String title, Integer starLevel, String difficulty, String frequencyLevel,
                                      String status, String oneLiner, Long version) {
}
