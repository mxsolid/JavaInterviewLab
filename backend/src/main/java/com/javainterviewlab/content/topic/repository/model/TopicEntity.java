package com.javainterviewlab.content.topic.repository.model;

import com.javainterviewlab.content.shared.ContentStatus;
import lombok.Data;

/**
 * 专题持久化实体，对应 topic 表。
 *
 * <p>categoryName 是专题列表 JOIN 分类表得到的只读展示字段，仍由 Service 转换为 API 响应。</p>
 */
@Data
public class TopicEntity {

    /** 专题主键。 */
    private Long id;

    /** 所属分类主键。 */
    private Long categoryId;

    /** 所属分类名称，仅用于列表查询投影。 */
    private String categoryName;

    /** 供种子和学习路线引用的稳定编码。 */
    private String code;

    /** 专题展示名称。 */
    private String name;

    /** 专题的补充说明。 */
    private String description;

    /** 专题的重要程度。 */
    private Integer starLevel;

    /** 控制专题在所属分类内的显示顺序。 */
    private Integer sortOrder;

    /** 禁用后不再出现在正常学习选择中。 */
    private ContentStatus status;
}
