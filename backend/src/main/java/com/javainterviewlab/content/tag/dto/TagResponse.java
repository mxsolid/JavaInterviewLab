package com.javainterviewlab.content.tag.dto;

/** 标签接口响应，题目详情只引用这三个稳定字段。 */
public record TagResponse(Long id, String code, String name) {
}
