package com.javainterviewlab.study.progress.domain;

/** 当前掌握度快照，用于避免每次查询都扫描全部答题历史。 */
public enum MasteryLevel {
    /** 无法独立回答。 */ UNKNOWN("不会"),
    /** 见过概念但不能完整组织。 */ SEEN("有印象"),
    /** 能回答基本概念和流程。 */ BASIC("基础掌握"),
    /** 能应对常见追问与场景。 */ SOLID("较熟练"),
    /** 能解释原理和取舍。 */ MASTERED("熟练掌握");
    private final String description;
    MasteryLevel(String description) { this.description = description; }
    public String getDescription() { return description; }
}
