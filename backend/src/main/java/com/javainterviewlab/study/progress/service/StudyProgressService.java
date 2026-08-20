package com.javainterviewlab.study.progress.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.study.attempt.repository.model.QuestionAttemptEntity;
import com.javainterviewlab.study.progress.domain.MasteryCalculator;
import com.javainterviewlab.study.progress.domain.MasteryLevel;
import com.javainterviewlab.study.progress.domain.StudyStage;
import com.javainterviewlab.study.progress.dto.StudyProgressResponse;
import com.javainterviewlab.study.progress.dto.WrongQuestionResponse;
import com.javainterviewlab.study.progress.repository.StudyProgressMapper;
import com.javainterviewlab.study.progress.repository.model.StudyProgressEntity;
import com.javainterviewlab.study.progress.repository.model.WrongQuestionRow;
import com.javainterviewlab.study.profile.service.CurrentProfileProvider;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.List;

/**
 * 将新答题历史转换为当前学习快照。
 *
 * <p>本服务只更新 {@code study_progress}，绝不修改 append-only 的答题历史。
 * 事务边界由 {@link com.javainterviewlab.study.attempt.service.StudySubmissionService} 持有，
 * 让历史和快照要么一起提交，要么一起回滚。</p>
 */
@Service
public class StudyProgressService {

    private final StudyProgressMapper mapper;
    private final CurrentProfileProvider currentProfileProvider;
    private final Clock clock;

    public StudyProgressService(
            StudyProgressMapper mapper,
            CurrentProfileProvider currentProfileProvider,
            Clock clock
    ) {
        this.mapper = mapper;
        this.currentProfileProvider = currentProfileProvider;
        this.clock = clock;
    }

    /**
     * 应用一条新写入的答题历史。
     *
     * <p>首次创建时没有进度行可加锁，因此使用 profile + question 粒度的事务锁。
     * 同题计算基于上一条已提交快照，不同题目不再被整个 profile 的行锁无谓串行化。</p>
     */
    public StudyProgressEntity applyAttempt(QuestionAttemptEntity attempt) {
        mapper.lockProgress(attempt.getProfileId(), attempt.getQuestionId());
        StudyProgressEntity current = mapper.findByProfileIdAndQuestionId(
                attempt.getProfileId(), attempt.getQuestionId()
        );
        MasteryLevel currentLevel = current == null ? MasteryLevel.UNKNOWN : current.getMasteryLevel();
        MasteryLevel next = MasteryCalculator.calculate(currentLevel, attempt.getResultType(), attempt.getSelfRating());
        StudyProgressEntity entity = mapper.upsertAfterAttempt(
                attempt.getProfileId(),
                attempt.getQuestionId(),
                next.name(),
                attempt.getResultType().name().equals("WRONG"),
                clock.instant()
        );
        return entity;
    }

    /**
     * 查询单题当前学习进度。
     *
     * <p>从未练习的题目返回默认快照，让题目详情页无需为“还没有数据库行”维护另一套状态。</p>
     */
    public StudyProgressResponse getQuestionProgress(Long questionId) {
        verifyEnabledQuestion(questionId);
        return toResponse(questionId, findCurrentEntity(currentProfileProvider.requireProfileId(), questionId));
    }

    /** 读取指定档案和题目的当前快照，供提交幂等回填使用。 */
    public StudyProgressEntity findCurrentEntity(Long profileId, Long questionId) {
        return mapper.findByProfileIdAndQuestionId(profileId, questionId);
    }

    /** 在 HTTP 边界组装当前快照；旧答题历史缺少快照时同样按未学习处理。 */
    public StudyProgressResponse toResponse(Long questionId, StudyProgressEntity entity) {
        if (entity == null) {
            return new StudyProgressResponse(
                    questionId,
                    StudyStage.PREVIEW.name(),
                    StudyStage.PREVIEW.getDescription(),
                    MasteryLevel.UNKNOWN.name(),
                    MasteryLevel.UNKNOWN.getDescription(),
                    0,
                    0,
                    false,
                    null,
                    0L
            );
        }
        return new StudyProgressResponse(
                entity.getQuestionId(),
                entity.getStage().name(),
                entity.getStage().getDescription(),
                entity.getMasteryLevel().name(),
                entity.getMasteryLevel().getDescription(),
                entity.getAttemptCount(),
                entity.getWrongCount(),
                entity.isWrongBookActive(),
                entity.getLastStudiedAt(),
                entity.getVersion()
        );
    }

    /** 查询当前仍需处理的启用错题；已停用题目不会在默认列表中出现。 */
    public List<WrongQuestionResponse> listActiveWrongQuestions() {
        return mapper.findActiveWrongQuestions(currentProfileProvider.requireProfileId()).stream()
                .map(this::toWrongQuestionResponse)
                .toList();
    }

    /**
     * 标记错题已解决。
     *
     * <p>重复调用保持幂等；它只清除当前错题标记，不会篡改历史错误次数，下一次 WRONG 会重新激活。</p>
     */
    public void resolveWrongBook(Long questionId) {
        mapper.resolveWrongBook(currentProfileProvider.requireProfileId(), questionId);
    }

    private void verifyEnabledQuestion(Long questionId) {
        if (mapper.countEnabledQuestionById(questionId) == 0) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "题目不存在或已停用");
        }
    }

    private WrongQuestionResponse toWrongQuestionResponse(WrongQuestionRow row) {
        return new WrongQuestionResponse(
                row.questionId(),
                row.title(),
                row.starLevel(),
                row.masteryLevel(),
                row.attemptCount(),
                row.wrongCount(),
                row.lastStudiedAt()
        );
    }
}
