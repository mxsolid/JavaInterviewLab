package com.javainterviewlab.study.progress.repository;

import com.javainterviewlab.study.progress.repository.model.StudyProgressEntity;
import com.javainterviewlab.study.progress.repository.model.WrongQuestionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/**
 * study_progress 的数据库访问接口。
 *
 * <p>同一档案的提交需要串行化。首次提交还没有 progress 行，单靠 {@code FOR UPDATE}
 * 无法锁住“尚不存在的行”，因此先锁定档案行，再读取和 UPSERT。</p>
 */
@Mapper
public interface StudyProgressMapper {

    /**
     * 锁定学习档案行。
     *
     * <p>当前 V0.2 每个本地档案只有一个活跃学习者，按档案串行化提交足以覆盖首次创建窗口；
     * 锁随提交或回滚自动释放，不需要 Redis，也不会遗留应用层锁。</p>
     */
    void lockProfileForProgress(@Param("profileId") Long profileId);

    /** 读取当前快照；在咨询锁之后保留行锁，避免绕过本提交链路的更新覆盖当前行。 */
    StudyProgressEntity findByProfileIdAndQuestionIdForUpdate(@Param("profileId") Long profileId, @Param("questionId") Long questionId);

    /** 用 UPSERT 创建或推进快照，并原子累加次数。 */
    StudyProgressEntity upsertAfterAttempt(@Param("profileId") Long profileId, @Param("questionId") Long questionId,
        @Param("masteryLevel") String masteryLevel, @Param("wrong") boolean wrong, @Param("now") Instant now);

    /** 查询仍启用且处于激活状态的错题摘要。 */
    List<WrongQuestionRow> findActiveWrongQuestions(@Param("profileId") Long profileId);

    /** 将错题状态改为已解决；历史错误次数仍保留用于学习分析。 */
    int resolveWrongBook(@Param("profileId") Long profileId, @Param("questionId") Long questionId);
}
