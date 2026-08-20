package com.javainterviewlab.study.note.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 已存在笔记的保存请求；version 是防止静默覆盖的必填快照。 */
public record SaveNoteRequest(
        @NotNull @Size(max = 20000) String content,
        @NotNull Long version
) {
}
