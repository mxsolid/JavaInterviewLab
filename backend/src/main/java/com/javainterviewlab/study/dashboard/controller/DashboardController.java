package com.javainterviewlab.study.dashboard.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.study.dashboard.dto.DashboardResponse;
import com.javainterviewlab.study.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 学习首页看板 HTTP 边界。 */
@Tag(name = "学习看板", description = "统一学习进度统计")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    /** 获取首页需要的聚合学习数据。 */
    @Operation(summary = "学习进度看板")
    @GetMapping
    public ApiResponse<DashboardResponse> getDashboard() {
        return ApiResponse.success(dashboardService.getDashboard());
    }
}
