package com.javainterviewlab.content.question.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.content.question.dto.AnswerViewRequest;
import com.javainterviewlab.content.question.dto.AnswerViewResponse;
import com.javainterviewlab.content.question.dto.QuestionLearningResponse;
import com.javainterviewlab.content.question.dto.QuestionWorkspaceResponse;
import com.javainterviewlab.content.question.service.QuestionWorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 题目工作区 API；练习首屏不返回答案，披露动作必须显式写库。 */
@Tag(name = "V0.3 题目工作区", description = "练习元数据、学习内容和答案披露")
@RestController
@RequestMapping("/api/v1/questions")
public class QuestionWorkspaceController {

    private final QuestionWorkspaceService questionWorkspaceService;

    public QuestionWorkspaceController(QuestionWorkspaceService questionWorkspaceService) {
        this.questionWorkspaceService = questionWorkspaceService;
    }

    @Operation(summary = "读取练习元数据", description = "不返回答案、讲解或追问参考答案。")
    @GetMapping("/{id}")
    public ApiResponse<QuestionWorkspaceResponse> metadata(@PathVariable Long id) {
        return ApiResponse.success(questionWorkspaceService.metadata(id));
    }

    @Operation(summary = "读取学习模式内容", description = "用户主动进入学习模式后返回完整教学内容。")
    @GetMapping("/{id}/learning")
    public ApiResponse<QuestionLearningResponse> learning(@PathVariable Long id) {
        return ApiResponse.success(questionWorkspaceService.learning(id));
    }

    @Operation(summary = "显式披露参考答案", description = "按 clientViewId 幂等记录披露行为并返回教学内容。")
    @PostMapping("/{id}/answer-view")
    public ApiResponse<AnswerViewResponse> reveal(
            @PathVariable Long id,
            @Valid @RequestBody AnswerViewRequest request
    ) {
        return ApiResponse.success(questionWorkspaceService.reveal(id, request));
    }
}
