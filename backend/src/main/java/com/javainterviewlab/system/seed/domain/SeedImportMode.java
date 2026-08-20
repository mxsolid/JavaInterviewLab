package com.javainterviewlab.system.seed.domain;

/** 题库包遇到已存在 externalKey 时的处理方式。 */
public enum SeedImportMode {

    /** 只新增不存在的题目，已有非用户题目保持不变。 */
    INSERT_ONLY("仅新增"),

    /** 更新受控的内置或同命名空间导入题，并替换其答案、追问和标签快照。 */
    UPSERT("受控更新");

    private final String description;

    SeedImportMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
