package com.javainterviewlab.content.question.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.common.api.PageResponse;
import com.javainterviewlab.content.question.dto.QuestionCreateRequest;
import com.javainterviewlab.content.question.dto.QuestionDetailResponse;
import com.javainterviewlab.content.question.dto.QuestionQuery;
import com.javainterviewlab.content.question.dto.QuestionSummaryResponse;
import com.javainterviewlab.content.question.dto.QuestionUpdateRequest;
import com.javainterviewlab.content.question.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 负责题目查询和编辑的 HTTP 边界，不直接访问 Mapper。 */
@Tag(name = "题库", description = "题目、答案和追问管理")
@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /** 支持关键字、分类、专题、星级、难度、高频度和状态的分页查询。 */
    @Operation(summary = "分页查询题目", description = "未传 page 和 pageSize 时分别默认为 1 和 20。")
    @GetMapping
    public ApiResponse<PageResponse<QuestionSummaryResponse>> list(@Valid QuestionQuery query) {
        return ApiResponse.success(questionService.list(query));
    }

    /** 返回题目正文、答案层级、追问和标签；题目不存在时返回 404。 */
    @Operation(summary = "查询题目详情", description = "返回完整学习内容；不存在时返回 404。")
    @GetMapping("/{id}")
    public ApiResponse<QuestionDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(questionService.detail(id));
    }

    /** 新增题目，创建请求不需要 version。 */
    @Operation(summary = "创建题目", description = "专题和标签必须存在；同一种答案只能提交一次。")
    @PostMapping
    public ApiResponse<QuestionDetailResponse> create(@Valid @RequestBody QuestionCreateRequest request) {
        return ApiResponse.success(questionService.create(request));
    }

    /** 使用 version 条件更新题目；旧版本保存返回 409。 */
    @Operation(summary = "修改题目", description = "version 过期时返回 409，调用方应先重新读取详情。")
    @PutMapping("/{id}")
    public ApiResponse<QuestionDetailResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody QuestionUpdateRequest request
    ) {
        return ApiResponse.success(questionService.update(id, request));
    }
}
