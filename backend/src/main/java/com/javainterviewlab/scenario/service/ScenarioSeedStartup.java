package com.javainterviewlab.scenario.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** 应用启动时确保内置场景库已按版本写入 PostgreSQL。 */
@Component
public class ScenarioSeedStartup implements ApplicationRunner {

    private final ScenarioSeedImportService importService;

    public ScenarioSeedStartup(ScenarioSeedImportService importService) {
        this.importService = importService;
    }

    @Override
    public void run(ApplicationArguments args) {
        importService.importBundled();
    }
}
