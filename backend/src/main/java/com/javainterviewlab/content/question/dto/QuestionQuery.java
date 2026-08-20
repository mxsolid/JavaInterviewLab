package com.javainterviewlab.content.question.dto;

import com.javainterviewlab.content.question.domain.Difficulty;
import com.javainterviewlab.content.question.domain.FrequencyLevel;
import com.javainterviewlab.content.shared.ContentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record QuestionQuery(String keyword, Long categoryId, Long topicId, @Min(1) @Max(5) Integer starLevel,
                            Difficulty difficulty, FrequencyLevel frequencyLevel, ContentStatus status,
                            @Min(1) Integer page, @Min(1) @Max(100) Integer pageSize) {
    public int effectivePage() { return page == null ? 1 : page; }
    public int effectivePageSize() { return pageSize == null ? 20 : pageSize; }
    public int offset() { return (effectivePage() - 1) * effectivePageSize(); }
}
