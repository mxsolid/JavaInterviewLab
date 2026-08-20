package com.javainterviewlab.content.topic.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.content.category.repository.CategoryMapper;
import com.javainterviewlab.content.topic.dto.TopicRequest;
import com.javainterviewlab.content.topic.dto.TopicResponse;
import com.javainterviewlab.content.topic.repository.TopicMapper;
import com.javainterviewlab.content.topic.repository.model.TopicEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 维护专题与分类归属。
 *
 * <p>专题归属必须在写入前校验分类存在，不能依赖数据库错误向前端泄漏实现细节。</p>
 */
@Service
public class TopicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicService.class);

    private final TopicMapper topicMapper;
    private final CategoryMapper categoryMapper;

    public TopicService(TopicMapper topicMapper, CategoryMapper categoryMapper) {
        this.topicMapper = topicMapper;
        this.categoryMapper = categoryMapper;
    }

    /** 按分类筛选专题，并把持久化实体转换为接口响应。 */
    public List<TopicResponse> list(Long categoryId) {
        return topicMapper.findAll(categoryId).stream().map(this::toResponse).toList();
    }

    /** 创建专题并回读数据库最终保存值。 */
    public TopicResponse create(TopicRequest request) {
        verifyCategory(request.categoryId());
        Long id = topicMapper.insert(toEntity(null, request));
        LOGGER.info("专题创建成功, topicId={}, categoryId={}", id, request.categoryId());
        return require(id);
    }

    /** 更新专题；外键校验和更新必须先后执行，避免无效分类写入。 */
    public TopicResponse update(Long id, TopicRequest request) {
        verifyCategory(request.categoryId());
        if (topicMapper.update(toEntity(id, request)) == 0) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "专题不存在");
        }
        LOGGER.info("专题更新成功, topicId={}, categoryId={}", id, request.categoryId());
        return require(id);
    }

    private void verifyCategory(Long categoryId) {
        if (categoryMapper.findById(categoryId) == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "分类不存在");
        }
    }

    private TopicResponse require(Long id) {
        TopicEntity topic = topicMapper.findById(id);
        if (topic == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "专题不存在");
        }
        return toResponse(topic);
    }

    private TopicEntity toEntity(Long id, TopicRequest request) {
        TopicEntity entity = new TopicEntity();
        entity.setId(id);
        entity.setCategoryId(request.categoryId());
        entity.setCode(request.code());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setStarLevel(request.starLevel());
        entity.setSortOrder(request.effectiveSortOrder());
        entity.setStatus(request.effectiveStatus());
        return entity;
    }

    private TopicResponse toResponse(TopicEntity entity) {
        return new TopicResponse(
                entity.getId(), entity.getCategoryId(), entity.getCategoryName(), entity.getCode(), entity.getName(),
                entity.getDescription(), entity.getStarLevel(), entity.getSortOrder(), entity.getStatus().name()
        );
    }
}
