package com.javainterviewlab.system.e2e;

import com.javainterviewlab.system.seed.dto.SeedImportResponse;
import com.javainterviewlab.system.seed.service.SeedImportService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 每次 E2E 后端启动创建空白档案，避免截图和断言受开发者本地学习历史影响。 */
@Component
@Profile("e2e")
@Order(Ordered.LOWEST_PRECEDENCE)
public class E2eProfileFixture implements ApplicationRunner {

    private static final String E2E_PROFILE_NAME = "E2E 固定档案";
    private static final String DEFAULT_PLAN_CODE = "SPRINT_10";
    private static final String CORE_SEED_PATH = "seed/v03-core-complete.json";
    private static final int CORE_QUESTION_COUNT = 336;

    private final E2eProfileMapper e2eProfileMapper;
    private final E2eProfileContext e2eProfileContext;
    private final SeedImportService seedImportService;

    public E2eProfileFixture(
            E2eProfileMapper e2eProfileMapper,
            E2eProfileContext e2eProfileContext,
            SeedImportService seedImportService
    ) {
        this.e2eProfileMapper = e2eProfileMapper;
        this.e2eProfileContext = e2eProfileContext;
        this.seedImportService = seedImportService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource seedResource = new ClassPathResource(CORE_SEED_PATH);
        SeedImportResponse importResponse = seedImportService.importJson(new E2eSeedMultipartFile(
                "v03-core-complete.json",
                seedResource.getInputStream().readAllBytes()
        ));
        if (importResponse.questionCount() != CORE_QUESTION_COUNT) {
            throw new IllegalStateException("E2E 核心题库数量不正确：" + importResponse.questionCount());
        }
        Long profileId = e2eProfileMapper.insertProfile(E2E_PROFILE_NAME);
        int activated = e2eProfileMapper.activatePlan(profileId, DEFAULT_PLAN_CODE);
        if (activated != 1) {
            throw new IllegalStateException("E2E 默认学习路线不存在：" + DEFAULT_PLAN_CODE);
        }
        e2eProfileContext.initialize(profileId);
    }
}
