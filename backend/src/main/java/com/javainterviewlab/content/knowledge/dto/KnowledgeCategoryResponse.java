package com.javainterviewlab.content.knowledge.dto;

import java.util.List;

/** 知识地图分类节点。 */
public record KnowledgeCategoryResponse(
        Long id,
        String code,
        String name,
        String description,
        List<KnowledgeTopicResponse> topics
) {
}
