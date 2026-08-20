package com.javainterviewlab.study.favorite.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.content.ContentTargetType;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.study.favorite.dto.FavoriteResponse;
import com.javainterviewlab.study.favorite.repository.FavoriteMapper;
import com.javainterviewlab.study.favorite.repository.model.FavoriteEntity;
import com.javainterviewlab.study.favorite.repository.model.FavoriteQuestionRow;
import com.javainterviewlab.study.profile.repository.StudyProfileMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** 收藏题目的业务服务；第一版只开放 QUESTION，底层类型为后续专题和场景预留。 */
@Service
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final StudyProfileMapper studyProfileMapper;

    public FavoriteService(FavoriteMapper favoriteMapper, StudyProfileMapper studyProfileMapper) {
        this.favoriteMapper = favoriteMapper;
        this.studyProfileMapper = studyProfileMapper;
    }

    /** 收藏启用题目；数据库唯一约束使重复请求返回同一当前状态。 */
    public void favoriteQuestion(Long questionId) {
        requireEnabledQuestion(questionId);
        FavoriteEntity entity = new FavoriteEntity();
        entity.setProfileId(requireDefaultProfileId());
        entity.setTargetType(ContentTargetType.QUESTION);
        entity.setTargetId(questionId);
        favoriteMapper.insertIgnore(entity);
    }

    /** 取消收藏是幂等操作，重复取消不产生错误或历史记录。 */
    public void unfavoriteQuestion(Long questionId) {
        favoriteMapper.deleteByProfileAndQuestion(requireDefaultProfileId(), questionId);
    }

    /** 返回启用题目的收藏摘要；已停用内置题目保留数据库记录但默认隐藏。 */
    public List<FavoriteResponse> listFavoriteQuestions() {
        return favoriteMapper.findEnabledQuestionFavorites(requireDefaultProfileId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private Long requireDefaultProfileId() {
        Long profileId = studyProfileMapper.findDefaultProfileId();
        if (profileId == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "默认学习档案不存在");
        }
        return profileId;
    }

    private void requireEnabledQuestion(Long questionId) {
        if (favoriteMapper.countEnabledQuestionById(questionId) == 0) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "题目不存在或已停用");
        }
    }

    private FavoriteResponse toResponse(FavoriteQuestionRow row) {
        return new FavoriteResponse(row.favoriteId(), row.questionId(), row.title(), row.starLevel(), row.createdAt());
    }
}
