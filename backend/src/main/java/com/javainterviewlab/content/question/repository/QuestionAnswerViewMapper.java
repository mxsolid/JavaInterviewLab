package com.javainterviewlab.content.question.repository;

import com.javainterviewlab.content.question.repository.model.QuestionAnswerViewRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.UUID;

/** 答案披露行为的 append-only 数据访问。 */
@Mapper
public interface QuestionAnswerViewMapper {

    QuestionAnswerViewRow find(
            @Param("profileId") Long profileId,
            @Param("clientViewId") UUID clientViewId
    );

    Instant insertIgnore(
            @Param("profileId") Long profileId,
            @Param("questionId") Long questionId,
            @Param("clientViewId") UUID clientViewId
    );
}
