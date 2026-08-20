package com.javainterviewlab.content.question.dto;

import java.util.List;

/** 练习模式首屏元数据；刻意不包含答案、讲解和追问参考答案。 */
public record QuestionWorkspaceResponse(
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
        String sourceVersion,
        String contentMode,
        List<TagItem> tags
) {
    /** 题目标签只用于上下文提示，不包含答案内容。 */
    public record TagItem(Long id, String code, String name) {}
}
