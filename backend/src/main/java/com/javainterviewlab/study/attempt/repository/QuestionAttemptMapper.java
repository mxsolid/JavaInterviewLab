package com.javainterviewlab.study.attempt.repository;

import com.javainterviewlab.study.attempt.repository.model.QuestionAttemptEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.UUID;

/**
 * 答题历史的数据库访问接口。
 *
 * <p>只提供追加和读取操作；UUID 唯一约束负责并发重试下的最终幂等防线。</p>
 */
@Mapper
public interface QuestionAttemptMapper {

    /** 读取唯一默认学习档案。 */
    Long findDefaultProfileId();

    /** 仅允许向存在且启用的题目追加练习历史。 */
    int countEnabledQuestionById(@Param("questionId") Long questionId);

    /**
     * 尝试追加历史。发生相同 profile + UUID 冲突时不写入，并返回 null。
     */
    Long insertIgnore(@Param("entity") QuestionAttemptEntity entity);

    /** 查询新增后或幂等冲突时原有的历史事实。 */
    QuestionAttemptEntity findById(@Param("id") Long id);

    /** 按业务幂等键读取已保存的历史事实。 */
    QuestionAttemptEntity findByProfileIdAndClientAttemptId(
            @Param("profileId") Long profileId,
            @Param("clientAttemptId") UUID clientAttemptId
    );
}
