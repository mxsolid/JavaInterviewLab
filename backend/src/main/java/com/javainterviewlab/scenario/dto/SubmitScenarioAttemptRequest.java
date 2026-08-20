package com.javainterviewlab.scenario.dto;

import com.javainterviewlab.scenario.domain.ScenarioAttemptResultType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** 场景作答提交；clientAttemptId 由客户端生成并用于网络重试幂等。 */
public record SubmitScenarioAttemptRequest(
        @NotNull UUID clientAttemptId,
        @NotNull Long scenarioId,
        Long caseId,
        @NotBlank @Size(max = 20_000) String answerText,
        @Min(1) @Max(5) Integer selfRating,
        @NotNull ScenarioAttemptResultType resultType,
        @Min(0) Integer durationSeconds
) {
}
