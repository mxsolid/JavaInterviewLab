package com.javainterviewlab.content.knowledge.dto;

/** 知识地图专题节点；masteryRate 只统计 SOLID/MASTERED，不把已浏览冒充掌握。 */
public record KnowledgeTopicResponse(
        Long id,
        String code,
        String name,
        String description,
        Integer starLevel,
        Long totalQuestionCount,
        Long touchedQuestionCount,
        Long masteredQuestionCount,
        Double masteryRate,
        String state,
        String stateDescription
) {
}
