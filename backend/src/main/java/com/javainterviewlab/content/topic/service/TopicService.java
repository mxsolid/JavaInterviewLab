package com.javainterviewlab.content.topic.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.content.category.repository.CategoryMapper;
import com.javainterviewlab.content.topic.dto.TopicRequest;
import com.javainterviewlab.content.topic.dto.TopicResponse;
import com.javainterviewlab.content.topic.repository.TopicMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/** 维护专题与分类归属。 */
@Service
public class TopicService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicService.class);

    private final TopicMapper topicMapper;
    private final CategoryMapper categoryMapper;

    public TopicService(TopicMapper topicMapper, CategoryMapper categoryMapper) {
        this.topicMapper = topicMapper;
        this.categoryMapper = categoryMapper;
    }

    public List<TopicResponse> list(Long categoryId) {
        return topicMapper.findAll(categoryId);
    }

    public TopicResponse create(TopicRequest request) {
        verifyCategory(request.categoryId());
        Long id = topicMapper.insert(request.categoryId(), request.code(), request.name(), request.description(), request.starLevel(), request.effectiveSortOrder(), request.effectiveStatus().name());
        LOGGER.info("专题创建成功, topicId={}, categoryId={}", id, request.categoryId());
        return require(id);
    }

    public TopicResponse update(Long id, TopicRequest request) {
        verifyCategory(request.categoryId());
        if (topicMapper.update(id, request.categoryId(), request.code(), request.name(), request.description(), request.starLevel(), request.effectiveSortOrder(), request.effectiveStatus().name()) == 0) {
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
        TopicResponse topic = topicMapper.findById(id);
        if (topic == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "专题不存在");
        }
        return topic;
    }
}
