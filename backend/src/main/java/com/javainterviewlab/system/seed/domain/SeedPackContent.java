package com.javainterviewlab.system.seed.domain;

import java.util.List;
import java.util.Map;

/** 完整校验后的 Seed V2 内容模型；只有该模型可进入数据库决策和写入阶段。 */
public record SeedPackContent(
        String seedPack,
        String version,
        String checksumSha256,
        SeedImportMode importMode,
        Map<String, String> categories,
        List<SeedTopic> topics,
        List<SeedQuestion> questions,
        List<String> warnings
) {

    /** Seed 中的专题定义。 */
    public record SeedTopic(
            String externalKey,
            String categoryCode,
            String name,
            int starLevel
    ) {
    }

    /** Seed 中的题目聚合，children 在 UPSERT 时按该快照整体替换。 */
    public record SeedQuestion(
            String externalKey,
            String topicCode,
            String title,
            int starLevel,
            String difficulty,
            String frequencyLevel,
            String questionType,
            String originType,
            String status,
            String oneLiner,
            String plainExplanation,
            String designReason,
            String commonMistakes,
            String scorePoints,
            Map<String, String> answers,
            List<SeedFollowUp> followUps,
            List<String> tags,
            String sourceVersion
    ) {
    }

    /** 追问标题和可选参考答案；旧包的字符串追问不会被伪造答案。 */
    public record SeedFollowUp(String title, String referenceAnswer) {
    }
}
