package com.javainterviewlab.source.repository;

import com.javainterviewlab.source.repository.model.SourceAnnotationRow;
import com.javainterviewlab.source.repository.model.SourceSnippetRow;
import com.javainterviewlab.source.repository.model.SourceSnippetSummaryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** Source 阅读只读查询。 */
@Mapper
public interface SourceSnippetMapper {

    List<SourceSnippetSummaryRow> findSummaries(
            @Param("topicId") Long topicId,
            @Param("version") String version
    );

    SourceSnippetRow findEnabledById(@Param("id") Long id);

    List<SourceAnnotationRow> findAnnotations(@Param("sourceSnippetId") Long sourceSnippetId);
}
