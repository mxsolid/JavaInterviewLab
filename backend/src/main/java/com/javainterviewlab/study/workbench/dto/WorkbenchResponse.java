package com.javainterviewlab.study.workbench.dto;

import com.javainterviewlab.study.dashboard.dto.DashboardResponse;
import com.javainterviewlab.study.progress.dto.WrongQuestionResponse;
import com.javainterviewlab.study.review.dto.ReviewTaskResponse;

import java.time.Instant;
import java.util.List;

/**
 * V0.3 首页工作台契约。
 *
 * <p>将原首页的 Dashboard、待复习和错题三个 HTTP 请求收敛为一个稳定入口。
 * overview 保持既有统计口径，避免新旧首页出现两套计算规则。</p>
 */
public record WorkbenchResponse(
        Instant generatedAt,
        DashboardResponse overview,
        List<ReviewTaskResponse> dueReviews,
        List<WrongQuestionResponse> wrongQuestions
) {
}
