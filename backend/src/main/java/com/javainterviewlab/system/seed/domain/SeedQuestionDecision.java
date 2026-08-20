package com.javainterviewlab.system.seed.domain;

/** 单题在一次 Seed V2 导入中的数据库决策。 */
public enum SeedQuestionDecision {

    CREATE("新增"),
    UPDATE("更新"),
    SKIP("跳过");

    private final String description;

    SeedQuestionDecision(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
