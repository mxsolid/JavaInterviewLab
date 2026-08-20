package com.javainterviewlab.system.status.repository.model;

public record SystemStatusRow(
        String databaseVersion,
        String flywayVersion,
        long questionCount,
        long enabledQuestionCount,
        long scenarioCount,
        long sourceSnippetCount,
        long labCount
) {
}
