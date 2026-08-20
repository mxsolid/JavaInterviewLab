package com.javainterviewlab.content.question;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** P05 题目练习元数据、学习内容和答案披露行为验收。 */
@SpringBootTest
@AutoConfigureMockMvc
class QuestionWorkspaceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldKeepAnswersOutOfPracticeMetadataAndExposeLearningContent() throws Exception {
        Long questionId = enabledQuestionIds().getFirst();

        mockMvc.perform(get("/api/v1/questions/{id}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contentMode").value("PRACTICE_METADATA"))
                .andExpect(jsonPath("$.data.answers").doesNotExist())
                .andExpect(jsonPath("$.data.plainExplanation").doesNotExist())
                .andExpect(jsonPath("$.data.followUps").doesNotExist());

        mockMvc.perform(get("/api/v1/questions/{id}/learning", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.contentMode").value("LEARNING"))
                .andExpect(jsonPath("$.data.answers", hasSize(3)))
                .andExpect(jsonPath("$.data.followUps").isArray());
    }

    @Test
    @Transactional
    void shouldAppendAndDeduplicateAnswerViews() throws Exception {
        List<Long> questionIds = enabledQuestionIds();
        UUID clientViewId = UUID.randomUUID();
        String body = "{\"clientViewId\":\"" + clientViewId + "\"}";

        mockMvc.perform(post("/api/v1/questions/{id}/answer-view", questionIds.get(0))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicated").value(false))
                .andExpect(jsonPath("$.data.learning.answers", hasSize(3)));
        mockMvc.perform(post("/api/v1/questions/{id}/answer-view", questionIds.get(0))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicated").value(true));
        mockMvc.perform(post("/api/v1/questions/{id}/answer-view", questionIds.get(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("VERSION_CONFLICT"));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM question_answer_view WHERE client_view_id = ?::uuid",
                Integer.class,
                clientViewId.toString()
        )).isEqualTo(1);
    }

    @Test
    void shouldPublishV1QuestionAndStudyPaths() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/questions/{id}']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/questions/{id}/learning']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/questions/{id}/answer-view']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/study/attempts']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/study/questions/{questionId}/progress']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/study/notes']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/study/favorites']").exists());
    }

    private List<Long> enabledQuestionIds() {
        return jdbcTemplate.queryForList(
                "SELECT id FROM question WHERE status = 'ENABLED' ORDER BY id LIMIT 2",
                Long.class
        );
    }
}
