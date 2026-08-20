package com.javainterviewlab.common.api;

import java.util.List;

/** 列表查询统一携带总数，前端无需自行推算是否还有下一页。 */
public record PageResponse<T>(List<T> items, long total, int page, int pageSize) {
}
