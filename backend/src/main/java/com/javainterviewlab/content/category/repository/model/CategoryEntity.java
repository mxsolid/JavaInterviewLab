package com.javainterviewlab.content.category.repository.model;

import com.javainterviewlab.content.shared.ContentStatus;
import lombok.Data;

/**
 * 分类持久化实体，对应 category 表。
 *
 * <p>该对象只在 Repository 和 Service 间流转，避免数据库字段变化直接影响分类 API 契约。</p>
 */
@Data
public class CategoryEntity {

    /** 分类主键。 */
    private Long id;

    /** 用于种子和专题关联的稳定分类编码。 */
    private String code;

    /** 分类展示名称。 */
    private String name;

    /** 分类的补充说明。 */
    private String description;

    /** 控制分类在题库中的显示顺序。 */
    private Integer sortOrder;

    /** 禁用后保留历史内容，但不再作为可选分类。 */
    private ContentStatus status;
}
