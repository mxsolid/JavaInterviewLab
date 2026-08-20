package com.javainterviewlab.interview.dto;

import com.javainterviewlab.interview.domain.InterviewMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 创建模拟面试会话。 */
public record CreateInterviewRequest(
        @NotNull InterviewMode mode,
        @Size(max = 120) String topicCode
) {
}
