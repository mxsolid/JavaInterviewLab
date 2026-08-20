package com.javainterviewlab.system.seed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.system.seed.dto.SeedImportResponse;
import com.javainterviewlab.system.seed.dto.SeedValidationResponse;
import com.javainterviewlab.system.seed.service.SeedImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Seed V2 的事务、版本、来源保护和完整字段集成验证。 */
@SpringBootTest
@AutoConfigureMockMvc
class SeedImportV2IntegrationTest {

    private static final String CORE_BANK_CHECKSUM =
            "a1f91d51d5fe1dbc687770bc3d88a0822eae4f1210327741599b5a56f301a5ec";

    @Autowired
    private SeedImportService seedImportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Transactional
    void shouldValidateBundledCoreBank() throws Exception {
        byte[] bytes = new ClassPathResource("seed/v03-core-complete.json").getInputStream().readAllBytes();
        SeedValidationResponse validation = seedImportService.validate(file(bytes));

        assertThat(validation.version()).isEqualTo("2026.08.21.2");
        assertThat(validation.checksumSha256()).isEqualTo(CORE_BANK_CHECKSUM);
        assertThat(validation.questionCount()).isEqualTo(336);
        assertThat(validation.created() + validation.updated() + validation.skipped()).isEqualTo(336);
        assertThat(validation.warnings()).isEmpty();
    }

