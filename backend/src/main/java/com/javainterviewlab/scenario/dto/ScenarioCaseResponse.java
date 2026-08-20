package com.javainterviewlab.scenario.dto;

import java.util.List;

/** 场景中的一个可独立作答案例。 */
public record ScenarioCaseResponse(
        Long id,
        String code,
        String title,
        String rootCause,
        String prompt,
        List<String> expectedAnalysis
) {
}
