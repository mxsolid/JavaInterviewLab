package com.javainterviewlab.content.topic.dto;

/** 专题接口响应，带分类名称以避免前端为每项专题重复查询分类。 */
public record TopicResponse(Long id, Long categoryId, String categoryName, String code, String name,
                            String description, Integer starLevel, Integer sortOrder, String status) {
}
