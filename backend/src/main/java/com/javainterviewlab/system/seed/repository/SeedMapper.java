package com.javainterviewlab.system.seed.repository;

import org.apache.ibatis.annotations.*;

@Mapper
public interface SeedMapper {
    @Select("SELECT COUNT(1) FROM question WHERE external_key=#{externalKey}") int questionExists(@Param("externalKey") String externalKey);
    @Select("SELECT id FROM topic WHERE code=#{code}") Long findTopicId(@Param("code") String code);
    @Select("SELECT id FROM category WHERE code=#{code}") Long findCategoryId(@Param("code") String code);
    @Select("INSERT INTO category(code,name,sort_order,status) VALUES(#{code},#{name},0,'ENABLED') ON CONFLICT(code) DO UPDATE SET name=EXCLUDED.name RETURNING id") Long upsertCategory(@Param("code") String code,@Param("name") String name);
    @Select("INSERT INTO topic(category_id,code,name,star_level,sort_order,status) VALUES(#{categoryId},#{code},#{name},#{starLevel},0,'ENABLED') ON CONFLICT(code) DO UPDATE SET category_id=EXCLUDED.category_id,name=EXCLUDED.name,star_level=EXCLUDED.star_level RETURNING id") Long upsertTopic(@Param("categoryId") Long categoryId,@Param("code") String code,@Param("name") String name,@Param("starLevel") Integer starLevel);
    @Select("INSERT INTO question(topic_id,external_key,title,question_type,star_level,difficulty,frequency_level,origin_type,status,one_liner,plain_explanation,design_reason,common_mistakes,score_points) VALUES(#{topicId},#{externalKey},#{title},'KNOWLEDGE',#{starLevel},#{difficulty},#{frequencyLevel},'IMPORTED','ENABLED',#{oneLiner},#{plainExplanation},#{designReason},NULL,NULL) RETURNING id") Long insertQuestion(@Param("topicId") Long topicId,@Param("externalKey") String externalKey,@Param("title") String title,@Param("starLevel") Integer starLevel,@Param("difficulty") String difficulty,@Param("frequencyLevel") String frequencyLevel,@Param("oneLiner") String oneLiner,@Param("plainExplanation") String plainExplanation,@Param("designReason") String designReason);
    @Insert("INSERT INTO question_answer(question_id,answer_type,content,sort_order) VALUES(#{questionId},#{answerType},#{content},#{sortOrder})") void insertAnswer(@Param("questionId") Long questionId,@Param("answerType") String answerType,@Param("content") String content,@Param("sortOrder") int sortOrder);
    @Insert("INSERT INTO question_follow_up(question_id,title,sort_order) VALUES(#{questionId},#{title},#{sortOrder})") void insertFollowUp(@Param("questionId") Long questionId,@Param("title") String title,@Param("sortOrder") int sortOrder);
    @Insert("INSERT INTO seed_import(seed_pack,version,imported_at) VALUES(#{seedPack},#{version},CURRENT_TIMESTAMP) ON CONFLICT(seed_pack) DO UPDATE SET version=EXCLUDED.version,imported_at=EXCLUDED.imported_at") void markImported(@Param("seedPack") String seedPack,@Param("version") String version);
}
