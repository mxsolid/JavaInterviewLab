package com.javainterviewlab.study.plan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.study.plan.repository.StudyPlanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * 将内置学习路线作为产品内容幂等写入数据库。
 *
 * <p>路线不能写死在 React：题库调整后，运营只需更新路线内容并重新导入，无需发布前端。</p>
 */
@Service
public class StudyPlanBootstrapService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StudyPlanBootstrapService.class);
    private static final String CATALOG_RESOURCE = "study/study-plans.json";
    private static final String TOPIC_TARGET = "TOPIC";
    private static final String QUESTION_TARGET = "QUESTION";

    private final ObjectMapper objectMapper;
    private final StudyPlanMapper studyPlanMapper;

    public StudyPlanBootstrapService(ObjectMapper objectMapper, StudyPlanMapper studyPlanMapper) {
        this.objectMapper = objectMapper;
        this.studyPlanMapper = studyPlanMapper;
    }

    /**
     * 路线定义与题库内容分开导入：题库尚未导入时保留路线和天数，待题库导入后再次执行即可补齐关联项。
     */
    @Transactional
    public void ensureSystemPlans() {
        PlanCatalog catalog = readCatalog();
        int unresolvedItems = 0;
        for (PlanDefinition plan : catalog.plans()) {
            Long planId = studyPlanMapper.upsertPlan(
                    plan.code(), plan.name(), plan.durationDays(), plan.description(), plan.sortOrder()
            );
            for (DayDefinition day : plan.days()) {
                Long dayId = studyPlanMapper.upsertDay(planId, day.dayNumber(), day.title(), day.description());
                for (int index = 0; index < day.items().size(); index++) {
                    ItemDefinition item = day.items().get(index);
                    Long targetId = resolveTargetId(item);
                    if (targetId == null) {
                        unresolvedItems++;
                        continue;
                    }
                    studyPlanMapper.upsertItem(dayId, item.targetType(), targetId, index);
                }
            }
        }
        if (unresolvedItems > 0) {
            LOGGER.info("学习路线已写入，等待题库导入后补齐关联项, unresolvedItems={}", unresolvedItems);
        }
    }

    private Long resolveTargetId(ItemDefinition item) {
        return switch (item.targetType()) {
            case TOPIC_TARGET -> studyPlanMapper.findTopicIdByCode(item.targetKey());
            case QUESTION_TARGET -> studyPlanMapper.findQuestionIdByExternalKey(item.targetKey());
            default -> null;
        };
    }

    private PlanCatalog readCatalog() {
        try {
            return objectMapper.readValue(new ClassPathResource(CATALOG_RESOURCE).getInputStream(), PlanCatalog.class);
        } catch (IOException exception) {
            throw new IllegalStateException("内置学习路线文件无法读取", exception);
        }
    }

    private record PlanCatalog(List<PlanDefinition> plans) {
    }

    private record PlanDefinition(
            String code,
            String name,
            Integer durationDays,
            String description,
            Integer sortOrder,
            List<DayDefinition> days
    ) {
    }

    private record DayDefinition(
            Integer dayNumber,
            String title,
            String description,
            List<ItemDefinition> items
    ) {
    }

    private record ItemDefinition(String targetType, String targetKey) {
    }
}
