package com.javainterviewlab.source.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.source.dto.SourceAnnotationResponse;
import com.javainterviewlab.source.dto.SourceSnippetDetailResponse;
import com.javainterviewlab.source.dto.SourceSnippetSummaryResponse;
import com.javainterviewlab.source.repository.SourceSnippetMapper;
import com.javainterviewlab.source.repository.model.SourceSnippetRow;
import org.springframework.stereotype.Service;

import java.util.List;

/** 源码阅读内容查询服务。 */
@Service
public class SourceSnippetService {

    private final SourceSnippetMapper mapper;

    public SourceSnippetService(SourceSnippetMapper mapper) {
        this.mapper = mapper;
    }

    public List<SourceSnippetSummaryResponse> list(Long topicId, String version) {
        return mapper.findSummaries(topicId, version).stream()
                .map(row -> new SourceSnippetSummaryResponse(
                        row.id(), row.externalKey(), row.topicId(), row.language(), row.libraryName(),
                        row.versionLabel(), row.title(), row.summary(), row.annotationCount()
                ))
                .toList();
    }

    public SourceSnippetDetailResponse detail(Long id) {
        SourceSnippetRow row = mapper.findEnabledById(id);
        if (row == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "源码片段不存在或已停用");
        }
        List<SourceAnnotationResponse> annotations = mapper.findAnnotations(id).stream()
                .map(item -> new SourceAnnotationResponse(
                        item.id(), item.lineStart(), item.lineEnd(), item.title(), item.explanation()
                ))
                .toList();
        return new SourceSnippetDetailResponse(
                row.id(), row.externalKey(), row.topicId(), row.language(), row.libraryName(),
                row.versionLabel(), row.sourcePath(), row.title(), row.summary(), row.codeText(),
                row.startLine(), row.endLine(), row.licenseName(), annotations
        );
    }
}
