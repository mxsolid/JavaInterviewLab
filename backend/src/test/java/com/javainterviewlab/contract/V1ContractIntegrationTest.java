package com.javainterviewlab.contract;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 固定 V0.3 新契约并确认 V0.2 入口继续兼容。 */
@SpringBootTest
@AutoConfigureMockMvc
class V1ContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldExposeWorkbenchAndKnowledgeMapWithoutRemovingDashboard() throws Exception {
        mockMvc.perform(get("/api/v1/workbench"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generatedAt").isString())
                .andExpect(jsonPath("$.data.overview.totalQuestionCount").isNumber())
                .andExpect(jsonPath("$.data.dueReviews").isArray())
                .andExpect(jsonPath("$.data.wrongQuestions").isArray());

        mockMvc.perform(get("/api/v1/knowledge-map"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generatedAt").isString())
                .andExpect(jsonPath("$.data.totalQuestionCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.categories[0].topics[0].stateDescription").isString());

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalQuestionCount").isNumber());
    }

    @Test
    void shouldPublishV1PathsInOpenApi() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/workbench']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/knowledge-map']").exists());
    }
}
