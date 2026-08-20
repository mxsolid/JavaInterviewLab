package com.javainterviewlab.source.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.source.dto.SourceSnippetDetailResponse;
import com.javainterviewlab.source.dto.SourceSnippetSummaryResponse;
import com.javainterviewlab.source.service.SourceSnippetService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** V0.3 源码阅读入口。 */
@RestController
@RequestMapping("/api/v1/source-snippets")
public class SourceSnippetController {

    private final SourceSnippetService service;

    public SourceSnippetController(SourceSnippetService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<SourceSnippetSummaryResponse>> list(
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) String version
    ) {
        return ApiResponse.success(service.list(topicId, version));
    }

    @GetMapping("/{id}")
    public ApiResponse<SourceSnippetDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(service.detail(id));
    }
}
