package com.javainterviewlab.system.e2e;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface E2eProfileMapper {

    Long insertProfile(@Param("displayName") String displayName);

    int activatePlan(@Param("profileId") Long profileId, @Param("planCode") String planCode);
}
