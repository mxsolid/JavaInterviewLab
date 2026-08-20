package com.javainterviewlab.content.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 单个追问的编辑请求，参考答案可留空以支持仅记录问题。 */
public record QuestionFollowUpRequest(@NotBlank @Size(max=500) String title, @Size(max=50000) String referenceAnswer, Integer sortOrder) {
    public int effectiveSortOrder() { return sortOrder == null ? 0 : sortOrder; }
}
