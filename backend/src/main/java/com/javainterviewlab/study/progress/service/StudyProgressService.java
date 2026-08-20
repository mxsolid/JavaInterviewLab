package com.javainterviewlab.study.progress.service;

import com.javainterviewlab.study.attempt.repository.model.QuestionAttemptEntity;
import com.javainterviewlab.study.progress.domain.MasteryCalculator;
import com.javainterviewlab.study.progress.domain.MasteryLevel;
import com.javainterviewlab.study.progress.dto.StudyProgressResponse;
import com.javainterviewlab.study.progress.dto.WrongQuestionResponse;
import com.javainterviewlab.study.progress.repository.StudyProgressMapper;
import com.javainterviewlab.study.progress.repository.model.StudyProgressEntity;
import com.javainterviewlab.study.progress.repository.model.WrongQuestionRow;
import com.javainterviewlab.study.profile.repository.StudyProfileMapper;
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
    private final StudyProfileMapper studyProfileMapper;
    private final Clock clock;

    public StudyProgressService(StudyProgressMapper mapper, StudyProfileMapper studyProfileMapper, Clock clock) {
        this.mapper = mapper;
        this.studyProfileMapper = studyProfileMapper;
        this.clock = clock;
    }

    /**
     * 应用一条新写入的答题历史。
     *
     * <p>先锁定学习档案行，弥补首次创建时没有进度行可加锁的问题。
     * 因此掌握度计算总是基于上一条已提交快照，UPSERT 只负责原子落库和计数累加。</p>
     */
    public StudyProgressResponse applyAttempt(QuestionAttemptEntity attempt) {
        mapper.lockProfileForProgress(attempt.getProfileId());
        StudyProgressEntity current = mapper.findByProfileIdAndQuestionIdForUpdate(
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
        return new StudyProgressResponse(
                entity.getQuestionId(),
                entity.getStage().name(),
                entity.getMasteryLevel().name(),
                entity.getAttemptCount(),
                entity.getWrongCount(),
                entity.isWrongBookActive(),
                entity.getLastStudiedAt(),
                entity.getVersion()
        );
    }

    /** 查询当前仍需处理的启用错题；已停用题目不会在默认列表中出现。 */
    public List<WrongQuestionResponse> listActiveWrongQuestions() {
        return mapper.findActiveWrongQuestions(requireDefaultProfileId()).stream()
                .map(this::toWrongQuestionResponse)
                .toList();
    }

    /**
     * 标记错题已解决。
     *
     * <p>重复调用保持幂等；它只清除当前错题标记，不会篡改历史错误次数，下一次 WRONG 会重新激活。</p>
     */
    public void resolveWrongBook(Long questionId) {
        mapper.resolveWrongBook(requireDefaultProfileId(), questionId);
    }

    private Long requireDefaultProfileId() {
        Long profileId = studyProfileMapper.findDefaultProfileId();
        if (profileId == null) {
            throw new com.javainterviewlab.common.exception.BusinessException(
                    com.javainterviewlab.common.api.ApiErrorCode.RESOURCE_NOT_FOUND,
                    "默认学习档案不存在"
            );
        }
        return profileId;
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
