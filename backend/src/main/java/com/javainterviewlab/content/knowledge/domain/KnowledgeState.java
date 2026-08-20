package com.javainterviewlab.content.knowledge.domain;

/** 专题在当前学习档案下的聚合状态。 */
public enum KnowledgeState {

    NOT_STARTED("未开始"),
    LEARNING("学习中"),
    MASTERED("已掌握");

    private final String description;

    KnowledgeState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
