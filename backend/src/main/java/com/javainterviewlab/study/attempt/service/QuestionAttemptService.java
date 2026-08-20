package com.javainterviewlab.study.attempt.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.study.attempt.dto.SubmitAttemptRequest;
import com.javainterviewlab.study.attempt.repository.QuestionAttemptMapper;
import com.javainterviewlab.study.attempt.repository.model.QuestionAttemptEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 追加答题历史的业务服务。
 *
 * <p>一次练习只能写入一条历史事实；本任务不更新 progress 或 review，避免提前引入后续任务的状态变化。</p>
 */
@Service
public class QuestionAttemptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionAttemptService.class);

    private final QuestionAttemptMapper questionAttemptMapper;

    public QuestionAttemptService(QuestionAttemptMapper questionAttemptMapper) {
        this.questionAttemptMapper = questionAttemptMapper;
    }

    /**
     * 提交一次答题历史。
     *
     * <p>插入和冲突回读放在同一事务中：先依赖数据库唯一约束裁决并发，再返回同一 UUID 对应的原记录。</p>
     */
    public AppendAttemptResult appendIdempotently(SubmitAttemptRequest request) {
        Long profileId = requireDefaultProfileId();
        verifyEnabledQuestion(request.questionId());
        QuestionAttemptEntity entity = toEntity(profileId, request);
        Long attemptId = questionAttemptMapper.insertIgnore(entity);
        if (attemptId == null) {
            QuestionAttemptEntity existing = questionAttemptMapper.findByProfileIdAndClientAttemptId(
                    profileId, request.clientAttemptId()
            );
            if (existing == null) {
                throw new IllegalStateException("答题历史幂等冲突后未找到原记录");
            }
            LOGGER.debug("答题历史幂等命中, profileId={}, questionId={}, attemptId={}", profileId, request.questionId(), existing.getId());
            return new AppendAttemptResult(existing, true);
        }
        QuestionAttemptEntity created = questionAttemptMapper.findById(attemptId);
        LOGGER.info("答题历史创建成功, profileId={}, questionId={}, attemptId={}, resultType={}",
                profileId, request.questionId(), attemptId, request.resultType());
        return new AppendAttemptResult(created, false);
    }

    private Long requireDefaultProfileId() {
        Long profileId = questionAttemptMapper.findDefaultProfileId();
        if (profileId == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "默认学习档案不存在");
        }
        return profileId;
    }

    private void verifyEnabledQuestion(Long questionId) {
        if (questionAttemptMapper.countEnabledQuestionById(questionId) == 0) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "题目不存在或已停用");
        }
    }

    private QuestionAttemptEntity toEntity(Long profileId, SubmitAttemptRequest request) {
        QuestionAttemptEntity entity = new QuestionAttemptEntity();
        entity.setProfileId(profileId);
        entity.setQuestionId(request.questionId());
        entity.setClientAttemptId(request.clientAttemptId());
        entity.setAnswerText(request.answerText());
        entity.setViewedAnswer(request.viewedAnswer());
        entity.setSelfRating(request.selfRating());
        entity.setResultType(request.resultType());
        entity.setElapsedMs(request.elapsedMs());
        return entity;
    }

    public com.javainterviewlab.study.attempt.dto.QuestionAttemptResponse toResponse(QuestionAttemptEntity entity) {
        return new com.javainterviewlab.study.attempt.dto.QuestionAttemptResponse(
                entity.getId(), entity.getQuestionId(), entity.getClientAttemptId(), entity.isViewedAnswer(),
                entity.getSelfRating(), entity.getResultType().name(), entity.getElapsedMs(), entity.getCreatedAt()
        );
    }

    /** 幂等追加结果，duplicated 为 true 时调用方不得再次推进 progress。 */
    public record AppendAttemptResult(QuestionAttemptEntity attempt, boolean duplicated) { }
}
