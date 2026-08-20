package com.javainterviewlab.content.knowledge.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.content.knowledge.dto.KnowledgeMapResponse;
import com.javainterviewlab.content.knowledge.service.KnowledgeMapService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** V0.3 知识地图版本化入口。 */
@RestController
@RequestMapping("/api/v1/knowledge-map")
public class KnowledgeMapController {

    private final KnowledgeMapService knowledgeMapService;

    public KnowledgeMapController(KnowledgeMapService knowledgeMapService) {
        this.knowledgeMapService = knowledgeMapService;
    }

    @GetMapping
    public ApiResponse<KnowledgeMapResponse> getKnowledgeMap() {
        return ApiResponse.success(knowledgeMapService.getKnowledgeMap());
    }
}
