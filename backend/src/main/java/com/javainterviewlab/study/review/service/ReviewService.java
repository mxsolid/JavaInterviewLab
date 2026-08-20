package com.javainterviewlab.study.review.service;

import com.javainterviewlab.study.attempt.repository.model.QuestionAttemptEntity;
import com.javainterviewlab.study.progress.domain.MasteryLevel;
import com.javainterviewlab.study.progress.repository.model.StudyProgressEntity;
import com.javainterviewlab.study.review.domain.ReviewPolicy;
import com.javainterviewlab.study.review.domain.ReviewTaskStatus;
import com.javainterviewlab.study.review.dto.ReviewTaskResponse;
import com.javainterviewlab.study.review.dto.ScheduledReviewResponse;
import com.javainterviewlab.study.review.repository.ReviewTaskMapper;
import com.javainterviewlab.study.review.repository.model.ReviewTaskEntity;
import com.javainterviewlab.study.review.repository.model.ReviewTaskRow;
import com.javainterviewlab.study.profile.service.CurrentProfileProvider;
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
    private final CurrentProfileProvider currentProfileProvider;
    private final ReviewPolicy reviewPolicy;
    private final Clock clock;

    public ReviewService(
            ReviewTaskMapper reviewTaskMapper,
            CurrentProfileProvider currentProfileProvider,
            ReviewPolicy reviewPolicy,
            Clock clock
    ) {
        this.reviewTaskMapper = reviewTaskMapper;
        this.currentProfileProvider = currentProfileProvider;
        this.reviewPolicy = reviewPolicy;
        this.clock = clock;
    }

    /**
     * 根据一次新答题安排下一次复习。
     *
     * <p>该方法由提交事务调用：先完成同题旧 pending，再写新的 pending，部分唯一索引确保任何时刻最多一个待复习任务。</p>
     */
    public ReviewTaskEntity scheduleAfterAttempt(QuestionAttemptEntity attempt, StudyProgressEntity progress) {
        Instant now = clock.instant();
        reviewTaskMapper.completePendingByProfileAndQuestion(attempt.getProfileId(), attempt.getQuestionId(), now);
        ReviewTaskEntity entity = new ReviewTaskEntity();
        entity.setProfileId(attempt.getProfileId());
        entity.setQuestionId(attempt.getQuestionId());
        entity.setDueAt(reviewPolicy.calculateNextReviewTime(
                progress.getMasteryLevel(),
                attempt.getResultType(),
                now
        ));
        return reviewTaskMapper.insertPending(entity);
    }

    /** 返回今天到期的待复习任务，按应用 Clock 的时区切分自然日。 */
    public List<ReviewTaskResponse> listToday() {
        LocalDate today = LocalDate.now(clock);
        Instant start = today.atStartOfDay(clock.getZone()).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        return reviewTaskMapper.findPendingDueBetween(currentProfileProvider.requireProfileId(), start, end).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 返回截止今天结束前仍待处理的复习任务。
     *
     * <p>未在原定日期完成的任务仍是学习者需要处理的事项，因此查询不能以当天零点作为下界。</p>
     */
    public List<ReviewTaskResponse> listDue() {
        LocalDate today = LocalDate.now(clock);
        Instant todayStart = today.atStartOfDay(clock.getZone()).toInstant();
        Instant tomorrowStart = today.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        return reviewTaskMapper.findPendingDueBefore(currentProfileProvider.requireProfileId(), tomorrowStart).stream()
                .map(row -> toResponse(row, row.dueAt().isBefore(todayStart)))
                .toList();
    }

    /** 返回指定状态的任务，默认 API 只用于 PENDING/COMPLETED/CANCELLED 的显式查询。 */
    public List<ReviewTaskResponse> listByStatus(ReviewTaskStatus status) {
        return reviewTaskMapper.findByProfileAndStatus(currentProfileProvider.requireProfileId(), status.name()).stream()
                .map(this::toResponse)
                .toList();
    }

    /** 读取当前 pending 任务，供重复提交返回可靠的现状而不重新调度。 */
    public ReviewTaskEntity findPendingEntity(Long profileId, Long questionId) {
        return reviewTaskMapper.findPendingByProfileIdAndQuestionId(profileId, questionId);
    }

    /** 将提交调度结果转换为接口模型。 */
    public ScheduledReviewResponse toScheduledResponse(ReviewTaskEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ScheduledReviewResponse(
                entity.getId(),
                entity.getQuestionId(),
                entity.getDueAt(),
                entity.getStatus().name(),
                entity.getStatus().getDescription()
        );
    }

    private ReviewTaskResponse toResponse(ReviewTaskRow row) {
        return toResponse(row, false);
    }

    private ReviewTaskResponse toResponse(ReviewTaskRow row, boolean overdue) {
        return new ReviewTaskResponse(
                row.id(), row.questionId(), row.title(), row.starLevel(), row.dueAt(), row.status(), overdue
        );
    }
}
