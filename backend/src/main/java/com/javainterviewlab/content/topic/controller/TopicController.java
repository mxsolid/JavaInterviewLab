package com.javainterviewlab.content.topic.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.content.topic.dto.TopicRequest;
import com.javainterviewlab.content.topic.dto.TopicResponse;
import com.javainterviewlab.content.topic.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 专题管理接口。 */
@RestController
@RequestMapping("/api/topics")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public ApiResponse<List<TopicResponse>> list(@RequestParam(required = false) Long categoryId) {
        return ApiResponse.success(topicService.list(categoryId));
    }

    @PostMapping
    public ApiResponse<TopicResponse> create(@Valid @RequestBody TopicRequest request) {
        return ApiResponse.success(topicService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<TopicResponse> update(@PathVariable Long id, @Valid @RequestBody TopicRequest request) {
        return ApiResponse.success(topicService.update(id, request));
    }
}
