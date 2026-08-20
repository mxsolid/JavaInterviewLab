package com.javainterviewlab.source.repository.model;

/** 源码阅读列表数据库投影。 */
public record SourceSnippetSummaryRow(
        Long id,
        String externalKey,
        Long topicId,
        String language,
        String libraryName,
        String versionLabel,
        String title,
        String summary,
        Long annotationCount
) {
}
