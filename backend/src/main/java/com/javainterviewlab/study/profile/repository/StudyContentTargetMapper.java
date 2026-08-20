package com.javainterviewlab.study.profile.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** 学习域校验受控多态内容目标的数据库访问接口。 */
@Mapper
public interface StudyContentTargetMapper {

    /** 校验一个启用题目是否存在。 */
    int countEnabledQuestionById(@Param("targetId") Long targetId);

    /** 校验一个启用专题是否存在。 */
    int countEnabledTopicById(@Param("targetId") Long targetId);
}
