package com.javainterviewlab.study.progress.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.study.progress.dto.StudyProgressResponse;
import com.javainterviewlab.study.progress.dto.WrongQuestionResponse;
import com.javainterviewlab.study.progress.service.StudyProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 学习进度 HTTP 边界；错题状态始终落在学习进度快照，不维护题目副本。 */
@Tag(name = "学习进度", description = "单题当前快照与错题状态")
@RestController
@RequestMapping({"/api/study", "/api/v1/study"})
public class StudyProgressController {

    private final StudyProgressService studyProgressService;

    public StudyProgressController(StudyProgressService studyProgressService) {
        this.studyProgressService = studyProgressService;
    }

    /** 获取当前激活的错题摘要。 */
    @Operation(summary = "错题列表")
    @GetMapping("/wrong-questions")
    public ApiResponse<List<WrongQuestionResponse>> listWrongQuestions() {
        return ApiResponse.success(studyProgressService.listActiveWrongQuestions());
    }

    /** 获取单题当前学习快照；从未练习时返回前端可直接展示的默认状态。 */
    @Operation(summary = "单题学习进度")
    @GetMapping("/questions/{questionId}/progress")
    public ApiResponse<StudyProgressResponse> getQuestionProgress(@PathVariable Long questionId) {
        return ApiResponse.success(studyProgressService.getQuestionProgress(questionId));
    }

    /** 将一道错题标记为已解决。 */
    @Operation(summary = "标记错题已解决")
    @PutMapping("/questions/{questionId}/wrong-book/resolve")
    public ApiResponse<Void> resolveWrongBook(@PathVariable Long questionId) {
        studyProgressService.resolveWrongBook(questionId);
        return ApiResponse.success(null);
    }
}
