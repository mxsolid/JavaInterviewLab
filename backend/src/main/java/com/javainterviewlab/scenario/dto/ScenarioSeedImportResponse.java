package com.javainterviewlab.scenario.dto;

/** 场景种子导入结果。 */
public record ScenarioSeedImportResponse(
        String seedPack,
        String version,
        String checksumSha256,
        int scenarioCount,
        int caseCount,
        int solutionCount,
        boolean duplicated
) {
}
