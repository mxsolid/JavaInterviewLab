package com.javainterviewlab.content.question.repository;

import com.javainterviewlab.content.question.dto.QuestionCreateRequest;
import com.javainterviewlab.content.question.dto.QuestionDetailResponse;
import com.javainterviewlab.content.question.dto.QuestionDetailRow;
import com.javainterviewlab.content.question.dto.QuestionQuery;
import com.javainterviewlab.content.question.dto.QuestionSummaryResponse;
import com.javainterviewlab.content.question.dto.QuestionUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuestionMapper {

    List<QuestionSummaryResponse> findPage(QuestionQuery query);

    long count(QuestionQuery query);

    QuestionDetailRow findDetail(@Param("id") Long id);

    Long insert(@Param("request") QuestionCreateRequest request);

    /** 使用 version 条件更新，返回 0 代表资源不存在或已被其他编辑者修改。 */
    int update(@Param("id") Long id, @Param("request") QuestionUpdateRequest request);

    void deleteAnswers(@Param("questionId") Long questionId);

    void deleteFollowUps(@Param("questionId") Long questionId);

    void deleteTags(@Param("questionId") Long questionId);

    void insertAnswer(
            @Param("questionId") Long questionId,
            @Param("answerType") String answerType,
            @Param("content") String content,
            @Param("sortOrder") int sortOrder
    );

    void insertFollowUp(
            @Param("questionId") Long questionId,
            @Param("title") String title,
            @Param("referenceAnswer") String referenceAnswer,
            @Param("sortOrder") int sortOrder
    );

    void insertTag(@Param("questionId") Long questionId, @Param("tagId") Long tagId);

    List<QuestionDetailResponse.TagItem> findTags(@Param("questionId") Long questionId);

    List<QuestionDetailResponse.AnswerItem> findAnswers(@Param("questionId") Long questionId);

    List<QuestionDetailResponse.FollowUpItem> findFollowUps(@Param("questionId") Long questionId);

    int countById(@Param("id") Long id);
}
