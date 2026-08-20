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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证骨架阶段必须可启动，并且健康检查、traceId 和统一异常响应已接通。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(JavaInterviewLabApplicationTest.TestFailureController.class)
class JavaInterviewLabApplicationTest {

    private static final String TEST_TRACE_ID = "test-trace-001";

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

    @TestConfiguration
    @RestController
    static class TestFailureController {

        @GetMapping("/test-support/business-error")
        void throwBusinessException() {
            throw new BusinessException(ApiErrorCode.BUSINESS_RULE_VIOLATED, "测试业务规则异常");
        }
    }
}
