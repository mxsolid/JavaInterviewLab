package com.javainterviewlab.content.question.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.content.question.dto.AnswerViewRequest;
import com.javainterviewlab.content.question.dto.AnswerViewResponse;
import com.javainterviewlab.content.question.dto.QuestionLearningResponse;
import com.javainterviewlab.content.question.dto.QuestionWorkspaceResponse;
import com.javainterviewlab.content.question.repository.QuestionAnswerViewMapper;
import com.javainterviewlab.content.question.repository.QuestionMapper;
import com.javainterviewlab.content.question.repository.model.QuestionAnswerViewRow;
import com.javainterviewlab.content.question.repository.model.QuestionDetailRow;
import com.javainterviewlab.study.profile.service.CurrentProfileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/** V0.3 题目工作区读取与显式答案披露服务。 */
@Service
public class QuestionWorkspaceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionWorkspaceService.class);

    private final QuestionMapper questionMapper;
    private final QuestionAnswerViewMapper answerViewMapper;
    private final CurrentProfileProvider currentProfileProvider;

    public QuestionWorkspaceService(
            QuestionMapper questionMapper,
            QuestionAnswerViewMapper answerViewMapper,
            CurrentProfileProvider currentProfileProvider
    ) {
        this.questionMapper = questionMapper;
        this.answerViewMapper = answerViewMapper;
        this.currentProfileProvider = currentProfileProvider;
    }

    public QuestionWorkspaceResponse metadata(Long questionId) {
        QuestionDetailRow row = requireEnabled(questionId);
        return new QuestionWorkspaceResponse(
                row.id(), row.topicId(), row.topicName(), row.categoryId(), row.categoryName(),
                row.title(), row.questionType(), row.starLevel(), row.difficulty(), row.frequencyLevel(),
                row.originType(), row.sourceVersion(), "PRACTICE_METADATA",
                questionMapper.findTags(questionId).stream()
                        .map(tag -> new QuestionWorkspaceResponse.TagItem(tag.id(), tag.code(), tag.name()))
                        .toList()
        );
    }

    public QuestionLearningResponse learning(Long questionId) {
        return toLearning(requireEnabled(questionId));
    }

    /** 唯一键裁决网络重试；每个新 clientViewId 永久追加一条披露事实。 */
    @Transactional
    public AnswerViewResponse reveal(Long questionId, AnswerViewRequest request) {
        QuestionDetailRow question = requireEnabled(questionId);
        Long profileId = currentProfileProvider.requireProfileId();
        QuestionAnswerViewRow existing = answerViewMapper.find(profileId, request.clientViewId());
        if (existing != null) {
            validateQuestion(existing, questionId);
            return new AnswerViewResponse(request.clientViewId(), true, existing.createdAt(), toLearning(question));
        }

        Instant createdAt = answerViewMapper.insertIgnore(profileId, questionId, request.clientViewId());
        if (createdAt == null) {
            QuestionAnswerViewRow concurrent = answerViewMapper.find(profileId, request.clientViewId());
            if (concurrent == null) {
                throw new IllegalStateException("答案披露写入后无法回读");
            }
            validateQuestion(concurrent, questionId);
            return new AnswerViewResponse(request.clientViewId(), true, concurrent.createdAt(), toLearning(question));
        }
        LOGGER.info("参考答案已披露, profileId={}, questionId={}", profileId, questionId);
        return new AnswerViewResponse(request.clientViewId(), false, createdAt, toLearning(question));
    }

    private QuestionDetailRow requireEnabled(Long questionId) {
        QuestionDetailRow row = questionMapper.findDetail(questionId);
        if (row == null || !"ENABLED".equals(row.status())) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "题目不存在或已停用");
        }
        return row;
    }

    private void validateQuestion(QuestionAnswerViewRow row, Long questionId) {
        if (!row.questionId().equals(questionId)) {
            throw new BusinessException(ApiErrorCode.VERSION_CONFLICT, "clientViewId 已用于其他题目");
        }
    }

    private QuestionLearningResponse toLearning(QuestionDetailRow row) {
        return new QuestionLearningResponse(
                row.id(), "LEARNING", row.oneLiner(), row.plainExplanation(), row.designReason(),
                row.commonMistakes(), row.scorePoints(),
                questionMapper.findAnswers(row.id()).stream()
                        .map(answer -> new QuestionLearningResponse.AnswerItem(
                                answer.answerType(), answer.content(), answer.sortOrder()
                        )).toList(),
                questionMapper.findFollowUps(row.id()).stream()
                        .map(followUp -> new QuestionLearningResponse.FollowUpItem(
                                followUp.id(), followUp.title(), followUp.referenceAnswer(), followUp.sortOrder()
                        )).toList()
        );
    }
}
