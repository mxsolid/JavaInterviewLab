package com.javainterviewlab.scenario.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.scenario.domain.ScenarioSeedPack;
import com.javainterviewlab.scenario.domain.ScenarioSeedPack.SeedCase;
import com.javainterviewlab.scenario.domain.ScenarioSeedPack.SeedScenario;
import com.javainterviewlab.scenario.domain.ScenarioSeedPack.SeedSolution;
import com.javainterviewlab.scenario.dto.ScenarioSeedImportResponse;
import com.javainterviewlab.scenario.repository.ScenarioMapper;
import com.javainterviewlab.scenario.repository.model.ScenarioRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** 将内置场景包按稳定业务键幂等写入 PostgreSQL。 */
@Service
public class ScenarioSeedImportService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioSeedImportService.class);
    private static final String RESOURCE_PATH = "seed/scenarios.v03-complete.json";

    private final ScenarioSeedParser parser;
    private final ScenarioMapper scenarioMapper;
    private final ObjectMapper objectMapper;

    public ScenarioSeedImportService(
            ScenarioSeedParser parser,
            ScenarioMapper scenarioMapper,
            ObjectMapper objectMapper
    ) {
        this.parser = parser;
        this.scenarioMapper = scenarioMapper;
        this.objectMapper = objectMapper;
    }

    /** 同版本同 checksum 直接返回；不同 checksum 或用户场景冲突会使整个事务回滚。 */
    @Transactional
    public ScenarioSeedImportResponse importBundled() {
        ScenarioSeedPack pack = parser.parse(readResource());
        scenarioMapper.lockSeedPack(pack.seedPack());
        String existingChecksum = scenarioMapper.findSeedChecksum(pack.seedPack(), pack.version());
        if (existingChecksum != null) {
            if (!existingChecksum.equals(pack.checksumSha256())) {
                throw new BusinessException(ApiErrorCode.VERSION_CONFLICT, "场景包同版本 checksum 不一致");
            }
            return response(pack, true);
        }

        for (SeedScenario item : pack.scenarios()) {
            Long scenarioId = upsertScenario(pack, item);
            Map<String, Long> solutionIds = upsertSolutions(scenarioId, item);
            Map<String, Long> caseIds = upsertCases(scenarioId, item);
            scenarioMapper.deleteMatrix(scenarioId);
            for (SeedCase seedCase : item.cases()) {
                int sortOrder = 0;
                for (String solutionCode : seedCase.candidateSolutions()) {
                    scenarioMapper.insertMatrixCell(
                            caseIds.get(seedCase.code()),
                            solutionIds.get(solutionCode),
                            sortOrder++
                    );
                }
            }
        }
        scenarioMapper.insertSeedHistory(
                pack.seedPack(),
                pack.version(),
                pack.checksumSha256(),
                pack.scenarios().size(),
                caseCount(pack),
                solutionCount(pack)
        );
        LOGGER.info(
                "场景种子导入完成, seedPack={}, version={}, scenarios={}, cases={}, solutions={}",
                pack.seedPack(), pack.version(), pack.scenarios().size(), caseCount(pack), solutionCount(pack)
        );
        return response(pack, false);
    }

    private Long upsertScenario(ScenarioSeedPack pack, SeedScenario item) {
        ScenarioRow existing = scenarioMapper.findByExternalKey(item.externalKey());
        if (existing == null) {
            return scenarioMapper.insertScenario(pack.seedPack(), pack.version(), item);
        }
        if ("USER".equals(existing.originType())) {
            throw new BusinessException(ApiErrorCode.CONTENT_VALIDATION_FAILED, "场景种子不得覆盖 USER 内容");
        }
        int updated = scenarioMapper.updateScenario(pack.seedPack(), pack.version(), item);
        if (updated != 1) {
            throw new BusinessException(ApiErrorCode.CONTENT_VALIDATION_FAILED, "场景属于其他 seed namespace");
        }
        return existing.id();
    }

    private Map<String, Long> upsertSolutions(Long scenarioId, SeedScenario item) {
        Map<String, Long> ids = new HashMap<>();
        int sortOrder = 0;
        for (SeedSolution solution : item.solutions()) {
            ids.put(solution.code(), scenarioMapper.upsertSolution(solution, sortOrder++));
        }
        return ids;
    }

    private Map<String, Long> upsertCases(Long scenarioId, SeedScenario item) {
        Map<String, Long> ids = new HashMap<>();
        int sortOrder = 0;
        for (SeedCase seedCase : item.cases()) {
            ids.put(
                    seedCase.code(),
                    scenarioMapper.upsertCase(scenarioId, seedCase, toJson(seedCase.expectedAnalysis()), sortOrder++)
            );
        }
        return ids;
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("场景内容无法序列化", exception);
        }
    }

    private byte[] readResource() {
        try {
            return new ClassPathResource(RESOURCE_PATH).getInputStream().readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("内置场景种子不存在", exception);
        }
    }

    private int caseCount(ScenarioSeedPack pack) {
        return pack.scenarios().stream().mapToInt(item -> item.cases().size()).sum();
    }

    private int solutionCount(ScenarioSeedPack pack) {
        return pack.scenarios().stream().mapToInt(item -> item.solutions().size()).sum();
    }

    private ScenarioSeedImportResponse response(ScenarioSeedPack pack, boolean duplicated) {
        return new ScenarioSeedImportResponse(
                pack.seedPack(),
                pack.version(),
                pack.checksumSha256(),
                pack.scenarios().size(),
                caseCount(pack),
                solutionCount(pack),
                duplicated
        );
    }
}
