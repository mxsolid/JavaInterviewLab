package com.javainterviewlab.study.review.domain;

import com.javainterviewlab.study.attempt.domain.AttemptResultType;
import com.javainterviewlab.study.progress.domain.MasteryLevel;

import java.time.Instant;

/**
 * 复习时间计算策略。
 *
 * <p>提交服务只依赖该接口，未来更换 SM-2 或 FSRS 时不需要修改答题、进度和事务编排。</p>
 */
public interface ReviewPolicy {

    /** 根据本次结果和更新后的掌握度计算下一次复习时间。 */
    Instant calculateNextReviewTime(MasteryLevel masteryLevel, AttemptResultType resultType, Instant now);
}
