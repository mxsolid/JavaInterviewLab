package com.javainterviewlab.content.topic.dto;

public record TopicResponse(Long id, Long categoryId, String categoryName, String code, String name,
                            String description, Integer starLevel, Integer sortOrder, String status) {
}
