package com.javainterviewlab.study.workbench.service;

import com.javainterviewlab.study.dashboard.service.DashboardService;
import com.javainterviewlab.study.progress.service.StudyProgressService;
import com.javainterviewlab.study.review.service.ReviewService;
import com.javainterviewlab.study.workbench.dto.WorkbenchResponse;
import org.springframework.stereotype.Service;

import java.time.Clock;

/** 工作台只编排已有只读服务，不复制 Dashboard、复习或错题的业务口径。 */
@Service
public class WorkbenchService {

    private final DashboardService dashboardService;
    private final ReviewService reviewService;
    private final StudyProgressService studyProgressService;
    private final Clock clock;

    public WorkbenchService(
            DashboardService dashboardService,
            ReviewService reviewService,
            StudyProgressService studyProgressService,
            Clock clock
    ) {
        this.dashboardService = dashboardService;
        this.reviewService = reviewService;
        this.studyProgressService = studyProgressService;
        this.clock = clock;
    }

    /** 返回首屏需要的真实学习状态，所有数据仍来自 PostgreSQL。 */
    public WorkbenchResponse getWorkbench() {
        return new WorkbenchResponse(
                clock.instant(),
                dashboardService.getDashboard(),
                reviewService.listDue(),
                studyProgressService.listActiveWrongQuestions()
        );
    }
}
