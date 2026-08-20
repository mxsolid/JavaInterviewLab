package com.javainterviewlab.study.review.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.study.review.domain.ReviewTaskStatus;
import com.javainterviewlab.study.review.dto.ReviewTaskResponse;
import com.javainterviewlab.study.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 复习任务 HTTP 边界；完成动作统一由新的答题提交触发，避免双状态源。 */
@Tag(name = "间隔复习", description = "待复习任务查询")
@RestController
@RequestMapping("/api/study/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /** 获取今天到期的待复习任务。 */
    @Operation(summary = "今日复习")
    @GetMapping("/today")
    public ApiResponse<List<ReviewTaskResponse>> today() {
        return ApiResponse.success(reviewService.listToday());
    }

    /** 获取一个明确状态的复习任务列表。 */
    @Operation(summary = "复习任务列表")
    @GetMapping
    public ApiResponse<List<ReviewTaskResponse>> list(
            @RequestParam(defaultValue = "PENDING") ReviewTaskStatus status
    ) {
        return ApiResponse.success(reviewService.listByStatus(status));
    }
}
