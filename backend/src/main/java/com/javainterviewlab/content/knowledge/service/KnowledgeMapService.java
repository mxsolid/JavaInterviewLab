package com.javainterviewlab.content.knowledge.service;

import com.javainterviewlab.content.knowledge.domain.KnowledgeState;
import com.javainterviewlab.content.knowledge.dto.KnowledgeCategoryResponse;
import com.javainterviewlab.content.knowledge.dto.KnowledgeMapResponse;
import com.javainterviewlab.content.knowledge.dto.KnowledgeTopicResponse;
import com.javainterviewlab.content.knowledge.repository.KnowledgeMapMapper;
import com.javainterviewlab.content.knowledge.repository.model.KnowledgeTopicRow;
import com.javainterviewlab.study.profile.service.CurrentProfileProvider;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 将扁平 SQL 聚合结果组装为分类到专题的稳定知识树。 */
@Service
public class KnowledgeMapService {

    private final KnowledgeMapMapper knowledgeMapMapper;
    private final CurrentProfileProvider currentProfileProvider;
    private final Clock clock;

    public KnowledgeMapService(
            KnowledgeMapMapper knowledgeMapMapper,
            CurrentProfileProvider currentProfileProvider,
            Clock clock
    ) {
        this.knowledgeMapMapper = knowledgeMapMapper;
        this.currentProfileProvider = currentProfileProvider;
        this.clock = clock;
    }

    /** 聚合只计算启用题目，禁用内容不会稀释当前掌握率。 */
    public KnowledgeMapResponse getKnowledgeMap() {
        List<KnowledgeTopicRow> rows = knowledgeMapMapper.findKnowledgeTopics(
                currentProfileProvider.requireProfileId()
        );
        Map<Long, CategoryAccumulator> categoryMap = new LinkedHashMap<>();
        long total = 0;
        long touched = 0;
        long mastered = 0;
        for (KnowledgeTopicRow row : rows) {
            CategoryAccumulator category = categoryMap.computeIfAbsent(
                    row.categoryId(),
                    ignored -> new CategoryAccumulator(row)
            );
            category.topics.add(toTopic(row));
            total += row.totalQuestionCount();
            touched += row.touchedQuestionCount();
            mastered += row.masteredQuestionCount();
        }
        List<KnowledgeCategoryResponse> categories = categoryMap.values().stream()
                .map(CategoryAccumulator::toResponse)
                .toList();
        return new KnowledgeMapResponse(clock.instant(), total, touched, mastered, categories);
    }

    private KnowledgeTopicResponse toTopic(KnowledgeTopicRow row) {
        long total = row.totalQuestionCount();
        long mastered = row.masteredQuestionCount();
        double rate = total == 0 ? 0D : (double) mastered / total;
        KnowledgeState state = mastered > 0 && mastered == total
                ? KnowledgeState.MASTERED
                : row.touchedQuestionCount() > 0 ? KnowledgeState.LEARNING : KnowledgeState.NOT_STARTED;
        return new KnowledgeTopicResponse(
                row.topicId(),
                row.topicCode(),
                row.topicName(),
                row.topicDescription(),
                row.topicStarLevel(),
                total,
                row.touchedQuestionCount(),
                mastered,
                rate,
                state.name(),
                state.getDescription()
        );
    }

    private static final class CategoryAccumulator {

        private final Long id;
        private final String code;
        private final String name;
        private final String description;
        private final List<KnowledgeTopicResponse> topics = new ArrayList<>();

        private CategoryAccumulator(KnowledgeTopicRow row) {
            this.id = row.categoryId();
            this.code = row.categoryCode();
            this.name = row.categoryName();
            this.description = row.categoryDescription();
        }

        private KnowledgeCategoryResponse toResponse() {
            return new KnowledgeCategoryResponse(id, code, name, description, List.copyOf(topics));
        }
    }
}
