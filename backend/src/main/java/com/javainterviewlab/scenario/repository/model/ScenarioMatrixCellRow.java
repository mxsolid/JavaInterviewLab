package com.javainterviewlab.scenario.repository.model;

/** case-solution 关系投影。 */
public record ScenarioMatrixCellRow(
        Long caseId,
        Long solutionId,
        String recommendation,
        String reason,
        Integer sortOrder
) {
}
