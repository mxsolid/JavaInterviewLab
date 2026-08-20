package com.javainterviewlab.content.category.dto;

public record CategoryResponse(Long id, String code, String name, String description, Integer sortOrder, String status) {
}
