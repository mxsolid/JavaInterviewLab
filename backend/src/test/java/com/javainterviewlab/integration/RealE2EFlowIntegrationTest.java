package com.javainterviewlab.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** P07 禁止 Mock 的 Controller 到 PostgreSQL 主链路验收。 */
@SpringBootTest
@AutoConfigureMockMvc
class RealE2EFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void shouldCompleteRealLearningAndScenarioFlowsWithDatabaseAssertions() throws Exception {
        long questionId = cleanProductionQuestionId();
        long scenarioId = jdbcTemplate.queryForObject(
                "SELECT id FROM scenario WHERE status = 'ENABLED' ORDER BY id LIMIT 1", Long.class);
        long caseId = jdbcTemplate.queryForObject(
                "SELECT id FROM scenario_case WHERE scenario_id = ? ORDER BY sort_order LIMIT 1", Long.class, scenarioId);
        int beforeAttempts = count("SELECT COUNT(*) FROM question_attempt WHERE question_id = ?", questionId);
        int beforeWrong = nullableInt("SELECT wrong_count FROM study_progress WHERE profile_id = 1 AND question_id = ?", questionId);

        mockMvc.perform(get("/api/v1/workbench"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overview.totalQuestionCount").isNumber());
        mockMvc.perform(get("/api/v1/knowledge-map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.categories").isArray());
        mockMvc.perform(get("/api/questions").param("keyword", "HashMap").param("pageSize", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.total").isNumber());
        mockMvc.perform(get("/api/v1/source-snippets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].topicId").isNumber());
        mockMvc.perform(get("/api/v1/system/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("UP"));

        mockMvc.perform(get("/api/v1/questions/{id}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answers").doesNotExist());
        mockMvc.perform(get("/api/v1/questions/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        UUID viewId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/questions/{id}/answer-view", questionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"clientViewId\":\"" + viewId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.learning.answers").isArray());

        UUID attemptId = UUID.randomUUID();
        String attempt = "{\"questionId\":" + questionId + ",\"clientAttemptId\":\"" + attemptId
                + "\",\"answerText\":\"P07 真实联调回答\",\"viewedAnswer\":true,\"selfRating\":2,"
                + "\"resultType\":\"WRONG\",\"elapsedMs\":800}";
        mockMvc.perform(post("/api/v1/study/attempts").contentType(MediaType.APPLICATION_JSON).content(attempt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicated").value(false));
        mockMvc.perform(post("/api/v1/study/attempts").contentType(MediaType.APPLICATION_JSON).content(attempt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicated").value(true));
        mockMvc.perform(get("/api/v1/study/questions/{id}/progress", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.wrongBookActive").value(true));
        mockMvc.perform(get("/api/v1/study/wrong-questions"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/study/favorites/questions/{id}", questionId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/study/favorites"))
                .andExpect(status().isOk());

        String noteBody = mockMvc.perform(post("/api/v1/study/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetType\":\"QUESTION\",\"targetId\":" + questionId + ",\"content\":\"P07 第一版笔记\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(0))
                .andReturn().getResponse().getContentAsString();
        Number noteId = JsonPath.read(noteBody, "$.data.id");
        mockMvc.perform(put("/api/v1/study/notes/{id}", noteId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"P07 第二版笔记\",\"version\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").value(1));
        mockMvc.perform(put("/api/v1/study/notes/{id}", noteId.longValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"过期页面覆盖\",\"version\":0}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VERSION_CONFLICT"));

        UUID scenarioAttemptId = UUID.randomUUID();
        String scenarioAttempt = "{\"clientAttemptId\":\"" + scenarioAttemptId + "\",\"scenarioId\":" + scenarioId
                + ",\"caseId\":" + caseId + ",\"answerText\":\"P07 场景回答\",\"selfRating\":3,"
                + "\"resultType\":\"PARTIAL\",\"durationSeconds\":5}";
        mockMvc.perform(post("/api/v1/scenario-attempts").contentType(MediaType.APPLICATION_JSON).content(scenarioAttempt))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.duplicated").value(false));
        mockMvc.perform(post("/api/v1/scenario-attempts").contentType(MediaType.APPLICATION_JSON).content(scenarioAttempt))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.duplicated").value(true));

        MockMultipartFile invalidSeed = new MockMultipartFile("file", "invalid.json", "application/json", "{invalid".getBytes());
        mockMvc.perform(multipart("/api/v1/system/seeds/validate").file(invalidSeed))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("CONTENT_VALIDATION_FAILED"));

        assertThat(count("SELECT COUNT(*) FROM question_answer_view WHERE client_view_id = ?::uuid", viewId.toString())).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM question_attempt WHERE question_id = ?", questionId)).isEqualTo(beforeAttempts + 1);
        assertThat(count("SELECT COUNT(*) FROM review_task WHERE profile_id = 1 AND question_id = ? AND status = 'PENDING'", questionId)).isEqualTo(1);
        assertThat(nullableInt("SELECT wrong_count FROM study_progress WHERE profile_id = 1 AND question_id = ?", questionId)).isEqualTo(beforeWrong + 1);
        assertThat(count("SELECT COUNT(*) FROM favorite WHERE profile_id = 1 AND target_type = 'QUESTION' AND target_id = ?", questionId)).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM note WHERE profile_id = 1 AND target_type = 'QUESTION' AND target_id = ?", questionId)).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM scenario_attempt WHERE client_attempt_id = ?::uuid", scenarioAttemptId.toString())).isEqualTo(1);
    }

    private long cleanProductionQuestionId() {
        return jdbcTemplate.queryForObject("""
                SELECT q.id
                FROM question q
                WHERE q.status = 'ENABLED'
                  AND q.seed_pack = 'v03-core-complete'
                  AND NOT EXISTS (SELECT 1 FROM note n WHERE n.profile_id = 1 AND n.target_type = 'QUESTION' AND n.target_id = q.id)
                  AND NOT EXISTS (SELECT 1 FROM favorite f WHERE f.profile_id = 1 AND f.target_type = 'QUESTION' AND f.target_id = q.id)
                ORDER BY q.id
                LIMIT 1
                """, Long.class);
    }

    private int count(String sql, Object value) {
        return jdbcTemplate.queryForObject(sql, Integer.class, value);
    }

    private int nullableInt(String sql, Object value) {
        Integer result = jdbcTemplate.query(sql, resultSet -> resultSet.next() ? resultSet.getInt(1) : 0, value);
        return result == null ? 0 : result;
    }
}
