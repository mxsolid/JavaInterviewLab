package com.javainterviewlab.content.category.repository;

import com.javainterviewlab.content.category.dto.CategoryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    @Select("SELECT id, code, name, description, sort_order AS sortOrder, status FROM category ORDER BY sort_order, id")
    List<CategoryResponse> findAll();

    @Select("SELECT id, code, name, description, sort_order AS sortOrder, status FROM category WHERE id = #{id}")
    CategoryResponse findById(@Param("id") Long id);

    @Select("INSERT INTO category(code, name, description, sort_order, status) VALUES(#{code}, #{name}, #{description}, #{sortOrder}, #{status}) RETURNING id")
    Long insert(@Param("code") String code, @Param("name") String name, @Param("description") String description,
                @Param("sortOrder") Integer sortOrder, @Param("status") String status);

    @Update("UPDATE category SET code=#{code}, name=#{name}, description=#{description}, sort_order=#{sortOrder}, status=#{status}, updated_at=CURRENT_TIMESTAMP WHERE id=#{id}")
    int update(@Param("id") Long id, @Param("code") String code, @Param("name") String name,
               @Param("description") String description, @Param("sortOrder") Integer sortOrder, @Param("status") String status);
}
