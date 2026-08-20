package com.javainterviewlab.content.question.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.api.PageResponse;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.content.question.dto.QuestionAnswerRequest;
import com.javainterviewlab.content.question.dto.QuestionContentRequest;
import com.javainterviewlab.content.question.dto.QuestionCreateRequest;
import com.javainterviewlab.content.question.dto.QuestionDetailResponse;
import com.javainterviewlab.content.question.dto.QuestionDetailRow;
import com.javainterviewlab.content.question.dto.QuestionFollowUpRequest;
import com.javainterviewlab.content.question.dto.QuestionQuery;
import com.javainterviewlab.content.question.dto.QuestionSummaryResponse;
import com.javainterviewlab.content.question.dto.QuestionUpdateRequest;
import com.javainterviewlab.content.question.repository.QuestionMapper;
import com.javainterviewlab.content.tag.repository.TagMapper;
import com.javainterviewlab.content.topic.repository.TopicMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 维护题目主数据与子项，事务边界保证聚合内容不会只保存一部分。 */
@Service
public class QuestionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionMapper questionMapper;
    private final TopicMapper topicMapper;
    private final TagMapper tagMapper;

    public QuestionService(QuestionMapper questionMapper, TopicMapper topicMapper, TagMapper tagMapper) {
        this.questionMapper = questionMapper;
        this.topicMapper = topicMapper;
        this.tagMapper = tagMapper;
    }

    /** 按筛选条件分页返回摘要，避免列表页读取答案正文。 */
    public PageResponse<QuestionSummaryResponse> list(QuestionQuery query) {
        List<QuestionSummaryResponse> items = questionMapper.findPage(query);
        long total = questionMapper.count(query);
        return new PageResponse<>(items, total, query.effectivePage(), query.effectivePageSize());
    }

    /** 查询并组装一对多子项；题目不存在时返回受控 404。 */
    public QuestionDetailResponse detail(Long id) {
        return requireDetail(id);
    }

    /** 创建题目及完整子项；任一步失败都会回滚。 */
    @Transactional
    public QuestionDetailResponse create(QuestionCreateRequest request) {
        validateContent(request);
        Long questionId = questionMapper.insert(request);
        replaceChildren(questionId, request);
        LOGGER.info("题目创建成功, questionId={}, topicId={}", questionId, request.topicId());
        return requireDetail(questionId);
    }

    /**
     * 使用乐观锁更新题目。
     *
     * <p>version 不一致时不覆盖数据库中的新内容，防止两个编辑页面同时保存时后保存者静默覆盖前一个人的修改。</p>
     */
    @Transactional
    public QuestionDetailResponse update(Long id, QuestionUpdateRequest request) {
        validateContent(request);
        int updated = questionMapper.update(id, request);
        if (updated == 0) {
            if (questionMapper.countById(id) == 0) {
                throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "题目不存在");
            }
            throw new BusinessException(ApiErrorCode.VERSION_CONFLICT, ApiErrorCode.VERSION_CONFLICT.getDefaultMessage());
        }
        replaceChildren(id, request);
        LOGGER.info("题目更新成功, questionId={}, topicId={}", id, request.topicId());
        return requireDetail(id);
    }

    private void validateContent(QuestionContentRequest request) {
        if (topicMapper.countById(request.topicId()) == 0) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "专题不存在");
        }
        validateTags(request.tagIds());
        validateAnswers(request.answers());
    }

    private void validateTags(List<Long> tagIds) {
        List<Long> safeTagIds = tagIds == null ? List.of() : tagIds;
        if (new HashSet<>(safeTagIds).size() != safeTagIds.size()) {
            throw new BusinessException(ApiErrorCode.VALIDATION_FAILED, "标签不能重复");
        }
        if (!safeTagIds.isEmpty() && tagMapper.countByIds(safeTagIds) != safeTagIds.size()) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "存在不存在的标签");
        }
    }

    private void validateAnswers(List<QuestionAnswerRequest> answers) {
        List<QuestionAnswerRequest> safeAnswers = answers == null ? List.of() : answers;
        Set<Object> answerTypes = new HashSet<>();
        for (QuestionAnswerRequest answer : safeAnswers) {
            if (!answerTypes.add(answer.answerType())) {
                throw new BusinessException(ApiErrorCode.VALIDATION_FAILED, "同一种答案只能保存一次");
            }
        }
    }

    /** 编辑页提交完整快照，因此事务内全量替换子项，避免已删除项残留。 */
    private void replaceChildren(Long questionId, QuestionContentRequest request) {
        questionMapper.deleteAnswers(questionId);
        questionMapper.deleteFollowUps(questionId);
        questionMapper.deleteTags(questionId);
        if (request.answers() != null) {
            for (QuestionAnswerRequest answer : request.answers()) {
                questionMapper.insertAnswer(questionId, answer.answerType().name(), answer.content(), answer.effectiveSortOrder());
            }
        }
        if (request.followUps() != null) {
            for (QuestionFollowUpRequest followUp : request.followUps()) {
                questionMapper.insertFollowUp(questionId, followUp.title(), followUp.referenceAnswer(), followUp.effectiveSortOrder());
            }
        }
        if (request.tagIds() != null) {
            for (Long tagId : request.tagIds()) {
                questionMapper.insertTag(questionId, tagId);
            }
        }
    }

    private QuestionDetailResponse requireDetail(Long id) {
        QuestionDetailRow row = questionMapper.findDetail(id);
        if (row == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "题目不存在");
        }
        return new QuestionDetailResponse(
                row.id(), row.topicId(), row.topicName(), row.categoryId(), row.categoryName(), row.title(),
                row.questionType(), row.starLevel(), row.difficulty(), row.frequencyLevel(), row.originType(),
                row.status(), row.oneLiner(), row.plainExplanation(), row.designReason(), row.commonMistakes(),
                row.scorePoints(), row.version(), questionMapper.findTags(id), questionMapper.findAnswers(id),
                questionMapper.findFollowUps(id)
        );
    }
}
