package com.javainterviewlab.content.category.repository;

import com.javainterviewlab.content.category.dto.CategoryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    List<CategoryResponse> findAll();

    CategoryResponse findById(@Param("id") Long id);

    Long insert(
            @Param("code") String code,
            @Param("name") String name,
            @Param("description") String description,
            @Param("sortOrder") Integer sortOrder,
            @Param("status") String status
    );

    int update(
            @Param("id") Long id,
            @Param("code") String code,
            @Param("name") String name,
            @Param("description") String description,
            @Param("sortOrder") Integer sortOrder,
            @Param("status") String status
    );
}
