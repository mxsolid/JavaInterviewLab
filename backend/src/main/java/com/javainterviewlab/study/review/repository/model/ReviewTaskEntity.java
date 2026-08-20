package com.javainterviewlab.study.review.repository.model;

import com.javainterviewlab.study.review.domain.ReviewTaskStatus;
import lombok.Data;

import java.time.Instant;

/** review_task 持久化实体；历史状态保留以便后续复盘复习节奏。 */
@Data
public class ReviewTaskEntity {

    /** 任务主键。 */
    private Long id;
    /** 学习档案主键。 */
    private Long profileId;
    /** 关联题目主键。 */
    private Long questionId;
    /** 应当复习的时间。 */
    private Instant dueAt;
    /** 当前任务状态。 */
    private ReviewTaskStatus status;
    /** 创建时间。 */
    private Instant createdAt;
    /** 完成时间；待复习任务为空。 */
    private Instant completedAt;
}
