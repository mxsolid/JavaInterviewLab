package com.javainterviewlab.content.topic.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.content.topic.dto.*;
import com.javainterviewlab.content.topic.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/topics")
public class TopicController {
    private final TopicService topicService;
    public TopicController(TopicService topicService) { this.topicService=topicService; }
    @GetMapping public ApiResponse<List<TopicResponse>> list(@RequestParam(required=false) Long categoryId) { return ApiResponse.success(topicService.list(categoryId)); }
    @PostMapping public ApiResponse<TopicResponse> create(@Valid @RequestBody TopicRequest request) { return ApiResponse.success(topicService.create(request)); }
    @PutMapping("/{id}") public ApiResponse<TopicResponse> update(@PathVariable Long id,@Valid @RequestBody TopicRequest request) { return ApiResponse.success(topicService.update(id,request)); }
}
