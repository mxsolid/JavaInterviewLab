package com.javainterviewlab.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** 单轮回答提交；clientTurnId 由客户端生成，用于网络重试幂等。 */
public record SubmitInterviewTurnRequest(
        @NotNull UUID clientTurnId,
        @NotBlank @Size(max = 20_000) String answerText
) {
}
