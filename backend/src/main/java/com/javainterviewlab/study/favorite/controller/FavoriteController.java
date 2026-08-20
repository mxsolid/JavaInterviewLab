package com.javainterviewlab.study.favorite.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.study.favorite.dto.FavoriteResponse;
import com.javainterviewlab.study.favorite.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 收藏题目的 HTTP 边界；Controller 只负责请求路由，不承担偏好状态规则。 */
@Tag(name = "收藏", description = "题目收藏与取消收藏")
@RestController
@RequestMapping("/api/study/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    /** 获取当前档案的启用题目收藏列表。 */
    @Operation(summary = "收藏题目列表")
    @GetMapping
    public ApiResponse<List<FavoriteResponse>> list() {
        return ApiResponse.success(favoriteService.listFavoriteQuestions());
    }

    /** 收藏一道启用题目。 */
    @Operation(summary = "收藏题目")
    @PostMapping("/questions/{questionId}")
    public ApiResponse<Void> favorite(@PathVariable Long questionId) {
        favoriteService.favoriteQuestion(questionId);
        return ApiResponse.success(null);
    }

    /** 取消一道题目的收藏。 */
    @Operation(summary = "取消收藏题目")
    @DeleteMapping("/questions/{questionId}")
    public ApiResponse<Void> unfavorite(@PathVariable Long questionId) {
        favoriteService.unfavoriteQuestion(questionId);
        return ApiResponse.success(null);
    }
}
