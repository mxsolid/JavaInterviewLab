package com.javainterviewlab.source.repository.model;

/** 源码阅读详情数据库投影。 */
public record SourceSnippetRow(
        Long id,
        String externalKey,
        Long topicId,
        String language,
        String libraryName,
        String versionLabel,
        String sourcePath,
        String title,
        String summary,
        String codeText,
        Integer startLine,
        Integer endLine,
        String licenseName
) {
}
