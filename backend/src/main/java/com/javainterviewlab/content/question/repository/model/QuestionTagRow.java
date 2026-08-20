package com.javainterviewlab.content.question.repository.model;

/** 题目标签的只读投影，供 Service 组装详情响应。 */
public record QuestionTagRow(Long id, String code, String name) {
}
