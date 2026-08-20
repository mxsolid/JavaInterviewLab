package com.javainterviewlab.lab.repository;

import com.javainterviewlab.lab.repository.model.LabDefinitionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** Lab definition 只读查询。 */
@Mapper
public interface LabDefinitionMapper {

    List<LabDefinitionRow> findEnabled();

    LabDefinitionRow findEnabledByCode(@Param("code") String code);
}
