package com.javainterviewlab.content.question.dto;

import com.javainterviewlab.content.question.domain.AnswerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 单个答案层级的编辑请求；同一题的重复类型由 Service 拒绝。 */
public record QuestionAnswerRequest(@NotNull AnswerType answerType, @NotBlank @Size(max=50000) String content, Integer sortOrder) {
    public int effectiveSortOrder() { return sortOrder == null ? 0 : sortOrder; }
}
