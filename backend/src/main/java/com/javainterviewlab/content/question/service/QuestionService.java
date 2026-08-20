package com.javainterviewlab.content.question.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.api.PageResponse;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.content.question.dto.QuestionAnswerRequest;
import com.javainterviewlab.content.question.dto.QuestionContentRequest;
import com.javainterviewlab.content.question.dto.QuestionCreateRequest;
import com.javainterviewlab.content.question.dto.QuestionDetailResponse;
import com.javainterviewlab.content.question.dto.QuestionFollowUpRequest;
import com.javainterviewlab.content.question.dto.QuestionQuery;
import com.javainterviewlab.content.question.dto.QuestionSummaryResponse;
import com.javainterviewlab.content.question.dto.QuestionUpdateRequest;
import com.javainterviewlab.content.question.repository.QuestionMapper;
import com.javainterviewlab.content.question.repository.model.QuestionAnswerRow;
import com.javainterviewlab.content.question.repository.model.QuestionDetailRow;
import com.javainterviewlab.content.question.repository.model.QuestionEntity;
import com.javainterviewlab.content.question.repository.model.QuestionFollowUpRow;
import com.javainterviewlab.content.question.repository.model.QuestionQueryModel;
import com.javainterviewlab.content.question.repository.model.QuestionSummaryRow;
import com.javainterviewlab.content.question.repository.model.QuestionTagRow;
import com.javainterviewlab.content.tag.repository.TagMapper;
import com.javainterviewlab.content.topic.repository.TopicMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 维护题目主数据与子项。
 *
 * <p>题目编辑提交的是完整聚合快照，因此主表与子项替换必须在同一事务中完成，避免详情出现半更新状态。</p>
 */
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
        QuestionQueryModel model = new QuestionQueryModel(
                query.keyword(), query.categoryId(), query.topicId(), query.starLevel(), query.difficulty(),
                query.frequencyLevel(), query.status(), query.effectivePageSize(), query.offset()
        );
        List<QuestionSummaryResponse> items = questionMapper.findPage(model).stream().map(this::toSummaryResponse).toList();
        long total = questionMapper.count(model);
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
        Long questionId = questionMapper.insert(toEntity(null, request, null));
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
        int updated = questionMapper.update(toEntity(id, request, request.version()));
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
                row.scorePoints(), row.version(), questionMapper.findTags(id).stream().map(this::toTagItem).toList(),
                questionMapper.findAnswers(id).stream().map(this::toAnswerItem).toList(),
                questionMapper.findFollowUps(id).stream().map(this::toFollowUpItem).toList()
        );
    }

    private QuestionEntity toEntity(Long id, QuestionContentRequest request, Long version) {
        QuestionEntity entity = new QuestionEntity();
        entity.setId(id);
        entity.setTopicId(request.topicId());
        entity.setTitle(request.title());
        entity.setQuestionType(request.effectiveQuestionType());
        entity.setStarLevel(request.starLevel());
        entity.setDifficulty(request.difficulty());
        entity.setFrequencyLevel(request.frequencyLevel());
        entity.setOriginType(request.effectiveOriginType());
        entity.setStatus(request.effectiveStatus());
        entity.setOneLiner(request.oneLiner());
        entity.setPlainExplanation(request.plainExplanation());
        entity.setDesignReason(request.designReason());
        entity.setCommonMistakes(request.commonMistakes());
        entity.setScorePoints(request.scorePoints());
        entity.setVersion(version);
        return entity;
    }

    private QuestionSummaryResponse toSummaryResponse(QuestionSummaryRow row) {
        return new QuestionSummaryResponse(
                row.id(), row.topicId(), row.topicName(), row.categoryId(), row.categoryName(), row.title(),
                row.starLevel(), row.difficulty(), row.frequencyLevel(), row.status(), row.oneLiner(), row.version()
        );
    }

    private QuestionDetailResponse.TagItem toTagItem(QuestionTagRow row) {
        return new QuestionDetailResponse.TagItem(row.id(), row.code(), row.name());
    }

    private QuestionDetailResponse.AnswerItem toAnswerItem(QuestionAnswerRow row) {
        return new QuestionDetailResponse.AnswerItem(row.answerType(), row.content(), row.sortOrder());
    }

    private QuestionDetailResponse.FollowUpItem toFollowUpItem(QuestionFollowUpRow row) {
        return new QuestionDetailResponse.FollowUpItem(
                row.id(), row.title(), row.referenceAnswer(), row.sortOrder()
        );
    }
}
