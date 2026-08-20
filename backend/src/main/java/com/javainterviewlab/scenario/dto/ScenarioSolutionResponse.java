package com.javainterviewlab.scenario.dto;

/** 场景候选方案及其适用边界。 */
public record ScenarioSolutionResponse(
        Long id,
        String code,
        String name,
        String principle,
        String pros,
        String cons,
        String boundary
) {
}
