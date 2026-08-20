package com.javainterviewlab.content.shared;

/**
 * 内容是否可在正常学习流程中使用。
 *
 * <p>禁用保留历史引用和编辑记录，避免物理删除破坏既有题目关系。</p>
 */
public enum ContentStatus {

    /** 内容可被查询、选择和学习。 */
    ENABLED("启用"),

    /** 内容保留在数据库，但不再作为正常可用内容。 */
    DISABLED("禁用");

    private final String description;

    ContentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
