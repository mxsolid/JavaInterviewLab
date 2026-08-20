package com.javainterviewlab.content.tag.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.content.tag.dto.TagRequest;
import com.javainterviewlab.content.tag.dto.TagResponse;
import com.javainterviewlab.content.tag.service.TagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 标签管理接口。 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /** 查询全部标签。 */
    @GetMapping
    public ApiResponse<List<TagResponse>> list() {
        return ApiResponse.success(tagService.list());
    }

    /** 创建标签。 */
    @PostMapping
    public ApiResponse<TagResponse> create(@Valid @RequestBody TagRequest request) {
        return ApiResponse.success(tagService.create(request));
    }

    /** 更新指定标签。 */
    @PutMapping("/{id}")
    public ApiResponse<TagResponse> update(@PathVariable Long id, @Valid @RequestBody TagRequest request) {
        return ApiResponse.success(tagService.update(id, request));
    }
}
