package com.javainterviewlab.study.note;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** 验证首次笔记保存遇到同一唯一键时只保留一条记录并安全回读。 */
@SpringBootTest
@AutoConfigureMockMvc
class NoteServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 连续首次创建模拟两个自动保存入口，后一请求不能覆盖前一请求的内容。 */
    @Test
    @Transactional
    void shouldReturnExistingNoteWhenInitialCreateIsRepeated() throws Exception {
        long questionId = createEnabledQuestion();
        String firstBody = create(questionId, "先保存的笔记")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("先保存的笔记"))
                .andReturn().getResponse().getContentAsString();
        Number firstNoteId = JsonPath.read(firstBody, "$.data.id");

        String duplicateBody = create(questionId, "竞争请求的笔记")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").value("先保存的笔记"))
                .andExpect(jsonPath("$.data.version").value(0))
                .andReturn().getResponse().getContentAsString();
        Number duplicateNoteId = JsonPath.read(duplicateBody, "$.data.id");
        Integer noteCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM note WHERE target_type = 'QUESTION' AND target_id = ?", Integer.class, questionId
        );
        assertThat(duplicateNoteId.longValue()).isEqualTo(firstNoteId.longValue());
        assertThat(noteCount).isEqualTo(1);
    }

    private org.springframework.test.web.servlet.ResultActions create(long questionId, String content) throws Exception {
        return mockMvc.perform(post("/api/study/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"targetType\":\"QUESTION\",\"targetId\":" + questionId + ",\"content\":\"" + content + "\"}"));
    }

    private long createEnabledQuestion() {
        Long topicId = jdbcTemplate.queryForObject("SELECT id FROM topic WHERE status = 'ENABLED' ORDER BY id LIMIT 1", Long.class);
        return jdbcTemplate.queryForObject(
                "INSERT INTO question (topic_id, title, question_type, star_level, difficulty, frequency_level, origin_type, status) "
                        + "VALUES (?, ?, 'CONCEPT', 3, 'MEDIUM', 'HIGH', 'USER', 'ENABLED') RETURNING id",
                Long.class,
                topicId,
                "笔记创建测试题 " + System.nanoTime()
        );
    }
}
