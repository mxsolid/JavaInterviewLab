package com.javainterviewlab.study.progress.dto;

import java.time.Instant;

/** 当前错题列表响应，保留错误次数和最近练习时间以辅助用户安排复习。 */
public record WrongQuestionResponse(
        Long questionId,
        String title,
        Integer starLevel,
        String masteryLevel,
        Integer attemptCount,
        Integer wrongCount,
        Instant lastStudiedAt
) {
}
