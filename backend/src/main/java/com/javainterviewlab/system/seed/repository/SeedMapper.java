package com.javainterviewlab.system.seed.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SeedMapper {

    int questionExists(@Param("externalKey") String externalKey);

    Long findTopicId(@Param("code") String code);

    Long findCategoryId(@Param("code") String code);

    Long upsertCategory(@Param("code") String code, @Param("name") String name);

    Long upsertTopic(
            @Param("categoryId") Long categoryId,
            @Param("code") String code,
            @Param("name") String name,
            @Param("starLevel") Integer starLevel
    );

    Long insertQuestion(
            @Param("topicId") Long topicId,
            @Param("externalKey") String externalKey,
            @Param("title") String title,
            @Param("starLevel") Integer starLevel,
            @Param("difficulty") String difficulty,
            @Param("frequencyLevel") String frequencyLevel,
            @Param("oneLiner") String oneLiner,
            @Param("plainExplanation") String plainExplanation,
            @Param("designReason") String designReason
    );

    void insertAnswer(
            @Param("questionId") Long questionId,
            @Param("answerType") String answerType,
            @Param("content") String content,
            @Param("sortOrder") int sortOrder
    );

    void insertFollowUp(
            @Param("questionId") Long questionId,
            @Param("title") String title,
            @Param("sortOrder") int sortOrder
    );

    void markImported(@Param("seedPack") String seedPack, @Param("version") String version);
}
