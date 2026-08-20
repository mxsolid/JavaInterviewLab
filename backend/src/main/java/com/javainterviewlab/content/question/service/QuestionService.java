package com.javainterviewlab.content.question.service;

import com.javainterviewlab.common.api.*;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.content.question.dto.*;
import com.javainterviewlab.content.question.repository.QuestionMapper;
import com.javainterviewlab.content.tag.repository.TagMapper;
import com.javainterviewlab.content.topic.repository.TopicMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
public class QuestionService {
    private final QuestionMapper questionMapper; private final TopicMapper topicMapper; private final TagMapper tagMapper;
    public QuestionService(QuestionMapper questionMapper, TopicMapper topicMapper, TagMapper tagMapper) { this.questionMapper=questionMapper; this.topicMapper=topicMapper; this.tagMapper=tagMapper; }

    public PageResponse<QuestionSummaryResponse> list(QuestionQuery query) {
        List<QuestionSummaryResponse> items=questionMapper.findPage(query.keyword(),query.categoryId(),query.topicId(),query.starLevel(),query.difficulty(),query.frequencyLevel(),query.status(),query.effectivePageSize(),query.offset());
        long total=questionMapper.count(query.keyword(),query.categoryId(),query.topicId(),query.starLevel(),query.difficulty(),query.frequencyLevel(),query.status());
        return new PageResponse<>(items,total,query.effectivePage(),query.effectivePageSize());
    }
    public QuestionDetailResponse detail(Long id) { return requireDetail(id); }
    @Transactional
    public QuestionDetailResponse create(QuestionRequest request) {
        validateRequest(request); Long id=questionMapper.insert(request.topicId(),request.title(),request.effectiveQuestionType().name(),request.starLevel(),request.difficulty().name(),request.frequencyLevel().name(),request.effectiveOriginType().name(),request.effectiveStatus().name(),request.oneLiner(),request.plainExplanation(),request.designReason(),request.commonMistakes(),request.scorePoints());
        replaceChildren(id,request); return requireDetail(id);
    }
    @Transactional
    public QuestionDetailResponse update(Long id, QuestionRequest request) {
        validateRequest(request);
        int updated=questionMapper.update(id,request.version(),request.topicId(),request.title(),request.effectiveQuestionType().name(),request.starLevel(),request.difficulty().name(),request.frequencyLevel().name(),request.effectiveOriginType().name(),request.effectiveStatus().name(),request.oneLiner(),request.plainExplanation(),request.designReason(),request.commonMistakes(),request.scorePoints());
        if(updated==0) {
            if(questionMapper.countById(id)==0) throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND,"题目不存在");
            throw new BusinessException(ApiErrorCode.VERSION_CONFLICT,ApiErrorCode.VERSION_CONFLICT.getDefaultMessage());
        }
        replaceChildren(id,request); return requireDetail(id);
    }
    private void validateRequest(QuestionRequest request) {
        if(topicMapper.countById(request.topicId())==0) throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND,"专题不存在");
        List<Long> tagIds=request.tagIds()==null?List.of():request.tagIds();
        if(new HashSet<>(tagIds).size()!=tagIds.size()) throw new BusinessException(ApiErrorCode.VALIDATION_FAILED,"标签不能重复");
        if(!tagIds.isEmpty() && tagMapper.countByIds(tagIds)!=tagIds.size()) throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND,"存在不存在的标签");
        List<QuestionAnswerRequest> answers=request.answers()==null?List.of():request.answers();
        Set<Object> types=new HashSet<>();
        for(QuestionAnswerRequest answer:answers) if(!types.add(answer.answerType())) throw new BusinessException(ApiErrorCode.VALIDATION_FAILED,"同一种答案只能保存一次");
    }
    /** 子项采用全量替换：编辑页提交的是完整快照，避免逐项差异计算留下已删除的答案或标签。 */
    private void replaceChildren(Long questionId,QuestionRequest request) {
        questionMapper.deleteAnswers(questionId); questionMapper.deleteFollowUps(questionId); questionMapper.deleteTags(questionId);
        if(request.answers()!=null) for(QuestionAnswerRequest item:request.answers()) questionMapper.insertAnswer(questionId,item.answerType().name(),item.content(),item.effectiveSortOrder());
        if(request.followUps()!=null) for(QuestionFollowUpRequest item:request.followUps()) questionMapper.insertFollowUp(questionId,item.title(),item.referenceAnswer(),item.effectiveSortOrder());
        if(request.tagIds()!=null) for(Long tagId:request.tagIds()) questionMapper.insertTag(questionId,tagId);
    }
    private QuestionDetailResponse requireDetail(Long id) {
        QuestionDetailRow detail=questionMapper.findDetail(id);
        if(detail==null) throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND,"题目不存在");
        return new QuestionDetailResponse(detail.id(),detail.topicId(),detail.topicName(),detail.categoryId(),detail.categoryName(),detail.title(),detail.questionType(),detail.starLevel(),detail.difficulty(),detail.frequencyLevel(),detail.originType(),detail.status(),detail.oneLiner(),detail.plainExplanation(),detail.designReason(),detail.commonMistakes(),detail.scorePoints(),detail.version(),questionMapper.findTags(id),questionMapper.findAnswers(id),questionMapper.findFollowUps(id));
    }
}
