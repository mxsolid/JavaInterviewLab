package com.javainterviewlab.study.favorite.repository;

import com.javainterviewlab.study.favorite.repository.model.FavoriteEntity;
import com.javainterviewlab.study.favorite.repository.model.FavoriteQuestionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 收藏状态的数据库访问接口。 */
@Mapper
public interface FavoriteMapper {

    /** 只有启用题目允许被新收藏。 */
    int countEnabledQuestionById(@Param("questionId") Long questionId);

    /** 唯一约束配合冲突忽略，使双击收藏保持幂等。 */
    FavoriteEntity insertIgnore(@Param("entity") FavoriteEntity entity);

    /** 删除当前偏好；收藏不是历史事实，因此不保留删除行。 */
    int deleteByProfileAndQuestion(@Param("profileId") Long profileId, @Param("questionId") Long questionId);

    /** 只返回仍启用的题目，停用内置内容不会出现在默认收藏列表。 */
    List<FavoriteQuestionRow> findEnabledQuestionFavorites(@Param("profileId") Long profileId);
}
