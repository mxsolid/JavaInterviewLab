package com.javainterviewlab.study.plan.service;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 应用就绪后同步内置学习路线。
 *
 * <p>同步入口放在启动生命周期中，保证查询 API 只读，避免缓存预热或重复 GET 意外写入数据库。</p>
 */
@Component
public class StudyPlanStartupSync {

    private final StudyPlanBootstrapService studyPlanBootstrapService;

    public StudyPlanStartupSync(StudyPlanBootstrapService studyPlanBootstrapService) {
        this.studyPlanBootstrapService = studyPlanBootstrapService;
    }

    /** 应用就绪后同步路线；种子导入完成后会再次调用同一幂等方法。 */
    @EventListener(ApplicationReadyEvent.class)
    public void syncSystemPlans() {
        studyPlanBootstrapService.syncSystemPlans();
    }
}
