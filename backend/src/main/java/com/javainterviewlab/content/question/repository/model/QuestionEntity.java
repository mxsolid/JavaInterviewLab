package com.javainterviewlab.content.question.repository.model;

import com.javainterviewlab.content.question.domain.Difficulty;
import com.javainterviewlab.content.question.domain.FrequencyLevel;
import com.javainterviewlab.content.question.domain.OriginType;
import com.javainterviewlab.content.question.domain.QuestionType;
import com.javainterviewlab.content.shared.ContentStatus;
import lombok.Data;

/**
 * 题目主表持久化实体，对应 question 表。
 *
 * <p>答案、追问和标签属于独立子表，不能混入该对象，以免 Mapper 依赖编辑页面的嵌套请求结构。</p>
 */
@Data
public class QuestionEntity {

    /** 题目主键，创建前为空。 */
    private Long id;

    /** 所属专题主键。 */
    private Long topicId;

    /** 题目标题。 */
    private String title;

    /** 题目考查形式。 */
    private QuestionType questionType;

    /** 面试重要程度。 */
    private Integer starLevel;

    /** 回答所需知识深度。 */
    private Difficulty difficulty;

    /** 题目在面试中的出现频度。 */
    private FrequencyLevel frequencyLevel;

    /** 题目的内容来源。 */
    private OriginType originType;

    /** 是否允许在学习端展示。 */
    private ContentStatus status;

    /** 用于快速回忆的一句话解释。 */
    private String oneLiner;

    /** 面向初学者的通俗讲解。 */
    private String plainExplanation;

    /** 解释方案取舍的设计原因。 */
    private String designReason;

    /** 常见错误和误区。 */
    private String commonMistakes;

    /** 面试回答的得分点。 */
    private String scorePoints;

    /** 编辑乐观锁版本。 */
    private Long version;
}
