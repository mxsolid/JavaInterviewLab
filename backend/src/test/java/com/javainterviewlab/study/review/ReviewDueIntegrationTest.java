package com.javainterviewlab.study.review;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证跨天未完成的复习仍会留在学习入口和首页待处理统计中。 */
@SpringBootTest
@AutoConfigureMockMvc
class ReviewDueIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 昨日和今日任务应可见，明日任务不应提前进入待处理集合。 */
    @Test
    @Transactional
    void shouldIncludeOverdueAndTodayPendingReviews() throws Exception {
        long profileId = jdbcTemplate.queryForObject(
                "SELECT id FROM profile WHERE is_default = TRUE", Long.class
        );
        long overdueQuestionId = createEnabledQuestion("逾期复习");
        long todayQuestionId = createEnabledQuestion("今日复习");
        long futureQuestionId = createEnabledQuestion("明日复习");
        insertPendingReview(profileId, overdueQuestionId, "CURRENT_TIMESTAMP - INTERVAL '1 day'");
        insertPendingReview(profileId, todayQuestionId, "CURRENT_TIMESTAMP");
        insertPendingReview(profileId, futureQuestionId, "CURRENT_TIMESTAMP + INTERVAL '1 day'");

        mockMvc.perform(get("/api/study/reviews/due"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.questionId == " + overdueQuestionId + ")].overdue").value(true))
                .andExpect(jsonPath("$.data[?(@.questionId == " + todayQuestionId + ")].overdue").value(false))
                .andExpect(jsonPath("$.data[?(@.questionId == " + futureQuestionId + ")]").isEmpty());

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dueReviewCount", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.data.todayReviewCount", greaterThanOrEqualTo(1)));
    }

    private long createEnabledQuestion(String suffix) {
        Long topicId = jdbcTemplate.queryForObject("SELECT id FROM topic WHERE status = 'ENABLED' ORDER BY id LIMIT 1", Long.class);
        return jdbcTemplate.queryForObject(
                "INSERT INTO question (topic_id, title, question_type, star_level, difficulty, frequency_level, origin_type, status) "
                        + "VALUES (?, ?, 'CONCEPT', 5, 'MEDIUM', 'HIGH', 'USER', 'ENABLED') RETURNING id",
                Long.class,
                topicId,
                "V02.1 " + suffix + " " + System.nanoTime()
        );
    }

    private void insertPendingReview(long profileId, long questionId, String dueAtExpression) {
        jdbcTemplate.update(
                "INSERT INTO review_task (profile_id, question_id, due_at, status) VALUES (?, ?, " + dueAtExpression + ", 'PENDING')",
                profileId,
                questionId
        );
    }
}
