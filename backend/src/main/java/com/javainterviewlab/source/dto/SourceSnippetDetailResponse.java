package com.javainterviewlab.source.dto;

import java.util.List;

/** 源码阅读详情；内容只包含合规短摘录或本项目自写伪代码。 */
public record SourceSnippetDetailResponse(
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
        String licenseName,
        List<SourceAnnotationResponse> annotations
) {
}
