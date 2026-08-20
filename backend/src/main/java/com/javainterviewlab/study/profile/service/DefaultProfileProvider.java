package com.javainterviewlab.study.profile.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.study.profile.repository.StudyProfileMapper;
import org.springframework.stereotype.Component;

/** 单用户本地版本的当前档案实现。 */
@Component
public class DefaultProfileProvider implements CurrentProfileProvider {

    private final StudyProfileMapper studyProfileMapper;

    public DefaultProfileProvider(StudyProfileMapper studyProfileMapper) {
        this.studyProfileMapper = studyProfileMapper;
    }

    @Override
    public Long requireProfileId() {
        Long profileId = studyProfileMapper.findDefaultProfileId();
        if (profileId == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "默认学习档案不存在");
        }
        return profileId;
    }
}
