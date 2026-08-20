package com.javainterviewlab.content.tag.repository.model;

import lombok.Data;

/**
 * 标签持久化实体，对应 tag 表。
 *
 * <p>标签是题目的多对多索引，不直接承载题目内容。</p>
 */
@Data
public class TagEntity {

    /** 标签主键。 */
    private Long id;

    /** 用于导入和接口传递的稳定编码。 */
    private String code;

    /** 标签展示名称。 */
    private String name;
}
