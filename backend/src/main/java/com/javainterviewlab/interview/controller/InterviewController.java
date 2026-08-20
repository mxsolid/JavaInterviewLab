package com.javainterviewlab.interview.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.interview.dto.CreateInterviewRequest;
import com.javainterviewlab.interview.dto.InterviewFinishResponse;
import com.javainterviewlab.interview.dto.InterviewSessionResponse;
import com.javainterviewlab.interview.dto.InterviewTurnResponse;
import com.javainterviewlab.interview.dto.SubmitInterviewTurnRequest;
import com.javainterviewlab.interview.service.InterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** V0.3 本地规则模拟面试入口。 */
@Tag(name = "模拟面试", description = "数据库抽题、规则评分与会话持久化")
@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private final InterviewService service;

    public InterviewController(InterviewService service) {
        this.service = service;
    }

    @Operation(summary = "创建面试会话")
    @PostMapping
    public ApiResponse<InterviewSessionResponse> create(@Valid @RequestBody CreateInterviewRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @Operation(summary = "提交单轮回答并返回规则评分")
    @PostMapping("/{id}/turns")
    public ApiResponse<InterviewTurnResponse> submit(
            @PathVariable Long id,
            @Valid @RequestBody SubmitInterviewTurnRequest request
    ) {
        return ApiResponse.success(service.submit(id, request));
    }

    @Operation(summary = "结束面试并汇总多轮评分")
    @PostMapping("/{id}/finish")
    public ApiResponse<InterviewFinishResponse> finish(@PathVariable Long id) {
        return ApiResponse.success(service.finish(id));
    }
}
