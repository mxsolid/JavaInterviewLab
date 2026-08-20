package com.javainterviewlab.study.progress;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证尚无答题历史的题目也能向详情页提供可直接展示的学习快照。 */
@SpringBootTest
@AutoConfigureMockMvc
class StudyProgressApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 从未学习不应被当作资源不存在，而应返回 PREVIEW/UNKNOWN 默认状态。 */
    @Test
    @Transactional
    void shouldReturnDefaultProgressForUnpracticedQuestion() throws Exception {
        long questionId = createEnabledQuestion();

        mockMvc.perform(get("/api/study/questions/{questionId}/progress", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questionId").value(questionId))
                .andExpect(jsonPath("$.data.stage").value("PREVIEW"))
                .andExpect(jsonPath("$.data.stageDescription").value("预习"))
                .andExpect(jsonPath("$.data.masteryLevel").value("UNKNOWN"))
                .andExpect(jsonPath("$.data.masteryDescription").value("不会"))
                .andExpect(jsonPath("$.data.attemptCount").value(0))
                .andExpect(jsonPath("$.data.wrongCount").value(0))
                .andExpect(jsonPath("$.data.wrongBookActive").value(false))
                .andExpect(jsonPath("$.data.lastStudiedAt").doesNotExist());
    }

    private long createEnabledQuestion() {
        Long topicId = jdbcTemplate.queryForObject("SELECT id FROM topic WHERE status = 'ENABLED' ORDER BY id LIMIT 1", Long.class);
        return jdbcTemplate.queryForObject(
                "INSERT INTO question (topic_id, title, question_type, star_level, difficulty, frequency_level, origin_type, status) "
                        + "VALUES (?, ?, 'CONCEPT', 3, 'MEDIUM', 'HIGH', 'USER', 'ENABLED') RETURNING id",
                Long.class,
                topicId,
                "单题进度测试题 " + System.nanoTime()
        );
    }
}
