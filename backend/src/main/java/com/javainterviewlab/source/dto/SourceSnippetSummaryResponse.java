package com.javainterviewlab.source.dto;

/** 源码阅读列表项，不携带 codeText。 */
public record SourceSnippetSummaryResponse(
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
