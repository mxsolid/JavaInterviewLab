package com.javainterviewlab.content.topic.repository;

import com.javainterviewlab.content.topic.dto.TopicResponse;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TopicMapper {
    @Select("<script>SELECT t.id, t.category_id AS categoryId, c.name AS categoryName, t.code, t.name, t.description, t.star_level AS starLevel, t.sort_order AS sortOrder, t.status FROM topic t JOIN category c ON c.id=t.category_id <where><if test='categoryId != null'>t.category_id=#{categoryId}</if></where> ORDER BY t.sort_order,t.id</script>")
    List<TopicResponse> findAll(@Param("categoryId") Long categoryId);
    @Select("SELECT t.id, t.category_id AS categoryId, c.name AS categoryName, t.code, t.name, t.description, t.star_level AS starLevel, t.sort_order AS sortOrder, t.status FROM topic t JOIN category c ON c.id=t.category_id WHERE t.id=#{id}")
    TopicResponse findById(@Param("id") Long id);
    @Select("INSERT INTO topic(category_id, code, name, description, star_level, sort_order, status) VALUES(#{categoryId},#{code},#{name},#{description},#{starLevel},#{sortOrder},#{status}) RETURNING id")
    Long insert(@Param("categoryId") Long categoryId, @Param("code") String code, @Param("name") String name, @Param("description") String description, @Param("starLevel") Integer starLevel, @Param("sortOrder") Integer sortOrder, @Param("status") String status);
    @Update("UPDATE topic SET category_id=#{categoryId},code=#{code},name=#{name},description=#{description},star_level=#{starLevel},sort_order=#{sortOrder},status=#{status},updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int update(@Param("id") Long id, @Param("categoryId") Long categoryId, @Param("code") String code, @Param("name") String name, @Param("description") String description, @Param("starLevel") Integer starLevel, @Param("sortOrder") Integer sortOrder, @Param("status") String status);
    @Select("SELECT COUNT(1) FROM topic WHERE id=#{id}") int countById(@Param("id") Long id);
}
