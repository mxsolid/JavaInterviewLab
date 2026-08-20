package com.javainterviewlab.lab.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.lab.dto.LabDefinitionResponse;
import com.javainterviewlab.lab.repository.LabDefinitionMapper;
import com.javainterviewlab.lab.repository.model.LabDefinitionRow;
import org.springframework.stereotype.Service;

import java.util.List;

/** Lab definition 元数据服务；动画状态推进由浏览器本地模拟。 */
@Service
public class LabDefinitionService {

    private final LabDefinitionMapper mapper;
    private final ObjectMapper objectMapper;

    public LabDefinitionService(LabDefinitionMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public List<LabDefinitionResponse> list() {
        return mapper.findEnabled().stream().map(this::toResponse).toList();
    }

    public LabDefinitionResponse detail(String code) {
        LabDefinitionRow row = mapper.findEnabledByCode(code);
        if (row == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "Lab 不存在或已停用");
        }
        return toResponse(row);
    }

    private LabDefinitionResponse toResponse(LabDefinitionRow row) {
        try {
            return new LabDefinitionResponse(
                    row.id(), row.code(), row.title(), row.description(), row.algorithm(), row.versionLabel(),
                    objectMapper.readTree(row.initialDatasetJson()), objectMapper.readTree(row.configJson())
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Lab definition JSON 数据损坏", exception);
        }
    }
}
