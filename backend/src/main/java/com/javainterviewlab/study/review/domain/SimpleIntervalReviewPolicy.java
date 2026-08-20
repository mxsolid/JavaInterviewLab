package com.javainterviewlab.study.review.domain;

import com.javainterviewlab.study.attempt.domain.AttemptResultType;
import com.javainterviewlab.study.progress.domain.MasteryLevel;
import com.javainterviewlab.study.review.config.ReviewIntervalProperties;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 固定间隔复习策略。
 *
 * <p>规则简单且可解释，先按一次错误短间隔复习；此策略是独立变化点，未来升级算法无需侵入事务服务。</p>
 */
@Component
public class SimpleIntervalReviewPolicy implements ReviewPolicy {

    private final ReviewIntervalProperties intervals;

    public SimpleIntervalReviewPolicy(ReviewIntervalProperties intervals) {
        this.intervals = intervals;
    }

    @Override
    public Instant calculateNextReviewTime(MasteryLevel masteryLevel, AttemptResultType resultType, Instant now) {
        if (resultType == AttemptResultType.WRONG || masteryLevel == MasteryLevel.UNKNOWN) {
            return now.plus(intervals.getUnknown());
        }
        return switch (masteryLevel) {
            case SEEN -> now.plus(intervals.getSeen());
            case BASIC -> now.plus(intervals.getBasic());
            case SOLID -> now.plus(intervals.getSolid());
            case MASTERED -> now.plus(intervals.getMastered());
            case UNKNOWN -> now.plus(intervals.getUnknown());
        };
    }
}
