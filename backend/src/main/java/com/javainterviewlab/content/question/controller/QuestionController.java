package com.javainterviewlab.content.question.controller;

import com.javainterviewlab.common.api.*;
import com.javainterviewlab.content.question.dto.*;
import com.javainterviewlab.content.question.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/questions")
public class QuestionController {
    private final QuestionService questionService;
    public QuestionController(QuestionService questionService) { this.questionService=questionService; }
    @GetMapping public ApiResponse<PageResponse<QuestionSummaryResponse>> list(@Valid QuestionQuery query) { return ApiResponse.success(questionService.list(query)); }
    @GetMapping("/{id}") public ApiResponse<QuestionDetailResponse> detail(@PathVariable Long id) { return ApiResponse.success(questionService.detail(id)); }
    @PostMapping public ApiResponse<QuestionDetailResponse> create(@Valid @RequestBody QuestionRequest request) { return ApiResponse.success(questionService.create(request)); }
    @PutMapping("/{id}") public ApiResponse<QuestionDetailResponse> update(@PathVariable Long id,@Valid @RequestBody QuestionRequest request) { return ApiResponse.success(questionService.update(id,request)); }
}
