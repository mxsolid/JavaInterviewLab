package com.javainterviewlab.lab.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.lab.dto.LabDefinitionResponse;
import com.javainterviewlab.lab.service.LabDefinitionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** V0.3 算法实验室定义入口。 */
@RestController
@RequestMapping("/api/v1/labs")
public class LabDefinitionController {

    private final LabDefinitionService service;

    public LabDefinitionController(LabDefinitionService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<LabDefinitionResponse>> list() {
        return ApiResponse.success(service.list());
    }

    @GetMapping("/{code}")
    public ApiResponse<LabDefinitionResponse> detail(@PathVariable String code) {
        return ApiResponse.success(service.detail(code));
    }
}
