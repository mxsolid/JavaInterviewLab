package com.javainterviewlab.content.question.domain;

/**
 * 题目考查形式。
 *
 * <p>类型决定讲解组织方式，不能仅按标题内容在前端自行推断。</p>
 */
public enum QuestionType {

    /** 侧重概念、原理和基本使用。 */
    KNOWLEDGE("知识题"),

    /** 侧重具体业务情境下的判断和处理。 */
    SCENARIO("场景题"),

    /** 侧重设计模式的适用边界和取舍。 */
    DESIGN_PATTERN("设计模式题");

    private final String description;

    QuestionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
