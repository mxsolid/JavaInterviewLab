package com.javainterviewlab.system.seed.repository.model;

/** externalKey 对应题目的来源保护信息。 */
public record SeedQuestionRow(
        Long id,
        String originType,
        String seedPack,
        Long version
) {
}
