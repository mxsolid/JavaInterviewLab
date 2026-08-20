package com.javainterviewlab.content.knowledge.repository;

import com.javainterviewlab.content.knowledge.repository.model.KnowledgeTopicRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 知识地图只读聚合查询，避免逐专题查询进度形成 N+1。 */
@Mapper
public interface KnowledgeMapMapper {

    /** 一次返回启用分类、专题及当前档案的题目掌握统计。 */
    List<KnowledgeTopicRow> findKnowledgeTopics(@Param("profileId") Long profileId);
}
