package com.javainterviewlab.content.question.dto;

import com.javainterviewlab.content.question.domain.Difficulty;
import com.javainterviewlab.content.question.domain.FrequencyLevel;
import com.javainterviewlab.content.question.domain.OriginType;
import com.javainterviewlab.content.question.domain.QuestionType;
import com.javainterviewlab.content.shared.ContentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 更新请求必须包含 version。
 *
 * <p>条件更新会拒绝旧版本，防止后保存者静默覆盖另一位编辑者的新内容。</p>
 */
@Schema(description = "修改题目请求")
public record QuestionUpdateRequest(
        @NotNull Long topicId,
        @NotBlank @Size(max = 500) String title,
        QuestionType questionType,
        @NotNull @Min(1) @Max(5) Integer starLevel,
        @NotNull Difficulty difficulty,
        @NotNull FrequencyLevel frequencyLevel,
        OriginType originType,
        ContentStatus status,
        @Size(max = 10000) String oneLiner,
        @Size(max = 50000) String plainExplanation,
        @Size(max = 50000) String designReason,
        @Size(max = 50000) String commonMistakes,
        @Size(max = 50000) String scorePoints,
        @NotNull @Min(0) @Schema(description = "详情接口返回的版本号", example = "0") Long version,
        List<Long> tagIds,
        List<@Valid QuestionAnswerRequest> answers,
        List<@Valid QuestionFollowUpRequest> followUps
) implements QuestionContentRequest {
}
