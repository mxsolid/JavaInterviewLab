package com.javainterviewlab.content.question.dto;

import com.javainterviewlab.content.question.domain.Difficulty;
import com.javainterviewlab.content.question.domain.FrequencyLevel;
import com.javainterviewlab.content.question.domain.OriginType;
import com.javainterviewlab.content.question.domain.QuestionType;
import com.javainterviewlab.content.shared.ContentStatus;

import java.util.List;

/** 创建和修改共有的题目内容，避免两套 DTO 的字段和校验逐渐漂移。 */
public interface QuestionContentRequest {

    Long topicId();

    String title();

    QuestionType questionType();

    Integer starLevel();

    Difficulty difficulty();

    FrequencyLevel frequencyLevel();

    OriginType originType();

    ContentStatus status();

    String oneLiner();

    String plainExplanation();

    String designReason();

    String commonMistakes();

    String scorePoints();

    List<Long> tagIds();

    List<QuestionAnswerRequest> answers();

    List<QuestionFollowUpRequest> followUps();

    default QuestionType effectiveQuestionType() {
        return questionType() == null ? QuestionType.KNOWLEDGE : questionType();
    }

    default OriginType effectiveOriginType() {
        return originType() == null ? OriginType.USER : originType();
    }

    default ContentStatus effectiveStatus() {
        return status() == null ? ContentStatus.ENABLED : status();
    }
}
