package com.javainterviewlab.content.tag.repository;

import com.javainterviewlab.content.tag.dto.TagResponse;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TagMapper {
    @Select("SELECT id,code,name FROM tag ORDER BY name,id") List<TagResponse> findAll();
    @Select("SELECT id,code,name FROM tag WHERE id=#{id}") TagResponse findById(@Param("id") Long id);
    @Select("INSERT INTO tag(code,name) VALUES(#{code},#{name}) RETURNING id") Long insert(@Param("code") String code,@Param("name") String name);
    @Update("UPDATE tag SET code=#{code},name=#{name} WHERE id=#{id}") int update(@Param("id") Long id,@Param("code") String code,@Param("name") String name);
    @Select("<script>SELECT COUNT(1) FROM tag WHERE id IN <foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach></script>") int countByIds(@Param("ids") List<Long> ids);
}
