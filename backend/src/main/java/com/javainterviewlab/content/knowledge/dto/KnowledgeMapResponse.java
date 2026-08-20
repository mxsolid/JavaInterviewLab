package com.javainterviewlab.content.knowledge.dto;

import java.time.Instant;
import java.util.List;

/** V0.3 知识地图契约；所有计数均按当前档案和启用题目实时聚合。 */
public record KnowledgeMapResponse(
        Instant generatedAt,
        Long totalQuestionCount,
        Long touchedQuestionCount,
        Long masteredQuestionCount,
        List<KnowledgeCategoryResponse> categories
) {
}
