package com.javainterviewlab.source.dto;

/** 教学片段的行级注释。 */
public record SourceAnnotationResponse(
        Long id,
        Integer lineStart,
        Integer lineEnd,
        String title,
        String explanation
) {
}
