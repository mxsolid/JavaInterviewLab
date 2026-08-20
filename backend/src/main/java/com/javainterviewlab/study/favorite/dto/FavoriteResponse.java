package com.javainterviewlab.study.favorite.dto;

import java.time.Instant;

/** 收藏题目列表响应；正文仍由题目详情接口按需读取。 */
public record FavoriteResponse(Long favoriteId, Long questionId, String title, Integer starLevel, Instant createdAt) {
}
