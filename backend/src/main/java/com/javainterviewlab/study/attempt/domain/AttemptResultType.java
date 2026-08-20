package com.javainterviewlab.study.attempt.domain;

/** 一次练习的判定结果，是不可变答题历史的一部分。 */
public enum AttemptResultType {

    /** 未能组织出有效回答。 */
    NOT_ANSWERED("不会/未作答"),

    /** 回答结论或核心过程错误。 */
    WRONG("回答错误"),

    /** 回答覆盖部分要点，但仍有缺失。 */
    PARTIAL("部分正确"),

    /** 回答满足当前题目的正确标准。 */
    CORRECT("回答正确");

    private final String description;

    AttemptResultType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
