package com.javainterviewlab.system.status.service;

import com.javainterviewlab.system.status.dto.SystemStatusResponse;
import com.javainterviewlab.system.status.repository.SystemStatusMapper;
import com.javainterviewlab.system.status.repository.model.SystemStatusRow;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SystemStatusService {

    private static final String APPLICATION_NAME = "Java Interview Lab";
    private static final String UP = "UP";

    private final SystemStatusMapper systemStatusMapper;

    public SystemStatusService(SystemStatusMapper systemStatusMapper) {
        this.systemStatusMapper = systemStatusMapper;
    }

    /** 单次只读事务保证所有计数来自同一数据库快照。 */
    @Transactional(readOnly = true)
    public SystemStatusResponse getStatus() {
        SystemStatusRow row = systemStatusMapper.selectStatus();
        return new SystemStatusResponse(
                UP,
                APPLICATION_NAME,
                row.databaseVersion(),
                row.flywayVersion(),
                row.questionCount(),
                row.enabledQuestionCount(),
                row.scenarioCount(),
                row.sourceSnippetCount(),
                row.labCount(),
                LocalDateTime.now()
        );
    }
}
