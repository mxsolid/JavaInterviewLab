package com.javainterviewlab.content.tag.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.content.tag.dto.*;
import com.javainterviewlab.content.tag.service.TagService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;
    public TagController(TagService tagService) { this.tagService=tagService; }
    @GetMapping public ApiResponse<List<TagResponse>> list() { return ApiResponse.success(tagService.list()); }
    @PostMapping public ApiResponse<TagResponse> create(@Valid @RequestBody TagRequest request) { return ApiResponse.success(tagService.create(request)); }
    @PutMapping("/{id}") public ApiResponse<TagResponse> update(@PathVariable Long id,@Valid @RequestBody TagRequest request) { return ApiResponse.success(tagService.update(id,request)); }
}
