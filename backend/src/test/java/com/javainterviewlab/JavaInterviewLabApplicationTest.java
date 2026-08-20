package com.javainterviewlab;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

/**
 * 验证骨架阶段必须可启动，并且健康检查、traceId 和统一异常响应已接通。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(JavaInterviewLabApplicationTest.TestFailureController.class)
class JavaInterviewLabApplicationTest {

    private static final String TEST_TRACE_ID = "AB12CD";
    private static final String TRACE_ID_PATTERN = "[A-Z0-9]{6}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointShouldBeAvailableAndReturnTraceId() throws Exception {
        mockMvc.perform(get("/actuator/health").header("X-Trace-Id", TEST_TRACE_ID))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Trace-Id", TEST_TRACE_ID))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void businessExceptionShouldUseUnifiedResponse() throws Exception {
        mockMvc.perform(get("/test-support/business-error").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_RULE_VIOLATED"))
                .andExpect(jsonPath("$.traceId").isNotEmpty());
    }

    @Test
    void missingFaviconShouldReturnNormalNotFoundResponse() throws Exception {
        mockMvc.perform(get("/favicon.ico").header("X-Trace-Id", "invalid-trace-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(result -> assertThat(result.getResponse().getHeader("X-Trace-Id"))
                        .matches(TRACE_ID_PATTERN));
    }

    /** 内容编辑接口共用一个测试事务，既验证真实 SQL，又不污染本地开发题库。 */
    @Test
    @Transactional
    void contentCrudShouldCreateAndVersionCheckQuestion() throws Exception {
        long unique = System.nanoTime();
        String categoryJson = "{\"code\":\"TEST" + unique + "\",\"name\":\"测试分类\"}";
        String categoryBody = mockMvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON).content(categoryJson))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true)).andReturn().getResponse().getContentAsString();
        long categoryId = ((Number) com.jayway.jsonpath.JsonPath.read(categoryBody, "$.data.id")).longValue();
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.id == " + categoryId + ")].name").value("测试分类"));
        String topicJson = "{\"categoryId\":" + categoryId + ",\"code\":\"test-" + unique + "\",\"name\":\"测试专题\",\"starLevel\":3}";
        String topicBody = mockMvc.perform(post("/api/topics").contentType(MediaType.APPLICATION_JSON).content(topicJson))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        long topicId = ((Number) com.jayway.jsonpath.JsonPath.read(topicBody, "$.data.id")).longValue();
        mockMvc.perform(get("/api/topics").param("categoryId", String.valueOf(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(topicId))
                .andExpect(jsonPath("$.data[0].categoryName").value("测试分类"));
        String questionJson = "{\"topicId\":" + topicId + ",\"title\":\"测试题目\",\"starLevel\":3,\"difficulty\":\"MEDIUM\",\"frequencyLevel\":\"HIGH\",\"version\":0,\"tagIds\":[],\"answers\":[{\"answerType\":\"QUICK_30S\",\"content\":\"测试答案\"}],\"followUps\":[]}";
        String questionBody = mockMvc.perform(post("/api/questions").contentType(MediaType.APPLICATION_JSON).content(questionJson))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.answers[0].content").value("测试答案")).andReturn().getResponse().getContentAsString();
        long questionId = ((Number) com.jayway.jsonpath.JsonPath.read(questionBody, "$.data.id")).longValue();
        mockMvc.perform(get("/api/questions").param("topicId", String.valueOf(topicId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items[0].id").value(questionId));
        mockMvc.perform(get("/api/questions/{id}", questionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.answers[0].content").value("测试答案"));
        String updateJson = questionJson.replace("\"title\":\"测试题目\"", "\"title\":\"测试题目已更新\"");
        mockMvc.perform(put("/api/questions/{id}", questionId).contentType(MediaType.APPLICATION_JSON).content(updateJson))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.version").value(1));
        mockMvc.perform(put("/api/questions/{id}", questionId).contentType(MediaType.APPLICATION_JSON).content(updateJson))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("VERSION_CONFLICT"));
    }

    @TestConfiguration
    @RestController
    static class TestFailureController {

        @GetMapping("/test-support/business-error")
        void throwBusinessException() {
            throw new BusinessException(ApiErrorCode.BUSINESS_RULE_VIOLATED, "测试业务规则异常");
        }
    }
}
