package com.javainterviewlab.lab.dto;

import com.fasterxml.jackson.databind.JsonNode;

/** 前端算法模拟所需的稳定元数据，不包含逐帧状态。 */
public record LabDefinitionResponse(
        Long id,
        String code,
        String title,
        String description,
        String algorithm,
        String versionLabel,
        JsonNode initialDataset,
        JsonNode config
) {
}
