package com.javainterviewlab.study.progress.repository.model;

import com.javainterviewlab.study.progress.domain.MasteryLevel;
import com.javainterviewlab.study.progress.domain.StudyStage;
import lombok.Data;
import java.time.Instant;

/** 当前学习进度持久化实体，对应 study_progress；它是历史 attempt 的可更新快照。 */
@Data
public class StudyProgressEntity {
    /** 快照主键。 */ private Long id;
    /** 学习档案。 */ private Long profileId;
    /** 题目。 */ private Long questionId;
    /** 当前学习阶段。 */ private StudyStage stage;
    /** 当前掌握度。 */ private MasteryLevel masteryLevel;
    /** 已追加的答题次数。 */ private Integer attemptCount;
    /** 判定错误次数。 */ private Integer wrongCount;
    /** 是否在错题本中激活。 */ private boolean wrongBookActive;
    /** 最近学习时间。 */ private Instant lastStudiedAt;
    /** 乐观锁版本。 */ private Long version;
    /** 创建时间。 */ private Instant createdAt;
    /** 更新时间。 */ private Instant updatedAt;
}
