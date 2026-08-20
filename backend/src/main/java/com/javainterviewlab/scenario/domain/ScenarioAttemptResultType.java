package com.javainterviewlab.scenario.domain;

/** 场景回答后的自评结果。 */
public enum ScenarioAttemptResultType {

    NEEDS_WORK("需要加强"),
    PARTIAL("部分掌握"),
    SOLID("较熟练");

    private final String description;

    ScenarioAttemptResultType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
