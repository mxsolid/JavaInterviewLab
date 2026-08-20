package com.javainterviewlab.system.status.dto;

import java.time.LocalDateTime;

public record SystemStatusResponse(
        String status,
        String application,
        String databaseVersion,
        String flywayVersion,
        long questionCount,
        long enabledQuestionCount,
        long scenarioCount,
        long sourceSnippetCount,
        long labCount,
        LocalDateTime checkedAt
) {
}
