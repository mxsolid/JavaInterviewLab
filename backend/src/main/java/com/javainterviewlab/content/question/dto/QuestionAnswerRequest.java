package com.javainterviewlab.content.question.dto;

import com.javainterviewlab.content.question.domain.AnswerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuestionAnswerRequest(@NotNull AnswerType answerType, @NotBlank @Size(max=50000) String content, Integer sortOrder) {
    public int effectiveSortOrder() { return sortOrder == null ? 0 : sortOrder; }
}
