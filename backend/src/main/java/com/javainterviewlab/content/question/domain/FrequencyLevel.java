package com.javainterviewlab.content.question.domain;

/**
 * 题目在面试中的出现频度。
 *
 * <p>频度用于学习排序，不替代难度判断。</p>
 */
public enum FrequencyLevel {

    /** 出现较少，适合作为拓展内容。 */
    LOW("低频"),

    /** 在部分岗位中会出现。 */
    MEDIUM("中频"),

    /** 常见面试题，应优先掌握。 */
    HIGH("高频"),

    /** 几乎所有相关岗位都会考查。 */
    VERY_HIGH("超高频");

    private final String description;

    FrequencyLevel(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
