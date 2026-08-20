package com.javainterviewlab.content.tag.repository;

import com.javainterviewlab.content.tag.repository.model.TagEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 标签表的读写 Mapper，避免接口层响应模型进入持久化层。 */
@Mapper
public interface TagMapper {

    /** 按名称读取全部标签。 */
    List<TagEntity> findAll();

    /** 按主键查询标签。 */
    TagEntity findById(@Param("id") Long id);

    /** 新建标签并返回生成主键。 */
    Long insert(@Param("entity") TagEntity entity);

    /** 更新标签；返回零表示记录不存在。 */
    int update(@Param("entity") TagEntity entity);

    /** 批量校验标签引用，避免逐条查询产生 N+1 访问。 */
    int countByIds(@Param("ids") List<Long> ids);
}
