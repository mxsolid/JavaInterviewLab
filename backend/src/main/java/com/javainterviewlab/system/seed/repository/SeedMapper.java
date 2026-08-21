package com.javainterviewlab.system.seed.repository;

import com.javainterviewlab.system.seed.domain.SeedPackContent.SeedQuestion;
import com.javainterviewlab.system.seed.repository.model.SeedHistoryRow;
import com.javainterviewlab.system.seed.repository.model.SeedQuestionRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/** Seed V2 的数据库操作；Service 负责决策，SQL 条件作为来源保护的最终防线。 */
@Mapper
public interface SeedMapper {

    void lockSeedPack(@Param("seedPack") String seedPack);

    SeedHistoryRow findHistory(@Param("seedPack") String seedPack, @Param("version") String version);

    SeedHistoryRow findLatestHistory(@Param("seedPack") String seedPack);

    SeedQuestionRow findQuestionByExternalKey(@Param("externalKey") String externalKey);

    Long findTopicId(@Param("code") String code);

    Long findCategoryId(@Param("code") String code);

    Long upsertCategory(@Param("code") String code, @Param("name") String name);

    Long upsertTopic(
            @Param("categoryId") Long categoryId,
            @Param("code") String code,
            @Param("name") String name,
            @Param("starLevel") Integer starLevel
    );

    int linkSourceSnippetsToTopics();

    Long insertQuestion(
            @Param("topicId") Long topicId,
            @Param("seedPack") String seedPack,
            @Param("question") SeedQuestion question
    );

    int updateQuestion(
            @Param("topicId") Long topicId,
            @Param("seedPack") String seedPack,
            @Param("question") SeedQuestion question
    );

    void deleteAnswers(@Param("questionId") Long questionId);

    void deleteFollowUps(@Param("questionId") Long questionId);

    void deleteTags(@Param("questionId") Long questionId);

    Long upsertTag(@Param("code") String code);

    void insertQuestionTag(@Param("questionId") Long questionId, @Param("tagId") Long tagId);

    void insertAnswer(
            @Param("questionId") Long questionId,
            @Param("answerType") String answerType,
            @Param("content") String content,
            @Param("sortOrder") int sortOrder
    );

    void insertFollowUp(
            @Param("questionId") Long questionId,
            @Param("title") String title,
            @Param("referenceAnswer") String referenceAnswer,
            @Param("sortOrder") int sortOrder
    );

    void insertHistory(
            @Param("seedPack") String seedPack,
            @Param("version") String version,
            @Param("checksum") String checksum,
            @Param("importMode") String importMode,
            @Param("created") int created,
            @Param("updated") int updated,
            @Param("skipped") int skipped,
            @Param("durationMs") long durationMs
    );

    void markImported(@Param("seedPack") String seedPack, @Param("version") String version);
}
