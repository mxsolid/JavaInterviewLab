package com.javainterviewlab.study.attempt.dto;

import com.javainterviewlab.study.attempt.domain.AttemptResultType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * 提交一次练习的请求。
 *
 * <p>clientAttemptId 由前端在一次练习开始时生成；网络重试必须复用该值，后端才能识别为同一历史事实。</p>
 */
public record SubmitAttemptRequest(
        @NotNull Long questionId,
        @NotNull UUID clientAttemptId,
        String answerText,
        boolean viewedAnswer,
        @Min(1) @Max(5) Integer selfRating,
        @NotNull AttemptResultType resultType,
        @Min(0) Long elapsedMs
) {
}
