package com.javainterviewlab.study.attempt.repository.model;

import com.javainterviewlab.study.attempt.domain.AttemptResultType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 答题历史持久化实体，对应 question_attempt 表。
 *
 * <p>该表是 append-only 历史事实，普通业务不提供 update/delete，以保留从不会到掌握的完整轨迹。</p>
 */
@Data
public class QuestionAttemptEntity {

    /** 答题历史主键。 */
    private Long id;

    /** 学习档案主键。 */
    private Long profileId;

    /** 被练习的题目主键。 */
    private Long questionId;

    /** 客户端生成的单次练习幂等标识。 */
    private UUID clientAttemptId;

    /** 用户输入的答案，只保存不写入日志。 */
    private String answerText;

    /** 用户是否已查看参考答案。 */
    private boolean viewedAnswer;

    /** 用户自评，允许为空。 */
    private Integer selfRating;

    /** 本次练习的结果判定。 */
    private AttemptResultType resultType;

    /** 从开始练习到提交的耗时毫秒数。 */
    private Long elapsedMs;

    /** 数据库写入时间。 */
    private Instant createdAt;
}
