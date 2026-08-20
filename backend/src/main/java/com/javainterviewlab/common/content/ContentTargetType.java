package com.javainterviewlab.common.content;

/**
 * 可被学习状态关联的内容目标类型。
 *
 * <p>收藏和笔记复用该类型，避免各学习模块各自定义名称相同但协议不一致的枚举。</p>
 */
public enum ContentTargetType {

    /** 题库中的一道题目。 */
    QUESTION("题目"),

    /** 题库中的一个专题。 */
    TOPIC("专题"),

    /** V0.3 场景训练中的一个场景。 */
    SCENARIO("场景");

    private final String description;

    ContentTargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
