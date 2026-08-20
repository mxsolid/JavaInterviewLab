package com.javainterviewlab.study;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证 V0.2 各学习状态通过 HTTP 和 PostgreSQL 真实 SQL 协同工作。 */
@SpringBootTest
@AutoConfigureMockMvc
class StudyLoopApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 错题、收藏、笔记版本和 review 替换均以同一条临时题目验证，测试结束自动回滚。 */
    @Test
    @Transactional
    void shouldMaintainStudyLoopStatesAndDashboard() throws Exception {
        long questionId = createEnabledQuestion();
        submit(questionId, "WRONG", 1);
        mockMvc.perform(get("/api/study/wrong-questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].questionId").value(questionId));
        mockMvc.perform(put("/api/study/questions/{questionId}/wrong-book/resolve", questionId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/study/wrong-questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
        submit(questionId, "WRONG", 1);
        mockMvc.perform(get("/api/study/wrong-questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].wrongCount").value(2));

        mockMvc.perform(post("/api/study/favorites/questions/{questionId}", questionId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/study/favorites/questions/{questionId}", questionId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/study/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(delete("/api/study/favorites/questions/{questionId}", questionId)).andExpect(status().isOk());
        mockMvc.perform(get("/api/study/favorites")).andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(0));

        String createBody = mockMvc.perform(post("/api/study/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"QUESTION\",\"targetId\":" + questionId + ",\"content\":\"第一版笔记\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(0))
                .andReturn().getResponse().getContentAsString();
        Number noteId = JsonPath.read(createBody, "$.data.id");
        mockMvc.perform(put("/api/study/notes/{id}", noteId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"第二版笔记\",\"version\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(1));
        mockMvc.perform(put("/api/study/notes/{id}", noteId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"旧页面覆盖\",\"version\":0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VERSION_CONFLICT"));
        mockMvc.perform(get("/api/study/notes").param("targetType", "QUESTION").param("targetId", String.valueOf(questionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("第二版笔记"));

        Integer pendingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_task WHERE question_id = ? AND status = 'PENDING'", Integer.class, questionId);
        Integer completedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM review_task WHERE question_id = ? AND status = 'COMPLETED'", Integer.class, questionId);
        assertThat(pendingCount).isEqualTo(1);
        assertThat(completedCount).isEqualTo(1);
        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.touchedQuestionCount").isNumber())
                .andExpect(jsonPath("$.data.activeWrongQuestionCount").value(1))
                .andExpect(jsonPath("$.data.recentStudyItems[0].questionId").value(questionId));
    }

    private void submit(long questionId, String resultType, int rating) throws Exception {
        mockMvc.perform(post("/api/study/attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\":" + questionId + ",\"clientAttemptId\":\"" + UUID.randomUUID()
                                + "\",\"viewedAnswer\":true,\"selfRating\":" + rating
                                + ",\"resultType\":\"" + resultType + "\",\"elapsedMs\":100}"))
                .andExpect(status().isOk());
    }

    private long createEnabledQuestion() {
        Long topicId = jdbcTemplate.queryForObject("SELECT id FROM topic WHERE status = 'ENABLED' ORDER BY id LIMIT 1", Long.class);
        return jdbcTemplate.queryForObject(
                "INSERT INTO question (topic_id, title, question_type, star_level, difficulty, frequency_level, origin_type, status) "
                        + "VALUES (?, ?, 'CONCEPT', 5, 'MEDIUM', 'HIGH', 'USER', 'ENABLED') RETURNING id",
                Long.class,
                topicId,
                "V02 集成测试题 " + System.nanoTime()
        );
    }
}
