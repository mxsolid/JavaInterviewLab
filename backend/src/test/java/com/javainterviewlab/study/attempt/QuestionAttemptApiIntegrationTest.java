package com.javainterviewlab.study.attempt;

import com.jayway.jsonpath.JsonPath;
import com.javainterviewlab.study.progress.service.StudyProgressService;
import com.javainterviewlab.study.review.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证答题历史只追加一次，并由数据库 UUID 唯一键处理重试。 */
@SpringBootTest
@AutoConfigureMockMvc
class QuestionAttemptApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @SpyBean
    private StudyProgressService studyProgressService;

    @SpyBean
    private ReviewService reviewService;

    /** 同一 UUID 重试返回原历史，不同 UUID 才产生新的历史事实。 */
    @Test
    @Transactional
    void shouldAppendAttemptIdempotently() throws Exception {
        long questionId = firstEnabledQuestionId();
        UUID firstAttemptId = UUID.randomUUID();
        String firstPayload = payload(questionId, firstAttemptId, 4, 1200L, "CORRECT");

        String firstBody = mockMvc.perform(post("/api/study/attempts")
                        .contentType(MediaType.APPLICATION_JSON).content(firstPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attempt.questionId").value(questionId))
                .andExpect(jsonPath("$.data.attempt.resultType").value("CORRECT"))
                .andExpect(jsonPath("$.data.progress.attemptCount").value(1))
                .andReturn().getResponse().getContentAsString();
        Number firstId = JsonPath.read(firstBody, "$.data.attempt.id");

        String duplicateBody = mockMvc.perform(post("/api/study/attempts")
                        .contentType(MediaType.APPLICATION_JSON).content(payload(questionId, firstAttemptId, 1, 1L, "WRONG")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.attempt.resultType").value("CORRECT"))
                .andExpect(jsonPath("$.data.progress").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        Number duplicateId = JsonPath.read(duplicateBody, "$.data.attempt.id");
        assertThat(duplicateId.longValue()).isEqualTo(firstId.longValue());

        String secondBody = mockMvc.perform(post("/api/study/attempts")
                        .contentType(MediaType.APPLICATION_JSON).content(payload(questionId, UUID.randomUUID(), 3, 300L, "PARTIAL")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Number secondId = JsonPath.read(secondBody, "$.data.attempt.id");
        assertThat(secondId.longValue()).isNotEqualTo(firstId.longValue());
    }

    /** 不存在题目和非法数值在进入 Service 前后均返回受控错误。 */
    @Test
    @Transactional
    void shouldRejectMissingQuestionAndInvalidValues() throws Exception {
        mockMvc.perform(post("/api/study/attempts").contentType(MediaType.APPLICATION_JSON)
                        .content(payload(99999999L, UUID.randomUUID(), 3, 0L, "PARTIAL")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        long questionId = firstEnabledQuestionId();
        mockMvc.perform(post("/api/study/attempts").contentType(MediaType.APPLICATION_JSON)
                        .content(payload(questionId, UUID.randomUUID(), 6, 0L, "CORRECT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        mockMvc.perform(post("/api/study/attempts").contentType(MediaType.APPLICATION_JSON)
                        .content(payload(questionId, UUID.randomUUID(), 3, -1L, "CORRECT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    /** progress 写入异常时，提交事务必须连同已插入的历史一起回滚。 */
    @Test
    void shouldRollbackAttemptWhenProgressUpdateFails() throws Exception {
        long questionId = firstEnabledQuestionId();
        UUID attemptId = UUID.randomUUID();
        doThrow(new IllegalStateException("模拟 progress 写入失败"))
                .when(studyProgressService).applyAttempt(any());
        try {
            mockMvc.perform(post("/api/study/attempts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload(questionId, attemptId, 3, 100L, "PARTIAL")))
                    .andExpect(status().isInternalServerError());

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM question_attempt WHERE client_attempt_id = ?",
                    Integer.class,
                    attemptId
            );
            assertThat(count).isZero();
        } finally {
            reset(studyProgressService);
        }
    }

    /** review 是提交事务的第三段；其失败也不能留下历史或 progress 半成品。 */
    @Test
    void shouldRollbackAttemptAndProgressWhenReviewScheduleFails() throws Exception {
        long questionId = firstEnabledQuestionId();
        UUID attemptId = UUID.randomUUID();
        doThrow(new IllegalStateException("模拟 review 写入失败"))
                .when(reviewService).scheduleAfterAttempt(any(), any());
        try {
            mockMvc.perform(post("/api/study/attempts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(payload(questionId, attemptId, 4, 100L, "CORRECT")))
                    .andExpect(status().isInternalServerError());

            Integer attemptCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM question_attempt WHERE client_attempt_id = ?", Integer.class, attemptId);
            Integer progressCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM study_progress WHERE question_id = ? AND last_studied_at >= CURRENT_TIMESTAMP - INTERVAL '1 minute'",
                    Integer.class,
                    questionId
            );
            assertThat(attemptCount).isZero();
            assertThat(progressCount).isZero();
        } finally {
            reset(reviewService);
        }
    }

    private long firstEnabledQuestionId() throws Exception {
        String body = mockMvc.perform(get("/api/questions").param("pageSize", "1").param("status", "ENABLED"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        Number id = JsonPath.read(body, "$.data.items[0].id");
        return id.longValue();
    }

    private String payload(long questionId, UUID attemptId, int selfRating, long elapsedMs, String resultType) {
        return "{\"questionId\":" + questionId + ",\"clientAttemptId\":\"" + attemptId
                + "\",\"answerText\":\"不记录到日志的测试答案\",\"viewedAnswer\":true,\"selfRating\":"
                + selfRating + ",\"resultType\":\"" + resultType + "\",\"elapsedMs\":" + elapsedMs + "}";
    }
}
