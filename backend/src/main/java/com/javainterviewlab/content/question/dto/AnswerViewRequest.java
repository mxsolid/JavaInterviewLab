package com.javainterviewlab.content.question.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** 客户端为一次显式答案披露生成稳定 UUID，网络重试复用同一值。 */
public record AnswerViewRequest(@NotNull UUID clientViewId) {}
