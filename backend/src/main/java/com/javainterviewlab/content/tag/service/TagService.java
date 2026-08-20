package com.javainterviewlab.content.tag.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.content.tag.dto.*;
import com.javainterviewlab.content.tag.repository.TagMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TagService {
    private final TagMapper tagMapper;
    public TagService(TagMapper tagMapper) { this.tagMapper=tagMapper; }
    public List<TagResponse> list() { return tagMapper.findAll(); }
    public TagResponse create(TagRequest request) { return require(tagMapper.insert(request.code(),request.name())); }
    public TagResponse update(Long id,TagRequest request) { if(tagMapper.update(id,request.code(),request.name())==0) throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND,"标签不存在"); return require(id); }
    private TagResponse require(Long id) { TagResponse item=tagMapper.findById(id); if(item==null) throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND,"标签不存在"); return item; }
}
