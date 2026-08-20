package com.javainterviewlab.content.topic.repository;

import com.javainterviewlab.content.topic.repository.model.TopicEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 专题表的读写 Mapper，查询结果保持在 Repository 模型层。 */
@Mapper
public interface TopicMapper {

    /** 按可选分类过滤专题。 */
    List<TopicEntity> findAll(@Param("categoryId") Long categoryId);

    /** 按主键查询专题，用于回读和外键校验。 */
    TopicEntity findById(@Param("id") Long id);

    /** 新建专题并返回生成主键。 */
    Long insert(@Param("entity") TopicEntity entity);

    /** 更新专题；返回零表示记录不存在。 */
    int update(@Param("entity") TopicEntity entity);

    /** 以 COUNT 避免把完整实体加载进只需存在性判断的调用链。 */
    int countById(@Param("id") Long id);
}
