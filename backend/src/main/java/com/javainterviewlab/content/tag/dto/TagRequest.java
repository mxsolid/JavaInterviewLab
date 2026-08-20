package com.javainterviewlab.content.tag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** 标签创建和更新请求，编码用于稳定导入，名称用于前端展示。 */
public record TagRequest(
        @NotBlank @Pattern(regexp = "[a-z][a-z0-9-]{1,63}") String code,
        @NotBlank @Size(max = 100) String name
) {
}
