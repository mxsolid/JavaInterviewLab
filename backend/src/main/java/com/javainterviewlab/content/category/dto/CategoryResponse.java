package com.javainterviewlab.content.category.dto;

/** 分类接口响应，只暴露前端展示和选择所需字段。 */
public record CategoryResponse(Long id, String code, String name, String description, Integer sortOrder, String status) {
}
