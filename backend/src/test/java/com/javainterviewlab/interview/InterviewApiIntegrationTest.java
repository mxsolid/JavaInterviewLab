package com.javainterviewlab.interview;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** P06 模拟面试 API 与持久化评分验收。 */
@SpringBootTest
@AutoConfigureMockMvc
class InterviewApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void shouldCreateScoreDeduplicateAndFinishInterview() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"RANDOM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.prompt").isNotEmpty())
                .andExpect(jsonPath("$.data.provider").value("RULE_BASED"))
                .andExpect(jsonPath("$.data.providerEnabled").value(false))
                .andReturn();
        JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsByteArray());
        long sessionId = createBody.path("data").path("id").asLong();
        UUID clientTurnId = UUID.randomUUID();
        String turnBody = "{\"clientTurnId\":\"" + clientTurnId
                + "\",\"answerText\":\"首先说明核心机制，然后解释事务与并发边界，最后补充性能权衡。\"}";

        mockMvc.perform(post("/api/v1/interviews/{id}/turns", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(turnBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicated").value(false))
                .andExpect(jsonPath("$.data.dimensions", hasSize(4)))
                .andExpect(jsonPath("$.data.totalScore").isNumber());
        mockMvc.perform(post("/api/v1/interviews/{id}/turns", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(turnBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicated").value(true));

        mockMvc.perform(post("/api/v1/interviews/{id}/finish", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FINISHED"))
                .andExpect(jsonPath("$.data.turnCount").value(1))
                .andExpect(jsonPath("$.data.dimensions", hasSize(4)));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM interview_turn WHERE session_id = ?",
                Integer.class,
                sessionId
        )).isEqualTo(1);
    }

    @Test
    void shouldValidateTopicModeAndPublishPaths() throws Exception {
        mockMvc.perform(post("/api/v1/interviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"TOPIC\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/interviews']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/interviews/{id}/turns']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/interviews/{id}/finish']").exists());
    }
}
