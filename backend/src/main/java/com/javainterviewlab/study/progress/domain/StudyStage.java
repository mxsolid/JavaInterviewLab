package com.javainterviewlab.study.progress.domain;

/** 题目在当前学习流程中的阶段；完成不表示永远不会遗忘。 */
public enum StudyStage {
    /** 尚未开始系统学习。 */ PREVIEW("预习"),
    /** 正在阅读和理解内容。 */ LEARNING("学习中"),
    /** 已产生答题历史。 */ PRACTICING("练习中"),
    /** 进入后续复习流程。 */ REVIEWING("复习中"),
    /** 达到当前版本的完成标准。 */ COMPLETED("达到当前完成标准");
    private final String description;
    StudyStage(String description) { this.description = description; }
    public String getDescription() { return description; }
}
