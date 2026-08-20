package com.javainterviewlab.content.question.domain;

/**
 * 题目答案的讲解深度。
 *
 * <p>同一题可按不同时间长度组织回答，避免把面试速答和原理展开混为一段内容。</p>
 */
public enum AnswerType {

    /** 适合开场快速表达的 30 秒回答。 */
    QUICK_30S("30 秒回答"),

    /** 覆盖主要概念、流程和场景的标准回答。 */
    STANDARD("标准回答"),

    /** 用于追问时展开原理和取舍的深入回答。 */
    DEEP("深入原理");

    private final String description;

    AnswerType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
