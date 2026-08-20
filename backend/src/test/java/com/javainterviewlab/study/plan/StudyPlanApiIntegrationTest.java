package com.javainterviewlab.study.plan;

import com.jayway.jsonpath.JsonPath;
import com.javainterviewlab.study.plan.service.StudyPlanBootstrapService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证路线配置持久化、当前路线切换与 Day 1 查询可以组成 B01 闭环。 */
@SpringBootTest
@AutoConfigureMockMvc
class StudyPlanApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudyPlanBootstrapService studyPlanBootstrapService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 选择路线后必须从数据库读回当前路线和当日任务，不能由前端自行计算或伪造。 */
    @Test
    @Transactional
    void shouldPersistPresetPlanAndReturnTodayTaskAfterActivation() throws Exception {
        String plansBody = mockMvc.perform(get("/api/study/plans").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<Number> planIds = JsonPath.read(plansBody, "$.data[?(@.code == 'RECOMMENDED_15')].id");
        Number planId = planIds.getFirst();
        assertThat(planId).isNotNull();

        mockMvc.perform(post("/api/study/plans/{planId}/activate", planId.longValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planCode").value("RECOMMENDED_15"))
                .andExpect(jsonPath("$.data.timeProgressDay").value(1));

        mockMvc.perform(get("/api/study/current-plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(planId.longValue()));

        mockMvc.perform(get("/api/study/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentPlan.planId").value(planId.longValue()))
                .andExpect(jsonPath("$.data.day.dayNumber").value(1));
    }

    /**
     * 目录同步允许启动和种子导入重复执行；同一 profile 的路线切换始终只能留下一个 active 行。
     */
    @Test
    @Transactional
    void shouldSynchronizeIdempotentlyAndKeepSingleActivePlan() throws Exception {
        Integer planCountBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM study_plan", Integer.class);
        Integer itemCountBefore = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM study_plan_item", Integer.class);

        studyPlanBootstrapService.syncSystemPlans();
        studyPlanBootstrapService.syncSystemPlans();

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM study_plan", Integer.class))
                .isEqualTo(planCountBefore);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM study_plan_item", Integer.class))
                .isEqualTo(itemCountBefore);

        String plansBody = mockMvc.perform(get("/api/study/plans")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<Number> planIds = JsonPath.read(plansBody, "$.data[*].id");
        long firstPlanId = planIds.getFirst().longValue();
        long secondPlanId = planIds.get(1).longValue();

        mockMvc.perform(post("/api/study/plans/{planId}/activate", firstPlanId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/study/plans/{planId}/activate", secondPlanId)).andExpect(status().isOk());
        mockMvc.perform(post("/api/study/plans/{planId}/activate", firstPlanId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planId").value(firstPlanId));

        Integer activeCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM profile_plan WHERE profile_id = (SELECT id FROM profile WHERE is_default = TRUE) AND active = TRUE",
                Integer.class
        );
        Integer reactivatedPlanRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM profile_plan WHERE profile_id = (SELECT id FROM profile WHERE is_default = TRUE) AND study_plan_id = ?",
                Integer.class,
                firstPlanId
        );
        assertThat(activeCount).isEqualTo(1);
        assertThat(reactivatedPlanRows).isEqualTo(1);
    }
}
