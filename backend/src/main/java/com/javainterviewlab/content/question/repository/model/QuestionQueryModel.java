package com.javainterviewlab.content.question.repository.model;

import com.javainterviewlab.content.question.domain.Difficulty;
import com.javainterviewlab.content.question.domain.FrequencyLevel;
import com.javainterviewlab.content.shared.ContentStatus;

/**
 * 题目列表的持久化查询条件。
 *
 * <p>分页已在 Service 归一化，Mapper 不再依赖 HTTP 查询参数对象。</p>
 */
public record QuestionQueryModel(
        String keyword,
        Long categoryId,
        Long topicId,
        Integer starLevel,
        Difficulty difficulty,
        FrequencyLevel frequencyLevel,
        ContentStatus status,
        int limit,
        int offset
) {
}
