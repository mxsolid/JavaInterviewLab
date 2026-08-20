package com.javainterviewlab.study.favorite.repository.model;

import java.time.Instant;

/** 收藏列表的题目摘要投影，避免 Mapper 依赖 HTTP Response DTO。 */
public record FavoriteQuestionRow(
        Long favoriteId,
        Long questionId,
        String title,
        Integer starLevel,
        Instant createdAt
) {
}
