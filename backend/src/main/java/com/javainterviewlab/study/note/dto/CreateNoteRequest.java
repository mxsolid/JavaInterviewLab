package com.javainterviewlab.study.note.dto;

import com.javainterviewlab.common.content.ContentTargetType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 首次保存笔记的请求；同一目标已有笔记时要求调用方改用带 version 的更新接口。 */
public record CreateNoteRequest(
        @NotNull ContentTargetType targetType,
        @NotNull Long targetId,
        @NotNull @Size(max = 20000) String content
) {
}