    @Test
    @Transactional
    void shouldValidateDryRunImportFullContentAndRepeatWithoutMutation() throws Exception {
        String suffix = uniqueSuffix();
        String seedPack = "p02-first-" + suffix;
        String questionKey = "q-first-" + suffix;
        byte[] bytes = pack(seedPack, "1", "UPSERT", question(questionKey, "完整字段题", "topic-" + suffix));

        mockMvc.perform(multipart("/api/v1/system/seeds/validate").file(file(bytes)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.created").value(1));
        mockMvc.perform(multipart("/api/v1/system/seeds/import")
                        .file(file(bytes))
                        .param("dryRun", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.dryRun").value(true))
                .andExpect(jsonPath("$.data.created").value(1));
        assertThat(questionCount(questionKey)).isZero();

        SeedImportResponse first = seedImportService.importJson(file(bytes), false);
        assertThat(first.created()).isEqualTo(1);
        assertThat(first.updated()).isZero();
        assertThat(jdbcTemplate.queryForMap(
                "SELECT common_mistakes, score_points, source_version, seed_pack FROM question WHERE external_key = ?",
                questionKey
        )).containsEntry("common_mistakes", "易错点").containsEntry("score_points", "得分点")
                .containsEntry("source_version", "OpenJDK 21").containsEntry("seed_pack", seedPack);
        assertThat(childCount(questionKey, "question_answer")).isEqualTo(3);
        assertThat(childCount(questionKey, "question_follow_up")).isEqualTo(3);
        assertThat(childCount(questionKey, "question_tag")).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM question_follow_up f JOIN question q ON q.id = f.question_id "
                        + "WHERE q.external_key = ? AND f.reference_answer IS NOT NULL",
                Integer.class,
                questionKey
        )).isEqualTo(3);

        SeedImportResponse repeated = seedImportService.importJson(file(bytes), false);
        assertThat(repeated.created()).isZero();
        assertThat(repeated.updated()).isZero();
        assertThat(repeated.skipped()).isEqualTo(1);
        assertThat(childCount(questionKey, "question_answer")).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM seed_import_history WHERE seed_pack = ? AND version = '1'",
                Integer.class,
                seedPack
        )).isEqualTo(1);

        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/system/seeds/validate']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/system/seeds/import']").exists());
    }

    @Test
    @Transactional
    void shouldUpgradeOneQuestionAndHonorInsertOnly() throws Exception {
        String suffix = uniqueSuffix();
        String seedPack = "p02-upgrade-" + suffix;
        String questionKey = "q-upgrade-" + suffix;
        seedImportService.importJson(file(pack(
                seedPack, "1", "UPSERT", question(questionKey, "旧标题", "topic-" + suffix)
        )), false);

        SeedImportResponse upgraded = seedImportService.importJson(file(pack(
                seedPack, "2", "UPSERT", question(questionKey, "新标题", "topic-" + suffix)
        )), false);
        assertThat(upgraded.updated()).isEqualTo(1);
        assertThat(questionTitle(questionKey)).isEqualTo("新标题");
        assertThat(questionVersion(questionKey)).isEqualTo(1L);

        SeedImportResponse insertOnly = seedImportService.importJson(file(pack(
                seedPack, "3", "INSERT_ONLY", question(questionKey, "不应覆盖", "topic-" + suffix)
        )), false);
        assertThat(insertOnly.skipped()).isEqualTo(1);
        assertThat(questionTitle(questionKey)).isEqualTo("新标题");
        assertThat(questionVersion(questionKey)).isEqualTo(1L);
    }

    @Test
    @Transactional
    void shouldRollbackOnBrokenTopicReference() throws Exception {
        String suffix = uniqueSuffix();
        String seedPack = "p02-broken-" + suffix;
        Map<String, Object> question = question("q-broken-" + suffix, "坏引用题", "missing-topic-" + suffix);
        byte[] bytes = pack(seedPack, "1", "UPSERT", question);

        assertThatThrownBy(() -> seedImportService.importJson(file(bytes), false))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ApiErrorCode.CONTENT_VALIDATION_FAILED));
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM category WHERE code = ?", Integer.class, "CAT-" + suffix
        )).isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM seed_import_history WHERE seed_pack = ?", Integer.class, seedPack
        )).isZero();
    }

    @Test
    @Transactional
    void shouldRejectDuplicateExternalKeyAndDuplicateAnswerType() throws Exception {
        String suffix = uniqueSuffix();
        Map<String, Object> duplicate = question("q-duplicate-" + suffix, "重复题", "topic-" + suffix);
        byte[] duplicateQuestions = pack(
                "p02-duplicate-" + suffix,
                "1",
                "UPSERT",
                List.of(duplicate, new LinkedHashMap<>(duplicate))
        );
        assertThatThrownBy(() -> seedImportService.validate(file(duplicateQuestions)))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ApiErrorCode.CONTENT_VALIDATION_FAILED));

        String duplicateAnswerJson = """
                {
                  "seedPack":"p02-answer-%s","version":"1","categories":{},"topics":[],
                  "questions":[{"answers":{"QUICK_30S":"A","QUICK_30S":"B"}}]
                }
                """.formatted(suffix);
        assertThatThrownBy(() -> seedImportService.validate(file(
                duplicateAnswerJson.getBytes(StandardCharsets.UTF_8)
        ))).isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ApiErrorCode.CONTENT_VALIDATION_FAILED));
    }

    @Test
    @Transactional
    void shouldRejectChecksumMismatchForSameVersion() throws Exception {
        String suffix = uniqueSuffix();
        String seedPack = "p02-checksum-" + suffix;
        String questionKey = "q-checksum-" + suffix;
        seedImportService.importJson(file(pack(
                seedPack, "1", "UPSERT", question(questionKey, "版本内容 A", "topic-" + suffix)
        )), false);

        assertThatThrownBy(() -> seedImportService.importJson(file(pack(
                seedPack, "1", "UPSERT", question(questionKey, "版本内容 B", "topic-" + suffix)
        )), false)).isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ApiErrorCode.VERSION_CONFLICT));
    }

    @Test
    @Transactional
    void shouldRejectStalePack() throws Exception {
        String suffix = uniqueSuffix();
        String seedPack = "p02-stale-" + suffix;
        String questionKey = "q-stale-" + suffix;
        seedImportService.importJson(file(pack(
                seedPack, "2", "UPSERT", question(questionKey, "当前版本", "topic-" + suffix)
        )), false);

        assertThatThrownBy(() -> seedImportService.importJson(file(pack(
                seedPack, "1", "UPSERT", question(questionKey, "过期版本", "topic-" + suffix)
        )), false)).isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ApiErrorCode.VERSION_CONFLICT));
    }

    @Test
    @Transactional
    void shouldNeverOverwriteUserQuestion() throws Exception {
        String suffix = uniqueSuffix();
        String questionKey = "q-user-" + suffix;
        Long topicId = jdbcTemplate.queryForObject(
                "SELECT id FROM topic WHERE status = 'ENABLED' ORDER BY id LIMIT 1", Long.class
        );
        jdbcTemplate.update(
                "INSERT INTO question (topic_id, external_key, title, question_type, star_level, difficulty, "
                        + "frequency_level, origin_type, status) VALUES (?, ?, '用户原题', 'KNOWLEDGE', 5, "
                        + "'MEDIUM', 'VERY_HIGH', 'USER', 'ENABLED')",
                topicId,
                questionKey
        );

        assertThatThrownBy(() -> seedImportService.importJson(file(pack(
                "p02-user-" + suffix,
                "1",
                "UPSERT",
                question(questionKey, "种子覆盖内容", "topic-" + suffix)
        )), false)).isInstanceOfSatisfying(BusinessException.class, exception ->
                assertThat(exception.getErrorCode()).isEqualTo(ApiErrorCode.CONTENT_VALIDATION_FAILED));
        assertThat(questionTitle(questionKey)).isEqualTo("用户原题");
    }

    private byte[] pack(
            String seedPack,
            String version,
            String mode,
            Map<String, Object> question
    ) throws Exception {
        return pack(seedPack, version, mode, List.of(question));
    }

    private byte[] pack(
            String seedPack,
            String version,
            String mode,
            List<Map<String, Object>> questions
    ) throws Exception {
        String suffix = seedPack.substring(seedPack.lastIndexOf('-') + 1);
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("seedPack", seedPack);
        root.put("version", version);
        root.put("mode", mode);
        root.put("categories", Map.of("CAT-" + suffix, "测试分类"));
        root.put("topics", List.of(Map.of(
                "externalKey", "topic-" + suffix,
                "category", "CAT-" + suffix,
                "name", "测试专题",
                "starLevel", 5
        )));
        root.put("questions", questions);
        return objectMapper.writeValueAsBytes(root);
    }

    private Map<String, Object> question(String externalKey, String title, String topicCode) {
        Map<String, Object> question = new LinkedHashMap<>();
        question.put("externalKey", externalKey);
        question.put("topic", topicCode);
        question.put("title", title);
        question.put("starLevel", 5);
        question.put("difficulty", "MEDIUM");
        question.put("frequencyLevel", "VERY_HIGH");
        question.put("questionType", "KNOWLEDGE");
        question.put("originType", "IMPORTED");
        question.put("status", "ENABLED");
        question.put("oneLiner", "一句话理解");
        question.put("plainExplanation", "通俗讲解");
        question.put("designReason", "设计原因");
        question.put("commonMistakes", "易错点");
        question.put("scorePoints", "得分点");
        question.put("answers", Map.of(
                "QUICK_30S", "快速回答",
                "STANDARD", "标准回答",
                "DEEP", "深入回答"
        ));
        List<Map<String, String>> followUps = new ArrayList<>();
        for (int index = 1; index <= 3; index++) {
            followUps.add(Map.of("title", "追问 " + index, "referenceAnswer", "参考追问答案 " + index));
        }
        question.put("followUps", followUps);
        question.put("tags", List.of("java", "seed-v2"));
        question.put("sourceVersion", "OpenJDK 21");
        return question;
    }

    private MockMultipartFile file(byte[] bytes) {
        return new MockMultipartFile("file", "seed.json", "application/json", bytes);
    }

    private int questionCount(String externalKey) {
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM question WHERE external_key = ?", Integer.class, externalKey
        );
    }

    private int childCount(String externalKey, String childTable) {
        if (!List.of("question_answer", "question_follow_up", "question_tag").contains(childTable)) {
            throw new IllegalArgumentException("不允许的子表");
        }
        return jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + childTable + " c JOIN question q ON q.id = c.question_id "
                        + "WHERE q.external_key = ?",
                Integer.class,
                externalKey
        );
    }

    private String questionTitle(String externalKey) {
        return jdbcTemplate.queryForObject(
                "SELECT title FROM question WHERE external_key = ?", String.class, externalKey
        );
    }

    private long questionVersion(String externalKey) {
        return jdbcTemplate.queryForObject(
                "SELECT version FROM question WHERE external_key = ?", Long.class, externalKey
        );
    }

    private String uniqueSuffix() {
        return Long.toString(System.nanoTime());
    }
}
