package com.javainterviewlab.study.favorite.repository.model;

import com.javainterviewlab.common.content.ContentTargetType;
import lombok.Data;

import java.time.Instant;

/**
 * 收藏持久化实体，对应 favorite 表。
 *
 * <p>仅描述当前偏好；取消收藏可以删除，不能与 append-only 的答题历史混用。</p>
 */
@Data
public class FavoriteEntity {

    /** 收藏主键。 */
    private Long id;

    /** 学习档案主键。 */
    private Long profileId;

    /** 被收藏内容类型。 */
    private ContentTargetType targetType;

    /** 目标内容主键。 */
    private Long targetId;

    /** 首次收藏时间。 */
    private Instant createdAt;
}
