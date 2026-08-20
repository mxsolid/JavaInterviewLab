package com.javainterviewlab.content.tag.repository;

import com.javainterviewlab.content.tag.dto.TagResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {

    List<TagResponse> findAll();

    TagResponse findById(@Param("id") Long id);

    Long insert(@Param("code") String code, @Param("name") String name);

    int update(
            @Param("id") Long id,
            @Param("code") String code,
            @Param("name") String name
    );

    /** 批量校验标签引用，避免逐条查询产生 N+1 访问。 */
    int countByIds(@Param("ids") List<Long> ids);
}
