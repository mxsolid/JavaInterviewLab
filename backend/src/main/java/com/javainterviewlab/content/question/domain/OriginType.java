package com.javainterviewlab.content.question.domain;

/**
 * 题目内容来源。
 *
 * <p>来源用于区分系统内容和可编辑内容，不能通过标题或创建时间猜测。</p>
 */
public enum OriginType {

    /** 随系统发布的内置内容。 */
    BUILTIN("系统内置"),

    /** 用户手工创建的内容。 */
    USER("用户创建"),

    /** 从受控种子包导入的内容。 */
    IMPORTED("导入内容"),

    /** 为后续 AI 辅助生成内容预留。 */
    FUTURE_AI("AI 生成");

    private final String description;

    OriginType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
