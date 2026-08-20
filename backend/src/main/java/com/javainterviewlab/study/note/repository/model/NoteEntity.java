package com.javainterviewlab.study.note.repository.model;

import com.javainterviewlab.common.content.ContentTargetType;
import lombok.Data;

import java.time.Instant;

/**
 * 用户笔记持久化实体，对应 note 表。
 *
 * <p>version 是乐观锁版本；Entity 只在 Service 与 Repository 边界使用。</p>
 */
@Data
public class NoteEntity {

    /** 笔记主键。 */
    private Long id;

    /** 学习档案主键。 */
    private Long profileId;

    /** 被记录的内容类型。 */
    private ContentTargetType targetType;

    /** 被记录的内容主键。 */
    private Long targetId;

    /** 用户笔记正文。 */
    private String content;

    /** 乐观锁版本。 */
    private Long version;

    /** 首次创建时间。 */
    private Instant createdAt;

    /** 最近保存时间。 */
    private Instant updatedAt;
}
