package com.javainterviewlab.scenario;

import com.jayway.jsonpath.JsonPath;
import com.javainterviewlab.scenario.dto.ScenarioSeedImportResponse;
import com.javainterviewlab.scenario.service.ScenarioSeedImportService;
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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** P03 场景、Source、Lab 和 OpenAPI 的真实 PostgreSQL 验收。 */
@SpringBootTest
@AutoConfigureMockMvc
class ScenarioContentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ScenarioSeedImportService seedImportService;

    @Test
    void shouldImportCompleteScenarioBankAndExposeDatabaseMatrix() throws Exception {
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scenario", Integer.class)).isEqualTo(12);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scenario_case", Integer.class)).isEqualTo(60);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scenario_solution", Integer.class)).isEqualTo(12);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM scenario_case_solution", Integer.class)).isEqualTo(140);

        String listBody = mockMvc.perform(get("/api/v1/scenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(12)))
                .andReturn().getResponse().getContentAsString();
        Number scenarioId = JsonPath.read(listBody, "$.data[0].id");

        mockMvc.perform(get("/api/v1/scenarios/{id}", scenarioId.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cases", hasSize(5)))
                .andExpect(jsonPath("$.data.solutions", hasSize(12)))
                .andExpect(jsonPath("$.data.cases[0].expectedAnalysis", hasSize(5)));
        mockMvc.perform(get("/api/v1/scenarios/{id}/matrix", scenarioId.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.cases", hasSize(5)))
                .andExpect(jsonPath("$.data.solutions", hasSize(12)))
                .andExpect(jsonPath("$.data.cells").isArray())
                .andExpect(jsonPath("$.data.cells[0].recommendation").value("CANDIDATE"));

        ScenarioSeedImportResponse repeated = seedImportService.importBundled();
        assertThat(repeated.duplicated()).isTrue();
        assertThat(repeated.scenarioCount()).isEqualTo(12);
    }

    @Test
    @Transactional
    void shouldAppendScenarioAttemptsAndDeduplicateSameClientId() throws Exception {
        Long scenarioId = jdbcTemplate.queryForObject("SELECT id FROM scenario ORDER BY id LIMIT 1", Long.class);
        Long caseId = jdbcTemplate.queryForObject(
                "SELECT id FROM scenario_case WHERE scenario_id = ? ORDER BY sort_order LIMIT 1",
                Long.class,
                scenarioId
        );
        UUID clientAttemptId = UUID.randomUUID();
        String request = request(clientAttemptId, scenarioId, caseId);

        mockMvc.perform(post("/api/v1/scenario-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicated").value(false));
        mockMvc.perform(post("/api/v1/scenario-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicated").value(true));
        mockMvc.perform(post("/api/v1/scenario-attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request(UUID.randomUUID(), scenarioId, caseId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.duplicated").value(false));

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM scenario_attempt WHERE scenario_id = ?", Integer.class, scenarioId
        )).isEqualTo(2);
    }

    @Test
    void shouldExposeSourceLabAndOpenApiContracts() throws Exception {
        String sourceBody = mockMvc.perform(get("/api/v1/source-snippets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andReturn().getResponse().getContentAsString();
        Number sourceId = JsonPath.read(sourceBody, "$.data[0].id");
        mockMvc.perform(get("/api/v1/source-snippets/{id}", sourceId.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.codeText").isString())
                .andExpect(jsonPath("$.data.licenseName").value("PROJECT_ORIGINAL"));

        mockMvc.perform(get("/api/v1/labs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.data[*].algorithm", containsInAnyOrder(
                        "BPLUS_TREE_INSERT", "HASHMAP_RESIZE", "LRU_CACHE",
                        "THREAD_POOL_SUBMIT", "REDIS_REHASH")))
                .andExpect(jsonPath("$.data[0].initialDataset").isMap());
        mockMvc.perform(get("/api/v1/labs/hashmap-resize"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.algorithm").value("HASHMAP_RESIZE"));

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/scenarios']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/scenarios/{id}/matrix']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/scenario-attempts']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/source-snippets']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/labs']").exists());
    }

    private String request(UUID clientAttemptId, Long scenarioId, Long caseId) {
        return """
                {
                  "clientAttemptId":"%s",
                  "scenarioId":%d,
                  "caseId":%d,
                  "answerText":"先定义不变量，再分析竞态并用数据库约束兜底。",
                  "selfRating":4,
                  "resultType":"SOLID",
                  "durationSeconds":90
                }
                """.formatted(clientAttemptId, scenarioId, caseId);
    }
}
