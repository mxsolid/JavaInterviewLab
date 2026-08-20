package com.javainterviewlab.content.question.dto;

import java.util.List;

/**
 * 题目详情响应。
 *
 * <p>答案、追问和标签是 API 展示子项；Repository 分别返回 Row，再由 Service 聚合到本对象。</p>
 */
public record QuestionDetailResponse(Long id, Long topicId, String topicName, Long categoryId, String categoryName,
                                     String title, String questionType, Integer starLevel, String difficulty,
                                     String frequencyLevel, String originType, String status, String oneLiner,
                                     String plainExplanation, String designReason, String commonMistakes,
                                     String scorePoints, Long version, List<TagItem> tags,
                                     List<AnswerItem> answers, List<FollowUpItem> followUps) {
    /** 题目关联的标签。 */
    public record TagItem(Long id, String code, String name) {}

    /** 按回答层级展示的答案内容。 */
    public record AnswerItem(String answerType, String content, Integer sortOrder) {}

    /** 用于面试延伸练习的追问。 */
    public record FollowUpItem(Long id, String title, String referenceAnswer, Integer sortOrder) {}
}
