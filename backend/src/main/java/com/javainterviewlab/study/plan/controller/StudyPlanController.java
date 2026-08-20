package com.javainterviewlab.study.plan.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.study.plan.dto.CurrentPlanResponse;
import com.javainterviewlab.study.plan.dto.StudyPlanDetailResponse;
import com.javainterviewlab.study.plan.dto.StudyPlanSummaryResponse;
import com.javainterviewlab.study.plan.dto.TodayStudyResponse;
import com.javainterviewlab.study.plan.service.StudyPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 学习路线 HTTP 边界，只负责请求转发与响应包装。 */
@Tag(name = "学习路线", description = "系统预设路线、当前路线和每日学习任务")
@RestController
@RequestMapping("/api/study")
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    public StudyPlanController(StudyPlanService studyPlanService) {
        this.studyPlanService = studyPlanService;
    }

    /** 查询 10 天、15 天和 30 天系统预设路线。 */
    @Operation(summary = "查询学习路线", description = "系统路线从 PostgreSQL 读取，不由前端硬编码。")
    @GetMapping("/plans")
    public ApiResponse<List<StudyPlanSummaryResponse>> listPlans() {
        return ApiResponse.success(studyPlanService.listPlans());
    }

    /** 查询一条路线的所有日计划和已关联的题目或专题。 */
    @Operation(summary = "查询学习路线详情", description = "包含逐日学习主题与关联内容。")
    @GetMapping("/plans/{planId}")
    public ApiResponse<StudyPlanDetailResponse> getPlan(@PathVariable Long planId) {
        return ApiResponse.success(studyPlanService.getPlan(planId));
    }

    /** 选择路线后开始新的自然时间进度；不会把此前学习记录当作完成。 */
    @Operation(summary = "选择学习路线", description = "同一学习档案同时只有一条当前路线。")
    @PostMapping("/plans/{planId}/activate")
    public ApiResponse<CurrentPlanResponse> activatePlan(@PathVariable Long planId) {
        return ApiResponse.success(studyPlanService.activatePlan(planId));
    }

    /** 查询当前路线；首次使用尚未选择路线时 data 为 null。 */
    @Operation(summary = "查询当前学习路线", description = "返回自然时间进度，不表示知识已经完成。")
    @GetMapping("/current-plan")
    public ApiResponse<CurrentPlanResponse> getCurrentPlan() {
        return ApiResponse.success(studyPlanService.getCurrentPlan());
    }

    /** 查询当前路线对应的当日任务；未选择路线时 data 为 null。 */
    @Operation(summary = "查询今日学习任务", description = "按路线开始日期计算 Day N，不将中断天数视为已学习。")
    @GetMapping("/today")
    public ApiResponse<TodayStudyResponse> getTodayStudy() {
        return ApiResponse.success(studyPlanService.getTodayStudy());
    }
}
