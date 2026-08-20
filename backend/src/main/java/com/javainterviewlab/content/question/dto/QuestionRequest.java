package com.javainterviewlab.content.question.dto;

import com.javainterviewlab.content.question.domain.*;
import com.javainterviewlab.content.shared.ContentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record QuestionRequest(
        @NotNull Long topicId,
        @NotBlank @Size(max=500) String title,
        QuestionType questionType,
        @NotNull @Min(1) @Max(5) Integer starLevel,
        @NotNull Difficulty difficulty,
        @NotNull FrequencyLevel frequencyLevel,
        OriginType originType,
        ContentStatus status,
        @Size(max=10000) String oneLiner,
        @Size(max=50000) String plainExplanation,
        @Size(max=50000) String designReason,
        @Size(max=50000) String commonMistakes,
        @Size(max=50000) String scorePoints,
        @NotNull Long version,
        List<Long> tagIds,
        List<@Valid QuestionAnswerRequest> answers,
        List<@Valid QuestionFollowUpRequest> followUps
) {
    public QuestionType effectiveQuestionType() { return questionType == null ? QuestionType.KNOWLEDGE : questionType; }
    public OriginType effectiveOriginType() { return originType == null ? OriginType.USER : originType; }
    public ContentStatus effectiveStatus() { return status == null ? ContentStatus.ENABLED : status; }
}
