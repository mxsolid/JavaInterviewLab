package com.javainterviewlab.system.seed.repository.model;

import java.time.Instant;

/** 已成功导入的题库版本投影。 */
public record SeedHistoryRow(
        String seedPack,
        String version,
        String checksumSha256,
        String importMode,
        Instant importedAt
) {
}
