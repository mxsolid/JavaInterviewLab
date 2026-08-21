package com.javainterviewlab.system.seed.service;

import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.system.seed.domain.SeedImportMode;
import com.javainterviewlab.system.seed.domain.SeedPackContent;
import com.javainterviewlab.system.seed.domain.SeedPackContent.SeedFollowUp;
import com.javainterviewlab.system.seed.domain.SeedPackContent.SeedQuestion;
import com.javainterviewlab.system.seed.domain.SeedPackContent.SeedTopic;
import com.javainterviewlab.system.seed.domain.SeedQuestionDecision;
import com.javainterviewlab.system.seed.dto.SeedImportResponse;
import com.javainterviewlab.system.seed.dto.SeedValidationResponse;
import com.javainterviewlab.system.seed.repository.SeedMapper;
import com.javainterviewlab.system.seed.repository.model.SeedHistoryRow;
import com.javainterviewlab.system.seed.repository.model.SeedQuestionRow;
import com.javainterviewlab.study.plan.service.StudyPlanBootstrapService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Seed V2 的校验、数据库决策和原子写入服务。 */
@Service
public class SeedImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeedImportService.class);

    private final SeedPackParser seedPackParser;
    private final SeedMapper seedMapper;
    private final StudyPlanBootstrapService studyPlanBootstrapService;

    public SeedImportService(
            SeedPackParser seedPackParser,
            SeedMapper seedMapper,
            StudyPlanBootstrapService studyPlanBootstrapService
    ) {
        this.seedPackParser = seedPackParser;
        this.seedMapper = seedMapper;
        this.studyPlanBootstrapService = studyPlanBootstrapService;
    }

    /** validate 会结合当前数据库计算决策，但不申请写锁或修改数据。 */
    @Transactional(readOnly = true)
    public SeedValidationResponse validate(MultipartFile file) {
        SeedPackContent pack = seedPackParser.parse(readBytes(file));
        ImportAnalysis analysis = analyze(pack);
        return new SeedValidationResponse(
                pack.seedPack(),
                pack.version(),
                pack.checksumSha256(),
                pack.importMode().name(),
                true,
                pack.questions().size(),
                analysis.created(),
                analysis.updated(),
                analysis.skipped(),
                pack.warnings()
        );
    }

    /** 保留 V0.1 入口的默认真实导入语义。 */
    @Transactional
    public SeedImportResponse importJson(MultipartFile file) {
        return importContent(file, false);
    }

    /** dryRun 与真实导入使用同一解析和决策代码；只有 false 分支允许写数据库。 */
    @Transactional
    public SeedImportResponse importJson(MultipartFile file, boolean dryRun) {
        return importContent(file, dryRun);
    }

    private SeedImportResponse importContent(MultipartFile file, boolean dryRun) {
        long startedAt = System.nanoTime();
        SeedPackContent pack = seedPackParser.parse(readBytes(file));
        if (!dryRun) {
            // 锁在 checksum/版本查询之前获取，防止同一包两个请求同时判断为首次导入。
            seedMapper.lockSeedPack(pack.seedPack());
        }
        ImportAnalysis analysis = analyze(pack);
        if (dryRun) {
            return response(pack, dryRun, analysis, elapsedMillis(startedAt));
        }
        if (analysis.alreadyImported()) {
            // 兼容曾在全新库中先执行 Flyway、后导入 Topic 而留下的 Source 空关联。
            seedMapper.linkSourceSnippetsToTopics();
            return response(pack, false, analysis, elapsedMillis(startedAt));
        }

        Map<String, Long> categoryIds = upsertCategories(pack);
        Map<String, Long> topicIds = upsertTopics(pack, categoryIds);
        seedMapper.linkSourceSnippetsToTopics();
        for (int index = 0; index < pack.questions().size(); index++) {
            SeedQuestion question = pack.questions().get(index);
            SeedQuestionDecision decision = analysis.decisions().get(index);
            if (decision == SeedQuestionDecision.SKIP) {
                continue;
            }
            Long topicId = requireTopicId(question.topicCode(), topicIds);
            Long questionId;
            if (decision == SeedQuestionDecision.CREATE) {
                questionId = seedMapper.insertQuestion(topicId, pack.seedPack(), question);
            } else {
                int updated = seedMapper.updateQuestion(topicId, pack.seedPack(), question);
                if (updated != 1) {
                    throw validation("题目来源在导入期间发生变化，已回滚：" + question.externalKey());
                }
                questionId = Objects.requireNonNull(
                        seedMapper.findQuestionByExternalKey(question.externalKey()),
                        "更新后的题目必须存在"
                ).id();
                // 只替换内容子表，attempt/progress/review/note/favorite 均不触碰。
                seedMapper.deleteAnswers(questionId);
                seedMapper.deleteFollowUps(questionId);
                seedMapper.deleteTags(questionId);
            }
            insertChildren(questionId, question);
        }

        long durationMs = elapsedMillis(startedAt);
        seedMapper.insertHistory(
                pack.seedPack(),
                pack.version(),
                pack.checksumSha256(),
                pack.importMode().name(),
                analysis.created(),
                analysis.updated(),
                analysis.skipped(),
                durationMs
        );
        seedMapper.markImported(pack.seedPack(), pack.version());
        // 路线目标解析与题库变更同事务提交，失败时不留下“已导入但路线未同步”的状态。
        studyPlanBootstrapService.syncSystemPlans();
        LOGGER.info(
                "种子导入完成, seedPack={}, version={}, created={}, updated={}, skipped={}, durationMs={}",
                pack.seedPack(), pack.version(), analysis.created(), analysis.updated(), analysis.skipped(), durationMs
        );
        return response(pack, false, analysis, durationMs);
    }

    private ImportAnalysis analyze(SeedPackContent pack) {
        validateReferences(pack);
        SeedHistoryRow sameVersion = seedMapper.findHistory(pack.seedPack(), pack.version());
        if (sameVersion != null) {
            if (!sameVersion.checksumSha256().equals(pack.checksumSha256())) {
                throw new BusinessException(
                        ApiErrorCode.VERSION_CONFLICT,
                        "同一 seedPack/version 的 checksum 不一致"
                );
            }
            return ImportAnalysis.alreadyImported(pack.questions().size());
        }
        SeedHistoryRow latest = seedMapper.findLatestHistory(pack.seedPack());
        if (latest != null && compareVersions(pack.version(), latest.version()) < 0) {
            throw new BusinessException(
                    ApiErrorCode.VERSION_CONFLICT,
                    "拒绝导入旧版本 " + pack.version() + "，当前最新版本为 " + latest.version()
            );
        }

        List<SeedQuestionDecision> decisions = new ArrayList<>(pack.questions().size());
        int created = 0;
        int updated = 0;
        int skipped = 0;
        for (SeedQuestion question : pack.questions()) {
            SeedQuestionRow existing = seedMapper.findQuestionByExternalKey(question.externalKey());
            SeedQuestionDecision decision = decideQuestion(pack, existing, question.externalKey());
            decisions.add(decision);
            switch (decision) {
                case CREATE -> created++;
                case UPDATE -> updated++;
                case SKIP -> skipped++;
            }
        }
        return new ImportAnalysis(List.copyOf(decisions), created, updated, skipped, false);
    }

    private SeedQuestionDecision decideQuestion(
            SeedPackContent pack,
            SeedQuestionRow existing,
            String externalKey
    ) {
        if (existing == null) {
            return SeedQuestionDecision.CREATE;
        }
        if ("USER".equals(existing.originType())) {
            throw validation("种子不得覆盖 USER 题目：" + externalKey);
        }
        if ("IMPORTED".equals(existing.originType())
                && !Objects.equals(existing.seedPack(), pack.seedPack())) {
            throw validation("IMPORTED 题目属于其他 seed namespace：" + externalKey);
        }
        if (!"BUILTIN".equals(existing.originType()) && !"IMPORTED".equals(existing.originType())) {
            throw validation("题目来源不允许由种子更新：" + externalKey);
        }
        return pack.importMode() == SeedImportMode.UPSERT
                ? SeedQuestionDecision.UPDATE
                : SeedQuestionDecision.SKIP;
    }

    private void validateReferences(SeedPackContent pack) {
        Map<String, Boolean> categoryExists = new HashMap<>();
        for (SeedTopic topic : pack.topics()) {
            if (pack.categories().containsKey(topic.categoryCode())) {
                continue;
            }
            boolean exists = categoryExists.computeIfAbsent(
                    topic.categoryCode(),
                    code -> seedMapper.findCategoryId(code) != null
            );
            if (!exists) {
                throw validation("专题引用的分类不存在：" + topic.categoryCode());
            }
        }

        Map<String, Boolean> packTopics = new HashMap<>();
        for (SeedTopic topic : pack.topics()) {
            packTopics.put(topic.externalKey(), true);
        }
        Map<String, Boolean> databaseTopicExists = new HashMap<>();
        for (SeedQuestion question : pack.questions()) {
            if (packTopics.containsKey(question.topicCode())) {
                continue;
            }
            boolean exists = databaseTopicExists.computeIfAbsent(
                    question.topicCode(),
                    code -> seedMapper.findTopicId(code) != null
            );
            if (!exists) {
                throw validation("题目引用的专题不存在：" + question.externalKey());
            }
        }
    }

    private Map<String, Long> upsertCategories(SeedPackContent pack) {
        Map<String, Long> ids = new HashMap<>();
        pack.categories().forEach((code, name) -> ids.put(code, seedMapper.upsertCategory(code, name)));
        return ids;
    }

    private Map<String, Long> upsertTopics(SeedPackContent pack, Map<String, Long> categoryIds) {
        Map<String, Long> ids = new HashMap<>();
        for (SeedTopic topic : pack.topics()) {
            Long categoryId = categoryIds.get(topic.categoryCode());
            if (categoryId == null) {
                categoryId = seedMapper.findCategoryId(topic.categoryCode());
            }
            if (categoryId == null) {
                throw validation("专题引用的分类不存在：" + topic.categoryCode());
            }
            ids.put(
                    topic.externalKey(),
                    seedMapper.upsertTopic(categoryId, topic.externalKey(), topic.name(), topic.starLevel())
            );
        }
        return ids;
    }

    private Long requireTopicId(String topicCode, Map<String, Long> topicIds) {
        Long topicId = topicIds.get(topicCode);
        if (topicId == null) {
            topicId = seedMapper.findTopicId(topicCode);
        }
        if (topicId == null) {
            throw validation("题目引用的专题不存在：" + topicCode);
        }
        return topicId;
    }

    private void insertChildren(Long questionId, SeedQuestion question) {
        int sortOrder = 0;
        for (Map.Entry<String, String> answer : question.answers().entrySet()) {
            seedMapper.insertAnswer(questionId, answer.getKey(), answer.getValue(), sortOrder++);
        }
        sortOrder = 0;
        for (SeedFollowUp followUp : question.followUps()) {
            seedMapper.insertFollowUp(
                    questionId,
                    followUp.title(),
                    followUp.referenceAnswer(),
                    sortOrder++
            );
        }
        for (String tag : question.tags()) {
            Long tagId = seedMapper.upsertTag(tag);
            seedMapper.insertQuestionTag(questionId, tagId);
        }
    }

    private SeedImportResponse response(
            SeedPackContent pack,
            boolean dryRun,
            ImportAnalysis analysis,
            long durationMs
    ) {
        return new SeedImportResponse(
                pack.seedPack(),
                pack.version(),
                pack.checksumSha256(),
                pack.importMode().name(),
                dryRun,
                pack.questions().size(),
                analysis.created(),
                analysis.updated(),
                analysis.skipped(),
                durationMs,
                pack.warnings()
        );
    }

    private byte[] readBytes(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw validation("种子文件不能为空");
        }
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new BusinessException(ApiErrorCode.REQUEST_BODY_INVALID, "种子文件无法读取");
        }
    }

    private int compareVersions(String left, String right) {
        String[] leftParts = left.split("[._-]");
        String[] rightParts = right.split("[._-]");
        int max = Math.max(leftParts.length, rightParts.length);
        for (int index = 0; index < max; index++) {
            String leftPart = index < leftParts.length ? leftParts[index] : "0";
            String rightPart = index < rightParts.length ? rightParts[index] : "0";
            int result = compareVersionPart(leftPart, rightPart);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

    private int compareVersionPart(String left, String right) {
        try {
            return Long.compare(Long.parseLong(left), Long.parseLong(right));
        } catch (NumberFormatException ignored) {
            return left.compareTo(right);
        }
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }

    private BusinessException validation(String message) {
        return new BusinessException(ApiErrorCode.CONTENT_VALIDATION_FAILED, message);
    }

    private record ImportAnalysis(
            List<SeedQuestionDecision> decisions,
            int created,
            int updated,
            int skipped,
            boolean alreadyImported
    ) {

        private static ImportAnalysis alreadyImported(int questionCount) {
            return new ImportAnalysis(List.of(), 0, 0, questionCount, true);
        }
    }
}
