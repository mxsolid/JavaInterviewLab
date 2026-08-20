package com.javainterviewlab.system.seed.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.content.question.domain.AnswerType;
import com.javainterviewlab.content.question.domain.Difficulty;
import com.javainterviewlab.content.question.domain.FrequencyLevel;
import com.javainterviewlab.content.question.domain.OriginType;
import com.javainterviewlab.content.question.domain.QuestionType;
import com.javainterviewlab.system.seed.domain.SeedImportMode;
import com.javainterviewlab.system.seed.domain.SeedPackContent;
import com.javainterviewlab.system.seed.domain.SeedPackContent.SeedFollowUp;
import com.javainterviewlab.system.seed.domain.SeedPackContent.SeedQuestion;
import com.javainterviewlab.system.seed.domain.SeedPackContent.SeedTopic;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** 将上传字节严格解析为完整校验后的 Seed V2 模型。 */
@Component
public class SeedPackParser {

    private static final int MAX_FILE_BYTES = 10 * 1024 * 1024;
    private static final int MAX_QUESTION_COUNT = 5_000;
    private static final Pattern CODE_PATTERN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9_-]{0,119}");
    private static final Pattern TAG_PATTERN = Pattern.compile("[a-z0-9][a-z0-9-]{0,63}");
    private static final Pattern DANGEROUS_HTML_PATTERN = Pattern.compile(
            "(?i)<\\s*/?\\s*(script|iframe|object|embed)\\b"
    );
    private static final Set<String> ANSWER_TYPES = enumNames(AnswerType.values());
    private static final Set<String> DIFFICULTIES = enumNames(Difficulty.values());
    private static final Set<String> FREQUENCIES = enumNames(FrequencyLevel.values());
    private static final Set<String> QUESTION_TYPES = enumNames(QuestionType.values());
    private static final Set<String> ORIGIN_TYPES = enumNames(OriginType.values());
    private static final Set<String> STATUSES = Set.of("ENABLED", "DISABLED");

    private final ObjectMapper strictObjectMapper;

    public SeedPackParser(ObjectMapper objectMapper) {
        this.strictObjectMapper = objectMapper.copy().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    }

    /** checksum 基于上传原始字节，换行或内容发生变化都会产生新摘要。 */
    public SeedPackContent parse(byte[] bytes) {
        if (bytes.length == 0) {
            throw validation("种子文件不能为空");
        }
        if (bytes.length > MAX_FILE_BYTES) {
            throw validation("种子文件不能超过 10MB");
        }
        try {
            JsonNode root = strictObjectMapper.readTree(bytes);
            validateRoot(root);
            String seedPack = required(root, "seedPack", "种子包");
            if (seedPack.length() > 120 || !CODE_PATTERN.matcher(seedPack).matches()) {
                throw validation("seedPack 必须是 1~120 位字母、数字、下划线或连字符");
            }
            String version = required(root, "version", "种子包");
            if (version.length() > 64) {
                throw validation("version 不能超过 64 个字符");
            }
            SeedImportMode mode = parseMode(root.path("mode").asText("UPSERT"));
            Map<String, String> categories = parseCategories(root.path("categories"));
            List<SeedTopic> topics = parseTopics(root.path("topics"));
            List<String> warnings = new ArrayList<>();
            List<SeedQuestion> questions = parseQuestions(root.path("questions"), warnings);
            if (questions.size() > MAX_QUESTION_COUNT) {
                throw validation("单个种子包题目不能超过 5000 道");
            }
            return new SeedPackContent(
                    seedPack,
                    version,
                    sha256(bytes),
                    mode,
                    Map.copyOf(categories),
                    List.copyOf(topics),
                    List.copyOf(questions),
                    List.copyOf(warnings)
            );
        } catch (BusinessException exception) {
            throw exception;
        } catch (IOException exception) {
            throw validation("种子 JSON 无法解析或包含重复字段");
        }
    }

    private void validateRoot(JsonNode root) {
        if (root == null || !root.isObject()
                || !root.path("categories").isObject()
                || !root.path("topics").isArray()
                || !root.path("questions").isArray()) {
            throw validation("种子 JSON 必须包含 categories、topics 和 questions");
        }
    }

    private Map<String, String> parseCategories(JsonNode node) {
        Map<String, String> categories = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String code = field.getKey();
            if (code.length() > 64 || !CODE_PATTERN.matcher(code).matches()) {
                throw validation("分类 code 不合法：" + code);
            }
            String name = textValue(field.getValue(), "分类 " + code + " 的名称");
            requireMaxLength(name, 100, "分类 " + code + " 的名称");
            rejectDangerousHtml(name, "分类 " + code);
            categories.put(code, name);
        }
        return categories;
    }

    private List<SeedTopic> parseTopics(JsonNode node) {
        List<SeedTopic> topics = new ArrayList<>();
        Set<String> externalKeys = new HashSet<>();
        for (JsonNode item : node) {
            String externalKey = required(item, "externalKey", "专题");
            if (externalKey.length() > 64
                    || !CODE_PATTERN.matcher(externalKey).matches()
                    || !externalKeys.add(externalKey)) {
                throw validation("专题 externalKey 重复或不合法：" + externalKey);
            }
            String category = required(item, "category", "专题 " + externalKey);
            String name = required(item, "name", "专题 " + externalKey);
            requireMaxLength(name, 160, "专题 " + externalKey + " 的名称");
            int starLevel = integer(item, "starLevel", 1, 5, "专题 " + externalKey);
            rejectDangerousHtml(name, "专题 " + externalKey);
            topics.add(new SeedTopic(externalKey, category, name, starLevel));
        }
        return topics;
    }

    private List<SeedQuestion> parseQuestions(JsonNode node, List<String> warnings) {
        List<SeedQuestion> questions = new ArrayList<>();
        Set<String> externalKeys = new HashSet<>();
        int legacyFollowUpCount = 0;
        for (JsonNode item : node) {
            String externalKey = required(item, "externalKey", "题目");
            if (!CODE_PATTERN.matcher(externalKey).matches() || !externalKeys.add(externalKey)) {
                throw validation("题目 externalKey 重复或不合法：" + externalKey);
            }
            int starLevel = integer(item, "starLevel", 1, 5, "题目 " + externalKey);
            String difficulty = enumValue(item, "difficulty", DIFFICULTIES, null, externalKey);
            String frequency = enumValue(item, "frequencyLevel", FREQUENCIES, null, externalKey);
            String questionType = enumValue(item, "questionType", QUESTION_TYPES, "KNOWLEDGE", externalKey);
            String originType = enumValue(item, "originType", ORIGIN_TYPES, "IMPORTED", externalKey);
            if ("USER".equals(originType) || "FUTURE_AI".equals(originType)) {
                throw validation("种子包不能创建 USER 或 FUTURE_AI 题目：" + externalKey);
            }
            String status = enumValue(item, "status", STATUSES, "ENABLED", externalKey);
            Map<String, String> answers = parseAnswers(item.path("answers"), externalKey, starLevel);
            FollowUpResult followUpResult = parseFollowUps(item.path("followUps"), externalKey, starLevel);
            legacyFollowUpCount += followUpResult.legacyCount();
            List<String> tags = parseTags(item.path("tags"), externalKey);
            SeedQuestion question = new SeedQuestion(
                    externalKey,
                    required(item, "topic", "题目 " + externalKey),
                    content(item, "title", externalKey),
                    starLevel,
                    difficulty,
                    frequency,
                    questionType,
                    originType,
                    status,
                    content(item, "oneLiner", externalKey),
                    content(item, "plainExplanation", externalKey),
                    content(item, "designReason", externalKey),
                    content(item, "commonMistakes", externalKey),
                    content(item, "scorePoints", externalKey),
                    Collections.unmodifiableMap(new LinkedHashMap<>(answers)),
                    List.copyOf(followUpResult.followUps()),
                    List.copyOf(tags),
                    sourceVersion(item, externalKey)
            );
            questions.add(question);
        }
        if (legacyFollowUpCount > 0) {
            warnings.add("检测到 " + legacyFollowUpCount + " 条旧字符串追问；已保留标题，未编造 referenceAnswer");
        }
        return questions;
    }

    private Map<String, String> parseAnswers(JsonNode node, String externalKey, int starLevel) {
        if (!node.isObject()) {
            throw validation("题目 " + externalKey + " 的 answers 必须是对象");
        }
        Map<String, String> answers = new LinkedHashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (!ANSWER_TYPES.contains(field.getKey())) {
                throw validation("题目 " + externalKey + " 存在未知 answerType：" + field.getKey());
            }
            String content = textValue(field.getValue(), "题目 " + externalKey + " 的答案 " + field.getKey());
            rejectDangerousHtml(content, "题目 " + externalKey);
            answers.put(field.getKey(), content);
        }
        if (starLevel == 5 && !answers.keySet().containsAll(ANSWER_TYPES)) {
            throw validation("五星题必须包含 QUICK_30S、STANDARD、DEEP：" + externalKey);
        }
        return answers;
    }

    private FollowUpResult parseFollowUps(JsonNode node, String externalKey, int starLevel) {
        if (!node.isArray()) {
            throw validation("题目 " + externalKey + " 的 followUps 必须是数组");
        }
        List<SeedFollowUp> followUps = new ArrayList<>();
        Set<String> titles = new HashSet<>();
        int legacyCount = 0;
        for (JsonNode item : node) {
            String title;
            String referenceAnswer = null;
            if (item.isTextual()) {
                title = textValue(item, "题目 " + externalKey + " 的追问");
                legacyCount++;
            } else if (item.isObject()) {
                title = required(item, "title", "题目 " + externalKey + " 的追问");
                referenceAnswer = optionalText(item, "referenceAnswer");
            } else {
                throw validation("题目 " + externalKey + " 的追问格式不合法");
            }
            if (!titles.add(title)) {
                throw validation("题目 " + externalKey + " 的追问标题重复：" + title);
            }
            rejectDangerousHtml(title, "题目 " + externalKey);
            rejectDangerousHtml(referenceAnswer, "题目 " + externalKey);
            followUps.add(new SeedFollowUp(title, referenceAnswer));
        }
        if (starLevel == 5 && followUps.size() < 3) {
            throw validation("五星题至少需要 3 个追问：" + externalKey);
        }
        return new FollowUpResult(followUps, legacyCount);
    }

    private List<String> parseTags(JsonNode node, String externalKey) {
        if (!node.isArray()) {
            throw validation("题目 " + externalKey + " 的 tags 必须是数组");
        }
        Set<String> tags = new LinkedHashSet<>();
        for (JsonNode item : node) {
            String tag = textValue(item, "题目 " + externalKey + " 的标签");
            if (!TAG_PATTERN.matcher(tag).matches()) {
                throw validation("题目 " + externalKey + " 的标签不合法：" + tag);
            }
            if (!tags.add(tag)) {
                throw validation("题目 " + externalKey + " 的标签重复：" + tag);
            }
        }
        return List.copyOf(tags);
    }

    private String content(JsonNode item, String field, String externalKey) {
        String value = required(item, field, "题目 " + externalKey);
        rejectDangerousHtml(value, "题目 " + externalKey + " 的 " + field);
        return value;
    }

    private String sourceVersion(JsonNode item, String externalKey) {
        String value = content(item, "sourceVersion", externalKey);
        requireMaxLength(value, 300, "题目 " + externalKey + " 的 sourceVersion");
        return value;
    }

    private String enumValue(JsonNode item, String field, Set<String> values, String defaultValue, String key) {
        String value = item.path(field).asText(defaultValue);
        if (value == null || !values.contains(value)) {
            throw validation("题目 " + key + " 的 " + field + " 不合法");
        }
        return value;
    }

    private SeedImportMode parseMode(String mode) {
        try {
            return SeedImportMode.valueOf(mode);
        } catch (IllegalArgumentException exception) {
            throw validation("mode 仅支持 INSERT_ONLY 或 UPSERT");
        }
    }

    private int integer(JsonNode item, String field, int min, int max, String context) {
        JsonNode value = item.path(field);
        if (!value.isIntegralNumber() || value.asInt() < min || value.asInt() > max) {
            throw validation(context + " 的 " + field + " 必须在 " + min + "~" + max + " 之间");
        }
        return value.asInt();
    }

    private String required(JsonNode node, String field, String context) {
        String value = optionalText(node, field);
        if (value == null) {
            throw validation(context + " 缺少字段：" + field);
        }
        return value;
    }

    private String optionalText(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }
        return textValue(value, field);
    }

    private String textValue(JsonNode node, String context) {
        if (!node.isTextual() || node.asText().isBlank()) {
            throw validation(context + " 必须是非空字符串");
        }
        return node.asText().trim();
    }

    private void rejectDangerousHtml(String value, String context) {
        if (value != null && DANGEROUS_HTML_PATTERN.matcher(value).find()) {
            throw validation(context + " 包含不允许的 HTML 标签");
        }
    }

    private void requireMaxLength(String value, int maxLength, String context) {
        if (value.length() > maxLength) {
            throw validation(context + " 不能超过 " + maxLength + " 个字符");
        }
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", exception);
        }
    }

    private static Set<String> enumNames(Enum<?>[] values) {
        Set<String> names = new HashSet<>();
        for (Enum<?> value : values) {
            names.add(value.name());
        }
        return Set.copyOf(names);
    }

    private BusinessException validation(String message) {
        return new BusinessException(ApiErrorCode.CONTENT_VALIDATION_FAILED, message);
    }

    private record FollowUpResult(List<SeedFollowUp> followUps, int legacyCount) {
    }
}
