package com.javainterviewlab.content.question.dto;

import java.time.Instant;
import java.util.UUID;

/** 答案披露结果；重复请求返回原披露时间和同一教学内容。 */
public record AnswerViewResponse(
        UUID clientViewId,
        boolean duplicated,
        Instant viewedAt,
        QuestionLearningResponse learning
) {}
