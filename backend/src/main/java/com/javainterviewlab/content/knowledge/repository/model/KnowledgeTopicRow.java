package com.javainterviewlab.content.knowledge.repository.model;

/** Knowledge Map 的扁平数据库投影，由 Service 按分类组装。 */
public record KnowledgeTopicRow(
        Long categoryId,
        String categoryCode,
        String categoryName,
        String categoryDescription,
        Integer categorySortOrder,
        Long topicId,
        String topicCode,
        String topicName,
        String topicDescription,
        Integer topicStarLevel,
        Integer topicSortOrder,
        Long totalQuestionCount,
        Long touchedQuestionCount,
        Long masteredQuestionCount
) {
}
