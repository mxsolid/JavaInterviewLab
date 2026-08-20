package com.javainterviewlab.content.topic.repository;

import com.javainterviewlab.content.topic.dto.TopicResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TopicMapper {

    List<TopicResponse> findAll(@Param("categoryId") Long categoryId);

    TopicResponse findById(@Param("id") Long id);

    Long insert(
            @Param("categoryId") Long categoryId,
            @Param("code") String code,
            @Param("name") String name,
            @Param("description") String description,
            @Param("starLevel") Integer starLevel,
            @Param("sortOrder") Integer sortOrder,
            @Param("status") String status
    );

    int update(
            @Param("id") Long id,
            @Param("categoryId") Long categoryId,
            @Param("code") String code,
            @Param("name") String name,
            @Param("description") String description,
            @Param("starLevel") Integer starLevel,
            @Param("sortOrder") Integer sortOrder,
            @Param("status") String status
    );

    int countById(@Param("id") Long id);
}
