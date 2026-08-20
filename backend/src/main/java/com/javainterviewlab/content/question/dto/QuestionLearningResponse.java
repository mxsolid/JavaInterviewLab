package com.javainterviewlab.content.question.dto;

import java.util.List;

/** 学习模式或显式披露后返回的完整教学内容。 */
public record QuestionLearningResponse(
        Long questionId,
        String contentMode,
        String oneLiner,
        String plainExplanation,
        String designReason,
        String commonMistakes,
        String scorePoints,
        List<AnswerItem> answers,
        List<FollowUpItem> followUps
) {
    /** 分层参考答案。 */
    public record AnswerItem(String answerType, String content, Integer sortOrder) {}

    /** 追问及可选参考答案。 */
    public record FollowUpItem(Long id, String title, String referenceAnswer, Integer sortOrder) {}
}
