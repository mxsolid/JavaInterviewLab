package com.javainterviewlab.system.status.repository;

import com.javainterviewlab.system.status.repository.model.SystemStatusRow;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemStatusMapper {

    SystemStatusRow selectStatus();
}
