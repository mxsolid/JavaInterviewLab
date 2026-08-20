package com.javainterviewlab.scenario.repository;

import com.javainterviewlab.scenario.domain.ScenarioSeedPack.SeedCase;
import com.javainterviewlab.scenario.domain.ScenarioSeedPack.SeedScenario;
import com.javainterviewlab.scenario.domain.ScenarioSeedPack.SeedSolution;
import com.javainterviewlab.scenario.dto.SubmitScenarioAttemptRequest;
import com.javainterviewlab.scenario.repository.model.ScenarioAttemptRow;
import com.javainterviewlab.scenario.repository.model.ScenarioCaseRow;
import com.javainterviewlab.scenario.repository.model.ScenarioMatrixCellRow;
import com.javainterviewlab.scenario.repository.model.ScenarioRow;
import com.javainterviewlab.scenario.repository.model.ScenarioSolutionRow;
import com.javainterviewlab.scenario.repository.model.ScenarioSummaryRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** 场景内容、矩阵和 append-only attempt 数据访问。 */
@Mapper
public interface ScenarioMapper {

    List<ScenarioSummaryRow> findSummaries(@Param("profileId") Long profileId);

    ScenarioRow findEnabledById(@Param("id") Long id);

    ScenarioRow findByExternalKey(@Param("externalKey") String externalKey);

    List<ScenarioCaseRow> findCases(@Param("scenarioId") Long scenarioId);

    List<ScenarioSolutionRow> findSolutions(@Param("scenarioId") Long scenarioId);

    List<ScenarioMatrixCellRow> findMatrixCells(@Param("scenarioId") Long scenarioId);

    int countEnabledScenario(@Param("scenarioId") Long scenarioId);

    int countCaseInScenario(@Param("scenarioId") Long scenarioId, @Param("caseId") Long caseId);

    Long insertAttemptIgnore(
            @Param("profileId") Long profileId,
            @Param("request") SubmitScenarioAttemptRequest request
    );

    ScenarioAttemptRow findAttempt(
            @Param("profileId") Long profileId,
            @Param("clientAttemptId") java.util.UUID clientAttemptId
    );

    void lockSeedPack(@Param("seedPack") String seedPack);

    String findSeedChecksum(@Param("seedPack") String seedPack, @Param("version") String version);

    Long insertScenario(@Param("pack") String pack, @Param("version") String version, @Param("item") SeedScenario item);

    int updateScenario(@Param("pack") String pack, @Param("version") String version, @Param("item") SeedScenario item);

    Long upsertCase(
            @Param("scenarioId") Long scenarioId,
            @Param("item") SeedCase item,
            @Param("expectedAnalysisJson") String expectedAnalysisJson,
            @Param("sortOrder") int sortOrder
    );

    Long upsertSolution(
            @Param("item") SeedSolution item,
            @Param("sortOrder") int sortOrder
    );

    void deleteMatrix(@Param("scenarioId") Long scenarioId);

    void insertMatrixCell(
            @Param("caseId") Long caseId,
            @Param("solutionId") Long solutionId,
            @Param("sortOrder") int sortOrder
    );

    void insertSeedHistory(
            @Param("seedPack") String seedPack,
            @Param("version") String version,
            @Param("checksum") String checksum,
            @Param("scenarioCount") int scenarioCount,
            @Param("caseCount") int caseCount,
            @Param("solutionCount") int solutionCount
    );
}
