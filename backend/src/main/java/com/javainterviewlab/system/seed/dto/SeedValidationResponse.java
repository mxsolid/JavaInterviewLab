package com.javainterviewlab.system.seed.dto;

import java.util.List;

/** Seed V2 校验及 dry-run 结果；计数表示在当前数据库上的预期决策。 */
public record SeedValidationResponse(
        String seedPack,
        String version,
        String checksumSha256,
        String importMode,
        boolean valid,
        int questionCount,
        int created,
        int updated,
        int skipped,
        List<String> warnings
) {
}
