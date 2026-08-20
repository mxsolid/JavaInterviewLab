package com.javainterviewlab.study.plan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.study.plan.domain.StudyPlanTargetType;
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

    private final ObjectMapper objectMapper;
    private final StudyPlanMapper studyPlanMapper;

    public StudyPlanBootstrapService(ObjectMapper objectMapper, StudyPlanMapper studyPlanMapper) {
        this.objectMapper = objectMapper;
        this.studyPlanMapper = studyPlanMapper;
    }

    /**
     * 同步系统内置路线到数据库。
     *
     * <p>该方法只由应用启动和种子导入调用。路线定义与题库内容分开时，未解析项会记录告警，种子导入完成后再次同步补齐。</p>
     */
    @Transactional
    public void syncSystemPlans() {
        PlanCatalog catalog = readCatalog();
        int unresolvedItems = 0;
        for (PlanDefinition plan : catalog.plans()) {
            Long planId = studyPlanMapper.upsertPlan(
                    plan.code(), plan.name(), plan.durationDays(), plan.description(), plan.sortOrder()
            );
            for (DayDefinition day : plan.days()) {
                Long dayId = studyPlanMapper.upsertDay(planId, day.dayNumber(), day.title(), day.description());
                List<CatalogItem> items = day.items().stream()
                        .map(item -> new CatalogItem(StudyPlanTargetType.fromCatalogValue(item.targetType()), item.targetKey()))
                        .toList();
                // 目录是路线项的唯一事实源；先清空当日旧项，才能避免 JSON 删除某项后数据库继续展示它。
                studyPlanMapper.deleteItemsByDayId(dayId);
                for (int index = 0; index < items.size(); index++) {
                    CatalogItem item = items.get(index);
                    Long targetId = resolveTargetId(item);
                    if (targetId == null) {
                        unresolvedItems++;
                        LOGGER.warn(
                                "学习路线目标尚未解析, targetType={}, targetKey={}",
                                item.targetType(),
                                item.targetKey()
                        );
                        continue;
                    }
                    studyPlanMapper.upsertItem(dayId, item.targetType(), targetId, index);
                }
            }
        }
        if (unresolvedItems > 0) {
            LOGGER.warn("学习路线同步完成，但仍有目标等待题库导入, unresolvedItems={}", unresolvedItems);
        }
    }

    private Long resolveTargetId(CatalogItem item) {
        return switch (item.targetType()) {
            case TOPIC -> studyPlanMapper.findTopicIdByCode(item.targetKey());
            case QUESTION -> studyPlanMapper.findQuestionIdByExternalKey(item.targetKey());
            // 场景表在 V0.3 才建立；保留合法目录类型，但当前不能伪造 targetId。
            case SCENARIO -> null;
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

    /** 已校验类型的目录项，避免后续解析继续传播原始字符串。 */
    private record CatalogItem(StudyPlanTargetType targetType, String targetKey) {
    }
}
