package com.javainterviewlab.study.review.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.study.attempt.repository.model.QuestionAttemptEntity;
import com.javainterviewlab.study.progress.dto.StudyProgressResponse;
import com.javainterviewlab.study.progress.domain.MasteryLevel;
import com.javainterviewlab.study.review.domain.ReviewPolicy;
import com.javainterviewlab.study.review.domain.ReviewTaskStatus;
import com.javainterviewlab.study.review.dto.ReviewTaskResponse;
import com.javainterviewlab.study.review.repository.ReviewTaskMapper;
import com.javainterviewlab.study.review.repository.model.ReviewTaskEntity;
import com.javainterviewlab.study.review.repository.model.ReviewTaskRow;
import com.javainterviewlab.study.profile.repository.StudyProfileMapper;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 间隔复习服务。
 *
 * <p>不使用定时任务主动推送；任务的到期状态由查询时和学习者提交下一次练习时自然体现，避免引入无必要后台状态机。</p>
 */
@Service
public class ReviewService {

    private final ReviewTaskMapper reviewTaskMapper;
    private final StudyProfileMapper studyProfileMapper;
    private final ReviewPolicy reviewPolicy;
    private final Clock clock;

    public ReviewService(
            ReviewTaskMapper reviewTaskMapper,
            StudyProfileMapper studyProfileMapper,
            ReviewPolicy reviewPolicy,
            Clock clock
    ) {
        this.reviewTaskMapper = reviewTaskMapper;
        this.studyProfileMapper = studyProfileMapper;
        this.reviewPolicy = reviewPolicy;
        this.clock = clock;
    }

    /**
     * 根据一次新答题安排下一次复习。
     *
     * <p>该方法由提交事务调用：先完成同题旧 pending，再写新的 pending，部分唯一索引确保任何时刻最多一个待复习任务。</p>
     */
    public ReviewTaskResponse scheduleAfterAttempt(QuestionAttemptEntity attempt, StudyProgressResponse progress) {
        Instant now = clock.instant();
        reviewTaskMapper.completePendingByProfileAndQuestion(attempt.getProfileId(), attempt.getQuestionId(), now);
        ReviewTaskEntity entity = new ReviewTaskEntity();
        entity.setProfileId(attempt.getProfileId());
        entity.setQuestionId(attempt.getQuestionId());
        entity.setDueAt(reviewPolicy.calculateNextReviewTime(
                MasteryLevel.valueOf(progress.masteryLevel()),
                attempt.getResultType(),
                now
        ));
        ReviewTaskEntity created = reviewTaskMapper.insertPending(entity);
        return new ReviewTaskResponse(
                created.getId(),
                created.getQuestionId(),
                null,
                null,
                created.getDueAt(),
                created.getStatus().name()
        );
    }

    /** 返回今天到期的待复习任务，按应用 Clock 的时区切分自然日。 */
    public List<ReviewTaskResponse> listToday() {
        LocalDate today = LocalDate.now(clock);
        Instant start = today.atStartOfDay(clock.getZone()).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        return reviewTaskMapper.findPendingDueBetween(requireDefaultProfileId(), start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    /** 返回指定状态的任务，默认 API 只用于 PENDING/COMPLETED/CANCELLED 的显式查询。 */
    public List<ReviewTaskResponse> listByStatus(ReviewTaskStatus status) {
        return reviewTaskMapper.findByProfileAndStatus(requireDefaultProfileId(), status.name()).stream()
                .map(this::toResponse)
                .toList();
    }

    private Long requireDefaultProfileId() {
        Long profileId = studyProfileMapper.findDefaultProfileId();
        if (profileId == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "默认学习档案不存在");
        }
        return profileId;
    }

    private ReviewTaskResponse toResponse(ReviewTaskRow row) {
        return new ReviewTaskResponse(row.id(), row.questionId(), row.title(), row.starLevel(), row.dueAt(), row.status());
    }
}
