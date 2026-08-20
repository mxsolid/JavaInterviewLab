package com.javainterviewlab.study.note.dto;

import java.time.Instant;

/** 笔记响应，前端保存后必须使用最新 version 继续编辑。 */
public record NoteResponse(
        Long id,
        String targetType,
        Long targetId,
        String content,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
