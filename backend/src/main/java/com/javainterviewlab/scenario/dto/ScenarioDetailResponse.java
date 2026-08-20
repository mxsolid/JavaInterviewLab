package com.javainterviewlab.scenario.dto;

import java.util.List;

/** 场景工作区详情。 */
public record ScenarioDetailResponse(
        Long id,
        String externalKey,
        String title,
        String summary,
        Integer starLevel,
        String sourceVersion,
        List<ScenarioCaseResponse> cases,
        List<ScenarioSolutionResponse> solutions
) {
}
