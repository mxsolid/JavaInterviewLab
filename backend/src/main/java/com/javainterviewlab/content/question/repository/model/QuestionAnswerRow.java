package com.javainterviewlab.content.question.repository.model;

/** 题目答案的只读投影，保留排序以稳定展示层级。 */
public record QuestionAnswerRow(String answerType, String content, Integer sortOrder) {
}
