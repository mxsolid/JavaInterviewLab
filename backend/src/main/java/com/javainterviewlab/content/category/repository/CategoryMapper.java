package com.javainterviewlab.content.category.repository;

import com.javainterviewlab.content.category.repository.model.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 分类表的读写 Mapper，只传递持久化实体，不依赖 HTTP DTO。 */
@Mapper
public interface CategoryMapper {

    /** 按配置顺序读取全部分类。 */
    List<CategoryEntity> findAll();

    /** 按主键读取分类，用于编辑回读和专题归属校验。 */
    CategoryEntity findById(@Param("id") Long id);

    /** 新建分类并返回数据库生成的主键。 */
    Long insert(@Param("entity") CategoryEntity entity);

    /** 更新分类；返回零表示资源已不存在。 */
    int update(@Param("entity") CategoryEntity entity);
}
