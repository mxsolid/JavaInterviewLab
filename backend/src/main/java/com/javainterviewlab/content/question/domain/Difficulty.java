package com.javainterviewlab.content.question.domain;

/**
 * 题目难度。
 *
 * <p>难度描述回答所需知识深度，不等同于面试出现频率。</p>
 */
public enum Difficulty {

    /** 基础题，重点是概念和基本使用。 */
    EASY("简单"),

    /** 需要理解原理、场景和常见追问。 */
    MEDIUM("中等"),

    /** 涉及源码、并发边界或多方案权衡。 */
    HARD("困难");

    private final String description;

    Difficulty(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
