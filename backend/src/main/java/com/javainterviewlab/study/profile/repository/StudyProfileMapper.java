package com.javainterviewlab.study.profile.repository;

import org.apache.ibatis.annotations.Mapper;

/** 学习档案的最小读取边界，供学习子模块定位本地默认档案。 */
@Mapper
public interface StudyProfileMapper {

    /** 返回唯一默认学习档案；初始化异常时调用方必须给出受控错误。 */
    Long findDefaultProfileId();
}
