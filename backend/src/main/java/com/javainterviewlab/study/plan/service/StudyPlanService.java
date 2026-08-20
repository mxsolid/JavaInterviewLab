package com.javainterviewlab.study.plan.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.study.plan.dto.CurrentPlanResponse;
import com.javainterviewlab.study.plan.dto.StudyPlanDayResponse;
import com.javainterviewlab.study.plan.dto.StudyPlanDetailResponse;
import com.javainterviewlab.study.plan.dto.StudyPlanItemResponse;
import com.javainterviewlab.study.plan.dto.StudyPlanSummaryResponse;
import com.javainterviewlab.study.plan.dto.TodayStudyResponse;
import com.javainterviewlab.study.plan.repository.StudyPlanMapper;
import com.javainterviewlab.study.plan.repository.model.CurrentPlanRow;
import com.javainterviewlab.study.plan.repository.model.StudyPlanDayRow;
import com.javainterviewlab.study.plan.repository.model.StudyPlanItemRow;
import com.javainterviewlab.study.plan.repository.model.StudyPlanRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Clock;
import java.util.List;

/**
 * 学习路线的查询和选择服务。
 *
 * <p>查询接口只读取 PostgreSQL；路线目录同步由应用启动和种子导入显式触发，不能让 GET 请求产生写副作用。</p>
 */
@Service
public class StudyPlanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudyPlanService.class);

    private final StudyPlanMapper studyPlanMapper;
    private final Clock clock;

    public StudyPlanService(StudyPlanMapper studyPlanMapper, Clock clock) {
        this.studyPlanMapper = studyPlanMapper;
        this.clock = clock;
    }

    /** 返回已由启动同步写入的系统预设路线。 */
    public List<StudyPlanSummaryResponse> listPlans() {
        return studyPlanMapper.findEnabledPlans().stream().map(this::toSummaryResponse).toList();
    }

    /** 查询路线的逐日内容；日内关联项由数据库中的目标关系返回。 */
    public StudyPlanDetailResponse getPlan(Long planId) {
        StudyPlanRow plan = requirePlan(planId);
        return new StudyPlanDetailResponse(
                plan.id(), plan.code(), plan.name(), plan.durationDays(), plan.description(), findDays(plan.id())
        );
    }

    /** 未选择路线时返回 null，前端应展示路线选择而不是伪造学习状态。 */
    public CurrentPlanResponse getCurrentPlan() {
        Long profileId = requireDefaultProfileId();
        return toCurrentPlan(studyPlanMapper.findActivePlanByProfileId(profileId));
    }

    /**
     * 切换当前路线并开始新的时间进度。
     *
     * <p>锁住 profile 行而不是引入 Redis 锁：学习状态的唯一事实源是 PostgreSQL，且锁范围仅限同一个本地用户。</p>
     */
    @Transactional
    public CurrentPlanResponse activatePlan(Long planId) {
        requirePlan(planId);
        Long profileId = studyPlanMapper.lockDefaultProfileId();
        if (profileId == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "默认学习档案不存在");
        }
        Instant startedAt = Instant.now(clock);
        studyPlanMapper.deactivateActivePlans(profileId);
        studyPlanMapper.activatePlan(profileId, planId, startedAt);
        CurrentPlanResponse currentPlan = toCurrentPlan(studyPlanMapper.findActivePlanByProfileId(profileId));
        LOGGER.info("学习路线已切换, profileId={}, planId={}", profileId, planId);
        return currentPlan;
    }

    /** 返回当前自然日对应的学习项；停学不会被误判为完成，学习完成进度由题目掌握度快照单独表示。 */
    public TodayStudyResponse getTodayStudy() {
        CurrentPlanResponse currentPlan = getCurrentPlan();
        if (currentPlan == null) {
            return null;
        }
        StudyPlanDayRow day = studyPlanMapper.findDayByPlanIdAndDayNumber(
                currentPlan.planId(), currentPlan.timeProgressDay()
        );
        if (day == null) {
            throw new BusinessException(ApiErrorCode.BUSINESS_RULE_VIOLATED, "当前路线缺少当日计划");
        }
        return new TodayStudyResponse(currentPlan, withItems(day));
    }

    private StudyPlanRow requirePlan(Long planId) {
        StudyPlanRow plan = studyPlanMapper.findEnabledPlanById(planId);
        if (plan == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "学习路线不存在或已停用");
        }
        return plan;
    }

    private Long requireDefaultProfileId() {
        Long profileId = studyPlanMapper.findDefaultProfileId();
        if (profileId == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "默认学习档案不存在");
        }
        return profileId;
    }

    private List<StudyPlanDayResponse> findDays(Long planId) {
        return studyPlanMapper.findDaysByPlanId(planId).stream().map(this::withItems).toList();
    }

    private StudyPlanDayResponse withItems(StudyPlanDayRow day) {
        List<StudyPlanItemResponse> items = studyPlanMapper.findItemsByDayId(day.id()).stream()
                .map(this::toItemResponse)
                .toList();
        return new StudyPlanDayResponse(day.id(), day.dayNumber(), day.title(), day.description(), items);
    }

    private CurrentPlanResponse toCurrentPlan(CurrentPlanRow row) {
        if (row == null) {
            return null;
        }
        int timeProgressDay = StudyPlanTimeProgressCalculator.calculate(row.startedAt(), row.durationDays(), clock);
        return new CurrentPlanResponse(
                row.planId(), row.planCode(), row.planName(), row.durationDays(), row.startedAt(), timeProgressDay
        );
    }

    private StudyPlanSummaryResponse toSummaryResponse(StudyPlanRow row) {
        return new StudyPlanSummaryResponse(row.id(), row.code(), row.name(), row.durationDays(), row.description());
    }

    private StudyPlanItemResponse toItemResponse(StudyPlanItemRow row) {
        return new StudyPlanItemResponse(
                row.id(), row.targetType().name(), row.targetId(), row.targetTitle(), row.sortOrder()
        );
    }
}
