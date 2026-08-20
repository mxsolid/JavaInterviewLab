package com.javainterviewlab.study.dashboard.repository;

import com.javainterviewlab.study.dashboard.repository.model.DashboardStatsRow;
import com.javainterviewlab.study.dashboard.repository.model.RecentStudyRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/** Dashboard 聚合查询的数据库访问接口。 */
@Mapper
public interface DashboardMapper {

    /** 一次聚合返回题目、进度、错题与收藏统计。 */
    DashboardStatsRow getStats(@Param("profileId") Long profileId);

    /** 今日到期的启用题目复习数量。 */
    long countTodayReviews(@Param("profileId") Long profileId, @Param("start") Instant start, @Param("end") Instant end);

    /** 统计截止指定时刻仍待处理的启用题目复习数量，包含逾期任务。 */
    long countDueReviews(@Param("profileId") Long profileId, @Param("endExclusive") Instant endExclusive);

    /** 按最后学习时间返回有限条最近学习项目。 */
    List<RecentStudyRow> findRecentStudyItems(@Param("profileId") Long profileId, @Param("limit") int limit);
}
