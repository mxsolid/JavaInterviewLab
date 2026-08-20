package com.javainterviewlab.study.plan.domain;

/**
 * 学习路线项指向的内容类型。
 *
 * <p>路线 JSON 和数据库都使用枚举名称作为协议值，避免 Bootstrap 用散落字符串决定目标解析逻辑。</p>
 */
public enum StudyPlanTargetType {

    /** 路线项指向一个专题。 */
    TOPIC("专题"),

    /** 路线项指向一道题目。 */
    QUESTION("题目"),

    /** 为尚未开放的场景内容预留，当前目录不会解析到具体内容。 */
    SCENARIO("场景");

    private final String description;

    StudyPlanTargetType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 把路线 JSON 中的协议值转换为受控枚举。
     *
     * <p>未知类型必须在启动同步时失败，不能静默跳过并制造看似完整的错误路线。</p>
     */
    public static StudyPlanTargetType fromCatalogValue(String value) {
        try {
            return StudyPlanTargetType.valueOf(value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalStateException("学习路线包含不支持的 targetType: " + value, exception);
        }
    }
}
