package com.javainterviewlab.study.note.repository;

import com.javainterviewlab.common.content.ContentTargetType;
import com.javainterviewlab.study.note.repository.model.NoteEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 用户笔记的数据库访问接口；更新必须带 version，不能静默覆盖。 */
@Mapper
public interface NoteMapper {

    /** 按档案和受控内容目标读取当前笔记。 */
    NoteEntity findByProfileAndTarget(
            @Param("profileId") Long profileId,
            @Param("targetType") ContentTargetType targetType,
            @Param("targetId") Long targetId
    );

    /** 原子尝试新建笔记；唯一键冲突时不写入并返回 null。 */
    Long insertIgnore(@Param("entity") NoteEntity entity);

    /** 仅当版本匹配时更新；返回 0 时由 Service 区分不存在和版本冲突。 */
    int updateIfVersionMatches(@Param("entity") NoteEntity entity);

    /** 辅助区分笔记不存在和乐观锁冲突。 */
    int countById(@Param("id") Long id);

    /** 按主键回读更新后的笔记。 */
    NoteEntity findById(@Param("id") Long id);
}
