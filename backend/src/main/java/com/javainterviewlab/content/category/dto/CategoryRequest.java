package com.javainterviewlab.content.category.dto;

import com.javainterviewlab.content.shared.ContentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 分类创建和更新请求。
 *
 * <p>排序和状态允许省略，默认值在 DTO 归一化，保证 Service 不依赖前端是否传空字段。</p>
 */
public record CategoryRequest(
    @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,63}") String code,
    @NotBlank @Size(max = 100) String name,
    @Size(max = 2000) String description,
    @Min(0) @Max(10000) Integer sortOrder,
    ContentStatus status
) {
    public int effectiveSortOrder() {
        return sortOrder == null ? 0 : sortOrder;
    }

    public ContentStatus effectiveStatus() {
        return status == null ? ContentStatus.ENABLED : status;
    }
}
