package com.javainterviewlab.content.question.repository.model;

/** 题目追问的只读投影，供详情接口保持编辑时的排序。 */
public record QuestionFollowUpRow(Long id, String title, String referenceAnswer, Integer sortOrder) {
}
