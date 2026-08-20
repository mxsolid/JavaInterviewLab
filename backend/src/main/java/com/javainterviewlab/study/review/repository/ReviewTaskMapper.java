package com.javainterviewlab.study.review.repository;

import com.javainterviewlab.study.review.repository.model.ReviewTaskEntity;
import com.javainterviewlab.study.review.repository.model.ReviewTaskRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/** 复习任务的数据库访问接口。 */
@Mapper
public interface ReviewTaskMapper {

    /** 新答题先关闭旧 pending，确保部分唯一索引允许写入新的计划。 */
    int completePendingByProfileAndQuestion(
            @Param("profileId") Long profileId,
            @Param("questionId") Long questionId,
            @Param("completedAt") Instant completedAt
    );

    /** 新建一条待复习任务，部分唯一索引是最终并发防线。 */
    ReviewTaskEntity insertPending(@Param("entity") ReviewTaskEntity entity);

    /** 查询某状态下的启用题目复习列表。 */
    List<ReviewTaskRow> findByProfileAndStatus(@Param("profileId") Long profileId, @Param("status") String status);

    /** 查询今天到期的待复习任务。 */
    List<ReviewTaskRow> findPendingDueBetween(
            @Param("profileId") Long profileId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
