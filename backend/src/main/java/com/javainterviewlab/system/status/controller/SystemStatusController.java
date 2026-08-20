package com.javainterviewlab.system.status.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.system.status.dto.SystemStatusResponse;
import com.javainterviewlab.system.status.service.SystemStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "System Status")
@RestController
@RequestMapping("/api/v1/system/status")
public class SystemStatusController {

    private final SystemStatusService systemStatusService;

    public SystemStatusController(SystemStatusService systemStatusService) {
        this.systemStatusService = systemStatusService;
    }

    @Operation(summary = "读取应用、数据库、Flyway 和生产内容状态")
    @GetMapping
    public ApiResponse<SystemStatusResponse> getStatus() {
        return ApiResponse.success(systemStatusService.getStatus());
    }
}
