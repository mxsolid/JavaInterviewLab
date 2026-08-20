package com.javainterviewlab.study.review.domain;

/** 复习任务状态；已完成和已取消任务保留为可追溯历史。 */
public enum ReviewTaskStatus {

    /** 到期后需要学习者处理。 */
    PENDING("待复习"),

    /** 已通过一次新的答题提交完成。 */
    COMPLETED("已完成"),

    /** 因业务规则调整而取消；当前应用不主动产生该状态。 */
    CANCELLED("已取消");

    private final String description;

    ReviewTaskStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
