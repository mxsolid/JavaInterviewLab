package com.javainterviewlab.content.topic.dto;

import com.javainterviewlab.content.shared.ContentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TopicRequest(
        @NotNull Long categoryId,
        @NotBlank @Pattern(regexp = "[a-z][a-z0-9-]{1,63}") String code,
        @NotBlank @Size(max = 160) String name,
        @Size(max = 2000) String description,
        @NotNull @Min(1) @Max(5) Integer starLevel,
        @Min(0) @Max(10000) Integer sortOrder,
        ContentStatus status
) {
    public int effectiveSortOrder() { return sortOrder == null ? 0 : sortOrder; }
    public ContentStatus effectiveStatus() { return status == null ? ContentStatus.ENABLED : status; }
}
