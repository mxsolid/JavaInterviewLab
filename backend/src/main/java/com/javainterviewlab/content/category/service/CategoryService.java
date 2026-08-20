package com.javainterviewlab.content.category.service;

import com.javainterviewlab.content.category.dto.CategoryResponse;
import com.javainterviewlab.content.category.dto.CategoryRequest;
import com.javainterviewlab.content.category.repository.CategoryMapper;
import com.javainterviewlab.content.category.repository.model.CategoryEntity;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 分类业务服务。
 *
 * <p>Service 负责在 API DTO 与持久化实体之间转换，避免接口字段渗入 Mapper。</p>
 */
@Service
public class CategoryService {
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    /** 查询全部分类，并保持数据库定义的排序。 */
    public List<CategoryResponse> list() {
        return categoryMapper.findAll().stream().map(this::toResponse).toList();
    }

    /** 创建分类后回读，以返回数据库最终保存的状态。 */
    public CategoryResponse create(CategoryRequest request) {
        Long id = categoryMapper.insert(toEntity(null, request));
        return require(id);
    }

    /** 更新分类；不存在时返回受控 404，而不是把零行更新伪装为成功。 */
    public CategoryResponse update(Long id, CategoryRequest request) {
        if (categoryMapper.update(toEntity(id, request)) == 0) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "分类不存在");
        }
        return require(id);
    }

    private CategoryResponse require(Long id) {
        CategoryEntity entity = categoryMapper.findById(id);
        if (entity == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "分类不存在");
        }
        return toResponse(entity);
    }

    private CategoryEntity toEntity(Long id, CategoryRequest request) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(id);
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setSortOrder(request.effectiveSortOrder());
        entity.setStatus(request.effectiveStatus());
        return entity;
    }

    private CategoryResponse toResponse(CategoryEntity entity) {
        return new CategoryResponse(
                entity.getId(), entity.getCode(), entity.getName(), entity.getDescription(), entity.getSortOrder(),
                entity.getStatus().name()
        );
    }
}
