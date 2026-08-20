package com.javainterviewlab.study.workbench.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.study.workbench.dto.WorkbenchResponse;
import com.javainterviewlab.study.workbench.service.WorkbenchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** V0.3 工作台版本化入口；旧 `/api/dashboard` 在 V0.3 验收前继续保留。 */
@RestController
@RequestMapping("/api/v1/workbench")
public class WorkbenchController {

    private final WorkbenchService workbenchService;

    public WorkbenchController(WorkbenchService workbenchService) {
        this.workbenchService = workbenchService;
    }

    @GetMapping
    public ApiResponse<WorkbenchResponse> getWorkbench() {
        return ApiResponse.success(workbenchService.getWorkbench());
    }
}
