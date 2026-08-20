package com.javainterviewlab.scenario.controller;

import com.javainterviewlab.common.api.ApiResponse;
import com.javainterviewlab.scenario.dto.ScenarioAttemptResponse;
import com.javainterviewlab.scenario.dto.ScenarioDetailResponse;
import com.javainterviewlab.scenario.dto.ScenarioMatrixResponse;
import com.javainterviewlab.scenario.dto.ScenarioSummaryResponse;
import com.javainterviewlab.scenario.dto.SubmitScenarioAttemptRequest;
import com.javainterviewlab.scenario.service.ScenarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** V0.3 场景训练、方案矩阵和作答入口。 */
@RestController
@RequestMapping("/api/v1")
public class ScenarioController {

    private final ScenarioService scenarioService;

    public ScenarioController(ScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @GetMapping("/scenarios")
    public ApiResponse<List<ScenarioSummaryResponse>> list() {
        return ApiResponse.success(scenarioService.list());
    }

    @GetMapping("/scenarios/{id}")
    public ApiResponse<ScenarioDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(scenarioService.detail(id));
    }

    @GetMapping("/scenarios/{id}/matrix")
    public ApiResponse<ScenarioMatrixResponse> matrix(@PathVariable Long id) {
        return ApiResponse.success(scenarioService.matrix(id));
    }

    @PostMapping("/scenario-attempts")
    public ApiResponse<ScenarioAttemptResponse> submit(@Valid @RequestBody SubmitScenarioAttemptRequest request) {
        return ApiResponse.success(scenarioService.submit(request));
    }
}
