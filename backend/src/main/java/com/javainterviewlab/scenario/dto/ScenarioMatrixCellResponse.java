package com.javainterviewlab.scenario.dto;

/** 方案矩阵的一个数据库关系单元。 */
public record ScenarioMatrixCellResponse(
        Long caseId,
        Long solutionId,
        String recommendation,
        String reason,
        Integer sortOrder
) {
}
