package com.javainterviewlab.content.category.service;

import com.javainterviewlab.content.category.dto.CategoryResponse;
import com.javainterviewlab.content.category.dto.CategoryRequest;
import com.javainterviewlab.content.category.repository.CategoryMapper;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryMapper categoryMapper;
    public CategoryService(CategoryMapper categoryMapper) { this.categoryMapper = categoryMapper; }
    public List<CategoryResponse> list() { return categoryMapper.findAll(); }
    public CategoryResponse create(CategoryRequest request) {
        Long id = categoryMapper.insert(request.code(), request.name(), request.description(), request.effectiveSortOrder(), request.effectiveStatus().name());
        return require(id);
    }
    public CategoryResponse update(Long id, CategoryRequest request) {
        if (categoryMapper.update(id, request.code(), request.name(), request.description(), request.effectiveSortOrder(), request.effectiveStatus().name()) == 0) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "分类不存在");
        }
        return require(id);
    }
    private CategoryResponse require(Long id) {
        CategoryResponse response = categoryMapper.findById(id);
        if (response == null) throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "分类不存在");
        return response;
    }
}
