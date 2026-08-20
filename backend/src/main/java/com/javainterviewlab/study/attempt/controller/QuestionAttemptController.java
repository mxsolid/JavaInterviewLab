package com.javainterviewlab.study.attempt.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.study.attempt.dto.SubmitAttemptRequest;
import com.javainterviewlab.study.attempt.dto.SubmitAttemptResponse;
import com.javainterviewlab.study.attempt.service.StudySubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 答题历史 HTTP 边界，只接受一次练习提交，不提供历史修改或删除接口。 */
@Tag(name = "练习记录", description = "追加答题历史并按客户端 UUID 幂等")
@RestController
@RequestMapping({"/api/study/attempts", "/api/v1/study/attempts"})
public class QuestionAttemptController {

    private final StudySubmissionService studySubmissionService;

    public QuestionAttemptController(StudySubmissionService studySubmissionService) {
        this.studySubmissionService = studySubmissionService;
    }

    /** 提交一次练习；重复的 clientAttemptId 返回此前保存的同一条历史。 */
    @Operation(summary = "提交答题历史", description = "相同 clientAttemptId 的网络重试不会产生第二条答题记录。")
    @PostMapping
    public ApiResponse<SubmitAttemptResponse> submit(@Valid @RequestBody SubmitAttemptRequest request) {
        return ApiResponse.success(studySubmissionService.submit(request));
    }
}
