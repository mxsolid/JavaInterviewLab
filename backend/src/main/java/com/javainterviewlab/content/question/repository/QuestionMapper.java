package com.javainterviewlab.content.question.repository;

import com.javainterviewlab.content.question.repository.model.QuestionAnswerRow;
import com.javainterviewlab.content.question.repository.model.QuestionDetailRow;
import com.javainterviewlab.content.question.repository.model.QuestionEntity;
import com.javainterviewlab.content.question.repository.model.QuestionFollowUpRow;
import com.javainterviewlab.content.question.repository.model.QuestionQueryModel;
import com.javainterviewlab.content.question.repository.model.QuestionSummaryRow;
import com.javainterviewlab.content.question.repository.model.QuestionTagRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 题目聚合的持久化 Mapper。
 *
 * <p>题目主表、答案、追问和标签分别映射为 Repository model，Service 再组装 HTTP 响应。</p>
 */
@Mapper
public interface QuestionMapper {

    /** 按归一化分页条件读取题目摘要。 */
    List<QuestionSummaryRow> findPage(QuestionQueryModel query);

    /** 统计与摘要查询使用相同条件，避免前端页码与总数不一致。 */
    long count(QuestionQueryModel query);

    /** 查询题目主表及所属专题、分类。 */
    QuestionDetailRow findDetail(@Param("id") Long id);

    /** 写入题目主表，子项由独立方法在同一 Service 事务内写入。 */
    Long insert(@Param("entity") QuestionEntity entity);

    /** 使用 version 条件更新，返回 0 代表资源不存在或已被其他编辑者修改。 */
    int update(@Param("entity") QuestionEntity entity);

    void deleteAnswers(@Param("questionId") Long questionId);

    void deleteFollowUps(@Param("questionId") Long questionId);

    void deleteTags(@Param("questionId") Long questionId);

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

    void insertTag(@Param("questionId") Long questionId, @Param("tagId") Long tagId);

    /** 读取题目标签。 */
    List<QuestionTagRow> findTags(@Param("questionId") Long questionId);

    /** 读取题目答案，并保持内容定义的排序。 */
    List<QuestionAnswerRow> findAnswers(@Param("questionId") Long questionId);

    /** 读取题目追问，并保持内容定义的排序。 */
    List<QuestionFollowUpRow> findFollowUps(@Param("questionId") Long questionId);

    int countById(@Param("id") Long id);
}
