package com.javainterviewlab.system.seed.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.system.seed.dto.SeedImportResponse;
import com.javainterviewlab.system.seed.repository.SeedMapper;
import com.javainterviewlab.study.plan.service.StudyPlanBootstrapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/** 将外部 JSON 转为受控内容数据；导入过程必须原子完成。 */
@Service
public class SeedImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeedImportService.class);

    private final ObjectMapper objectMapper;
    private final SeedMapper seedMapper;
    private final StudyPlanBootstrapService studyPlanBootstrapService;

    public SeedImportService(
            ObjectMapper objectMapper,
            SeedMapper seedMapper,
            StudyPlanBootstrapService studyPlanBootstrapService
    ) {
        this.objectMapper = objectMapper;
        this.seedMapper = seedMapper;
        this.studyPlanBootstrapService = studyPlanBootstrapService;
    }

    /** 任意结构或引用错误都会触发事务回滚，避免题库只导入一部分。 */
    @Transactional
    public SeedImportResponse importJson(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ApiErrorCode.VALIDATION_FAILED, "种子文件不能为空");
        }
        try {
            JsonNode root = objectMapper.readTree(file.getInputStream());
            validateRoot(root);
            String seedPack = required(root, "seedPack");
            String version = required(root, "version");
            importCategories(root.path("categories"));
            importTopics(root.path("topics"));
            ImportCount count = importQuestions(root.path("questions"));
            seedMapper.markImported(seedPack, version);
            // 题库导入后立即解析路线目标，避免用户刷新页面或重启应用才看到每日题目。
            studyPlanBootstrapService.ensureSystemPlans();
            LOGGER.info("种子导入完成, seedPack={}, version={}, created={}, skipped={}", seedPack, version, count.created, count.skipped);
            return new SeedImportResponse(seedPack, version, count.created, count.skipped);
        } catch (IOException exception) {
            throw new BusinessException(ApiErrorCode.REQUEST_BODY_INVALID, "种子文件无法读取");
        }
    }

    private void validateRoot(JsonNode root) {
        if (root == null || !root.isObject() || !root.path("categories").isObject()
                || !root.path("topics").isArray() || !root.path("questions").isArray()) {
            throw new BusinessException(ApiErrorCode.VALIDATION_FAILED, "种子 JSON 结构不合法");
        }
    }

    private void importCategories(JsonNode categories) {
        Iterator<Map.Entry<String, JsonNode>> iterator = categories.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> item = iterator.next();
            seedMapper.upsertCategory(item.getKey(), item.getValue().asText());
        }
    }

    private void importTopics(JsonNode topics) {
        for (JsonNode topic : topics) {
            Long categoryId = seedMapper.findCategoryId(required(topic, "category"));
            if (categoryId == null) {
                throw new BusinessException(ApiErrorCode.VALIDATION_FAILED, "专题引用的分类不存在");
            }
            seedMapper.upsertTopic(categoryId, required(topic, "externalKey"), required(topic, "name"), topic.path("starLevel").asInt(3));
        }
    }

    private ImportCount importQuestions(JsonNode questions) {
        int created = 0;
        int skipped = 0;
        for (JsonNode question : questions) {
            String externalKey = required(question, "externalKey");
            if (seedMapper.questionExists(externalKey) > 0) {
                skipped++;
                continue;
            }
            Long topicId = seedMapper.findTopicId(required(question, "topic"));
            if (topicId == null) {
                throw new BusinessException(ApiErrorCode.VALIDATION_FAILED, "题目引用的专题不存在");
            }
            Long questionId = seedMapper.insertQuestion(topicId, externalKey, required(question, "title"), question.path("starLevel").asInt(3), required(question, "difficulty"), required(question, "frequencyLevel"), question.path("oneLiner").asText(null), question.path("plainExplanation").asText(null), question.path("designReason").asText(null));
            importAnswers(questionId, question.path("answers"));
            importFollowUps(questionId, question.path("followUps"));
            created++;
        }
        return new ImportCount(created, skipped);
    }

    private void importAnswers(Long questionId, JsonNode answers) {
        int sortOrder = 0;
        Iterator<Map.Entry<String, JsonNode>> iterator = answers.fields();
        while (iterator.hasNext()) {
            Map.Entry<String, JsonNode> answer = iterator.next();
            seedMapper.insertAnswer(questionId, answer.getKey(), answer.getValue().asText(), sortOrder++);
        }
    }

    private void importFollowUps(Long questionId, JsonNode followUps) {
        int sortOrder = 0;
        for (JsonNode followUp : followUps) {
            seedMapper.insertFollowUp(questionId, followUp.asText(), sortOrder++);
        }
    }

    private String required(JsonNode node, String field) {
        String value = node.path(field).asText();
        if (value == null || value.isBlank()) {
            throw new BusinessException(ApiErrorCode.VALIDATION_FAILED, "种子字段缺失：" + field);
        }
        return value;
    }

    private record ImportCount(int created, int skipped) {
    }
}
