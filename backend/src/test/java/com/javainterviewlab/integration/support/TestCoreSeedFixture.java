package com.javainterviewlab.integration.support;

import com.javainterviewlab.system.seed.dto.SeedImportResponse;
import com.javainterviewlab.system.seed.service.SeedImportService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

/** 集成测试启动前幂等导入正式题库，使全新数据库不依赖开发机历史数据。 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "jil.test.seed.enabled", havingValue = "true")
public class TestCoreSeedFixture implements ApplicationRunner {

    private static final String SEED_PATH = "seed/v03-core-complete.json";
    private static final int EXPECTED_QUESTION_COUNT = 336;

    private final SeedImportService seedImportService;

    public TestCoreSeedFixture(SeedImportService seedImportService) {
        this.seedImportService = seedImportService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        ClassPathResource seedResource = new ClassPathResource(SEED_PATH);
        SeedImportResponse response = seedImportService.importJson(new MockMultipartFile(
                "file",
                "v03-core-complete.json",
                "application/json",
                seedResource.getInputStream()
        ));
        if (response.questionCount() != EXPECTED_QUESTION_COUNT) {
            throw new IllegalStateException("测试题库数量不正确：" + response.questionCount());
        }
    }
}
