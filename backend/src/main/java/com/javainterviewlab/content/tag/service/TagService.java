package com.javainterviewlab.content.tag.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.content.tag.dto.TagRequest;
import com.javainterviewlab.content.tag.dto.TagResponse;
import com.javainterviewlab.content.tag.repository.TagMapper;
import com.javainterviewlab.content.tag.repository.model.TagEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/** 标签业务服务，负责隔离 API DTO 与标签持久化实体。 */
@Service
public class TagService {

    private final TagMapper tagMapper;

    public TagService(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    /** 查询全部标签。 */
    public List<TagResponse> list() {
        return tagMapper.findAll().stream().map(this::toResponse).toList();
    }

    /** 创建标签后回读，保证返回数据库持久化后的主键。 */
    public TagResponse create(TagRequest request) {
        return require(tagMapper.insert(toEntity(null, request)));
    }

    /** 更新标签；零行更新转换为业务 404。 */
    public TagResponse update(Long id, TagRequest request) {
        if (tagMapper.update(toEntity(id, request)) == 0) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "标签不存在");
        }
        return require(id);
    }

    private TagResponse require(Long id) {
        TagEntity tag = tagMapper.findById(id);
        if (tag == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "标签不存在");
        }
        return toResponse(tag);
    }

    private TagEntity toEntity(Long id, TagRequest request) {
        TagEntity entity = new TagEntity();
        entity.setId(id);
        entity.setCode(request.code());
        entity.setName(request.name());
        return entity;
    }

    private TagResponse toResponse(TagEntity entity) {
        return new TagResponse(entity.getId(), entity.getCode(), entity.getName());
    }
}
