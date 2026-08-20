package com.javainterviewlab.study.plan.repository;

import com.javainterviewlab.study.plan.domain.StudyPlanTargetType;
import com.javainterviewlab.study.plan.repository.model.CurrentPlanRow;
import com.javainterviewlab.study.plan.repository.model.StudyPlanDayRow;
import com.javainterviewlab.study.plan.repository.model.StudyPlanItemRow;
import com.javainterviewlab.study.plan.repository.model.StudyPlanRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/**
 * 学习路线表的持久化 Mapper。
 *
 * <p>Mapper 只返回数据库投影；每日计划、当前路线等 HTTP 响应由 Service 组装。</p>
 */
@Mapper
public interface StudyPlanMapper {

    /** 读取可选择的系统路线。 */
    List<StudyPlanRow> findEnabledPlans();

    /** 按主键读取已启用路线。 */
    StudyPlanRow findEnabledPlanById(@Param("planId") Long planId);

    List<StudyPlanDayRow> findDaysByPlanId(@Param("planId") Long planId);

    /** 读取单日已解析的学习目标。 */
    List<StudyPlanItemRow> findItemsByDayId(@Param("dayId") Long dayId);

    Long findDefaultProfileId();

    /** 锁住默认 profile，使路线切换在同一 profile 内串行，避免同时产生两条 current plan。 */
    Long lockDefaultProfileId();

    /** 读取当前激活路线的元信息和开始时间。 */
    CurrentPlanRow findActivePlanByProfileId(@Param("profileId") Long profileId);

    void deactivateActivePlans(@Param("profileId") Long profileId);

    void activatePlan(
            @Param("profileId") Long profileId,
            @Param("planId") Long planId,
            @Param("startedAt") Instant startedAt
    );

    StudyPlanDayRow findDayByPlanIdAndDayNumber(
            @Param("planId") Long planId,
            @Param("dayNumber") Integer dayNumber
    );

    Long upsertPlan(
            @Param("code") String code,
            @Param("name") String name,
            @Param("durationDays") Integer durationDays,
            @Param("description") String description,
            @Param("sortOrder") Integer sortOrder
    );

    Long upsertDay(
            @Param("planId") Long planId,
            @Param("dayNumber") Integer dayNumber,
            @Param("title") String title,
            @Param("description") String description
    );

    Long findTopicIdByCode(@Param("code") String code);

    Long findQuestionIdByExternalKey(@Param("externalKey") String externalKey);

    void upsertItem(
            @Param("dayId") Long dayId,
            @Param("targetType") StudyPlanTargetType targetType,
            @Param("targetId") Long targetId,
            @Param("sortOrder") Integer sortOrder
    );

    /** 删除单日旧目标，使 catalog 删除某项后数据库不会残留失效内容。 */
    void deleteItemsByDayId(@Param("dayId") Long dayId);
}
