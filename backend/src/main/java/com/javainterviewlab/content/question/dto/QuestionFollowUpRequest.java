package com.javainterviewlab.content.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionFollowUpRequest(@NotBlank @Size(max=500) String title, @Size(max=50000) String referenceAnswer, Integer sortOrder) {
    public int effectiveSortOrder() { return sortOrder == null ? 0 : sortOrder; }
}
