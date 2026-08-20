package com.javainterviewlab.scenario.domain;

import java.util.List;

/** 完整校验后的场景种子包。 */
public record ScenarioSeedPack(
        String seedPack,
        String version,
        String checksumSha256,
        List<SeedScenario> scenarios
) {

    public record SeedScenario(
            String externalKey,
            String title,
            int starLevel,
            String summary,
            List<SeedCase> cases,
            List<SeedSolution> solutions
    ) {
    }

    public record SeedCase(
            String code,
            String title,
            String rootCause,
            List<String> candidateSolutions,
            List<String> expectedAnalysis
    ) {
    }

    public record SeedSolution(String code, String name, String principle) {
    }
}
