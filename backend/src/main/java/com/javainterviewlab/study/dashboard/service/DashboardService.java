package com.javainterviewlab.study.dashboard.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.study.dashboard.dto.DashboardResponse;
import com.javainterviewlab.study.dashboard.dto.RecentStudyItemResponse;
import com.javainterviewlab.study.dashboard.repository.DashboardMapper;
import com.javainterviewlab.study.dashboard.repository.model.DashboardStatsRow;
import com.javainterviewlab.study.dashboard.repository.model.RecentStudyRow;
import com.javainterviewlab.study.plan.dto.CurrentPlanResponse;
import com.javainterviewlab.study.plan.dto.TodayStudyResponse;
import com.javainterviewlab.study.plan.service.StudyPlanService;
import com.javainterviewlab.study.profile.repository.StudyProfileMapper;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 首页学习看板服务。
 *
 * <p>统计口径集中在后端，前端只展示，避免同一掌握率在不同页面使用不同公式。</p>
 */
@Service
public class DashboardService {

    private static final int RECENT_STUDY_LIMIT = 8;

    private final DashboardMapper dashboardMapper;
    private final StudyProfileMapper studyProfileMapper;
    private final StudyPlanService studyPlanService;
    private final Clock clock;

    public DashboardService(
            DashboardMapper dashboardMapper,
            StudyProfileMapper studyProfileMapper,
            StudyPlanService studyPlanService,
            Clock clock
    ) {
        this.dashboardMapper = dashboardMapper;
        this.studyProfileMapper = studyProfileMapper;
        this.studyPlanService = studyPlanService;
        this.clock = clock;
    }

    /** 一次返回首页主要数据，减少多个卡片分别发起 HTTP 请求。 */
    public DashboardResponse getDashboard() {
        Long profileId = requireDefaultProfileId();
        DashboardStatsRow stats = dashboardMapper.getStats(profileId);
        CurrentPlanResponse currentPlan = studyPlanService.getCurrentPlan();
        TodayStudyResponse todayStudy = studyPlanService.getTodayStudy();
        LocalDate today = LocalDate.now(clock);
        Instant start = today.atStartOfDay(clock.getZone()).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        long fiveStarTotal = stats.totalFiveStarQuestionCount();
        double fiveStarMasteryRate = fiveStarTotal == 0 ? 0D
                : (double) stats.solidFiveStarQuestionCount() / fiveStarTotal;
        List<RecentStudyItemResponse> recentItems = dashboardMapper.findRecentStudyItems(profileId, RECENT_STUDY_LIMIT)
                .stream().map(this::toRecentResponse).toList();
        return new DashboardResponse(
                currentPlan,
                currentPlan == null ? null : currentPlan.timeProgressDay(),
                currentPlan == null ? null : currentPlan.durationDays(),
                todayStudy == null ? 0 : todayStudy.day().items().size(),
                dashboardMapper.countTodayReviews(profileId, start, end),
                dashboardMapper.countDueReviews(profileId, end),
                stats.totalQuestionCount(),
                stats.touchedQuestionCount(),
                stats.solidQuestionCount(),
                stats.masteredQuestionCount(),
                fiveStarMasteryRate,
                stats.activeWrongQuestionCount(),
                stats.favoriteQuestionCount(),
                recentItems
        );
    }

    private Long requireDefaultProfileId() {
        Long profileId = studyProfileMapper.findDefaultProfileId();
        if (profileId == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "默认学习档案不存在");
        }
        return profileId;
    }

    private RecentStudyItemResponse toRecentResponse(RecentStudyRow row) {
        return new RecentStudyItemResponse(
                row.questionId(), row.title(), row.starLevel(), row.masteryLevel(), row.lastStudiedAt()
        );
    }
}
