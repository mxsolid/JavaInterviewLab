package com.javainterviewlab.source.repository.model;

/** 源码注释数据库投影。 */
public record SourceAnnotationRow(
        Long id,
        Integer lineStart,
        Integer lineEnd,
        String title,
        String explanation
) {
}
