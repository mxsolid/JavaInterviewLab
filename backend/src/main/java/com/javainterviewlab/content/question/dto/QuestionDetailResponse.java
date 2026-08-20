package com.javainterviewlab.content.question.dto;

import java.util.List;

public record QuestionDetailResponse(Long id, Long topicId, String topicName, Long categoryId, String categoryName,
                                     String title, String questionType, Integer starLevel, String difficulty,
                                     String frequencyLevel, String originType, String status, String oneLiner,
                                     String plainExplanation, String designReason, String commonMistakes,
                                     String scorePoints, Long version, List<TagItem> tags,
                                     List<AnswerItem> answers, List<FollowUpItem> followUps) {
    public record TagItem(Long id, String code, String name) {}
    public record AnswerItem(String answerType, String content, Integer sortOrder) {}
    public record FollowUpItem(Long id, String title, String referenceAnswer, Integer sortOrder) {}
}
