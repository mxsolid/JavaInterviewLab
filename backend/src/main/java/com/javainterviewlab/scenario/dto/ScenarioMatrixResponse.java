package com.javainterviewlab.scenario.dto;

import java.util.List;

/** 方案矩阵 read model，前端不需要维护 case-solution 对照常量。 */
public record ScenarioMatrixResponse(
        Long scenarioId,
        List<ScenarioCaseResponse> cases,
        List<ScenarioSolutionResponse> solutions,
        List<ScenarioMatrixCellResponse> cells
) {
}
