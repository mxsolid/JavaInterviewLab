package com.javainterviewlab.study.note.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.content.ContentTargetType;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.study.note.dto.CreateNoteRequest;
import com.javainterviewlab.study.note.dto.NoteResponse;
import com.javainterviewlab.study.note.dto.SaveNoteRequest;
import com.javainterviewlab.study.note.repository.NoteMapper;
import com.javainterviewlab.study.note.repository.model.NoteEntity;
import com.javainterviewlab.study.profile.repository.StudyContentTargetMapper;
import com.javainterviewlab.study.profile.repository.StudyProfileMapper;
import org.springframework.stereotype.Service;

/**
 * 用户笔记服务。
 *
 * <p>笔记通常低冲突，因此使用乐观锁而不是长事务或悲观锁；冲突时保留用户本地输入，由前端提示重新加载。</p>
 */
@Service
public class NoteService {

    private final NoteMapper noteMapper;
    private final StudyProfileMapper studyProfileMapper;
    private final StudyContentTargetMapper contentTargetMapper;

    public NoteService(
            NoteMapper noteMapper,
            StudyProfileMapper studyProfileMapper,
            StudyContentTargetMapper contentTargetMapper
    ) {
        this.noteMapper = noteMapper;
        this.studyProfileMapper = studyProfileMapper;
        this.contentTargetMapper = contentTargetMapper;
    }

    /** 查询一个已有笔记；不存在时返回 null，便于编辑器区分首次创建。 */
    public NoteResponse find(ContentTargetType targetType, Long targetId) {
        requireSupportedExistingTarget(targetType, targetId);
        NoteEntity entity = noteMapper.findByProfileAndTarget(requireDefaultProfileId(), targetType, targetId);
        return entity == null ? null : toResponse(entity);
    }

    /** 创建首次笔记；同一目标已有记录时拒绝，防止绕过 version 语义。 */
    public NoteResponse create(CreateNoteRequest request) {
        requireSupportedExistingTarget(request.targetType(), request.targetId());
        Long profileId = requireDefaultProfileId();
        if (noteMapper.findByProfileAndTarget(profileId, request.targetType(), request.targetId()) != null) {
            throw new BusinessException(ApiErrorCode.BUSINESS_RULE_VIOLATED, "该内容已有笔记，请使用最新版本保存");
        }
        NoteEntity entity = new NoteEntity();
        entity.setProfileId(profileId);
        entity.setTargetType(request.targetType());
        entity.setTargetId(request.targetId());
        entity.setContent(request.content());
        return toResponse(noteMapper.insert(entity));
    }

    /** 更新笔记；版本不一致时返回 409，绝不覆盖其他页面已保存的文本。 */
    public NoteResponse update(Long id, SaveNoteRequest request) {
        NoteEntity entity = new NoteEntity();
        entity.setId(id);
        entity.setContent(request.content());
        entity.setVersion(request.version());
        if (noteMapper.updateIfVersionMatches(entity) == 0) {
            if (noteMapper.countById(id) == 0) {
                throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "笔记不存在");
            }
            throw new BusinessException(ApiErrorCode.VERSION_CONFLICT, "笔记已被其他页面修改，请重新加载");
        }
        return toResponse(noteMapper.findById(id));
    }

    private Long requireDefaultProfileId() {
        Long profileId = studyProfileMapper.findDefaultProfileId();
        if (profileId == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "默认学习档案不存在");
        }
        return profileId;
    }

    private void requireSupportedExistingTarget(ContentTargetType targetType, Long targetId) {
        boolean exists = switch (targetType) {
            case QUESTION -> contentTargetMapper.countEnabledQuestionById(targetId) > 0;
            case TOPIC -> contentTargetMapper.countEnabledTopicById(targetId) > 0;
            case SCENARIO -> throw new BusinessException(ApiErrorCode.BUSINESS_RULE_VIOLATED, "场景训练尚未上线");
        };
        if (!exists) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "内容不存在或已停用");
        }
    }

    private NoteResponse toResponse(NoteEntity entity) {
        return new NoteResponse(
                entity.getId(),
                entity.getTargetType().name(),
                entity.getTargetId(),
                entity.getContent(),
                entity.getVersion(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
