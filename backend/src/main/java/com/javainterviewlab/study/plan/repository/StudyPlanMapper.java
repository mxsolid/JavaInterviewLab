package com.javainterviewlab.study.plan.repository;

import com.javainterviewlab.study.plan.dto.StudyPlanItemResponse;
import com.javainterviewlab.study.plan.dto.StudyPlanSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

@Mapper
public interface StudyPlanMapper {

    List<StudyPlanSummaryResponse> findEnabledPlans();

    StudyPlanSummaryResponse findEnabledPlanById(@Param("planId") Long planId);

    List<StudyPlanDayRow> findDaysByPlanId(@Param("planId") Long planId);

    List<StudyPlanItemResponse> findItemsByDayId(@Param("dayId") Long dayId);

    Long findDefaultProfileId();

    /** 锁住默认 profile，使路线切换在同一 profile 内串行，避免同时产生两条 current plan。 */
    Long lockDefaultProfileId();

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
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("sortOrder") Integer sortOrder
    );

    record CurrentPlanRow(
            Long planId,
            String planCode,
            String planName,
            Integer durationDays,
            Instant startedAt
    ) {
    }
}
