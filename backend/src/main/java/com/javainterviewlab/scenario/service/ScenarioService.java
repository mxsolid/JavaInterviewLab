package com.javainterviewlab.scenario.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.scenario.domain.ScenarioAttemptResultType;
import com.javainterviewlab.scenario.dto.ScenarioAttemptResponse;
import com.javainterviewlab.scenario.dto.ScenarioCaseResponse;
import com.javainterviewlab.scenario.dto.ScenarioDetailResponse;
import com.javainterviewlab.scenario.dto.ScenarioMatrixCellResponse;
import com.javainterviewlab.scenario.dto.ScenarioMatrixResponse;
import com.javainterviewlab.scenario.dto.ScenarioSolutionResponse;
import com.javainterviewlab.scenario.dto.ScenarioSummaryResponse;
import com.javainterviewlab.scenario.dto.SubmitScenarioAttemptRequest;
import com.javainterviewlab.scenario.repository.ScenarioMapper;
import com.javainterviewlab.scenario.repository.model.ScenarioAttemptRow;
import com.javainterviewlab.scenario.repository.model.ScenarioCaseRow;
import com.javainterviewlab.scenario.repository.model.ScenarioRow;
import com.javainterviewlab.scenario.repository.model.ScenarioSolutionRow;
import com.javainterviewlab.study.profile.service.CurrentProfileProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 场景工作区查询和作答历史服务。 */
@Service
public class ScenarioService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioService.class);

    private final ScenarioMapper scenarioMapper;
    private final CurrentProfileProvider currentProfileProvider;
    private final ObjectMapper objectMapper;

    public ScenarioService(
            ScenarioMapper scenarioMapper,
            CurrentProfileProvider currentProfileProvider,
            ObjectMapper objectMapper
    ) {
        this.scenarioMapper = scenarioMapper;
        this.currentProfileProvider = currentProfileProvider;
        this.objectMapper = objectMapper;
    }

    public List<ScenarioSummaryResponse> list() {
        return scenarioMapper.findSummaries(currentProfileProvider.requireProfileId()).stream()
                .map(row -> new ScenarioSummaryResponse(
                        row.id(), row.externalKey(), row.title(), row.summary(), row.starLevel(),
                        row.caseCount(), row.attemptCount()
                ))
                .toList();
    }

    public ScenarioDetailResponse detail(Long scenarioId) {
        ScenarioRow scenario = requireScenario(scenarioId);
        return new ScenarioDetailResponse(
                scenario.id(), scenario.externalKey(), scenario.title(), scenario.summary(),
                scenario.starLevel(), scenario.sourceVersion(), cases(scenarioId), solutions(scenarioId)
        );
    }

    public ScenarioMatrixResponse matrix(Long scenarioId) {
        requireScenario(scenarioId);
        List<ScenarioMatrixCellResponse> cells = scenarioMapper.findMatrixCells(scenarioId).stream()
                .map(row -> new ScenarioMatrixCellResponse(
                        row.caseId(), row.solutionId(), row.recommendation(), row.reason(), row.sortOrder()
                ))
                .toList();
        return new ScenarioMatrixResponse(scenarioId, cases(scenarioId), solutions(scenarioId), cells);
    }

    /** 唯一键裁决网络重试；不同 clientAttemptId 永远追加新历史。 */
    @Transactional
    public ScenarioAttemptResponse submit(SubmitScenarioAttemptRequest request) {
        Long profileId = currentProfileProvider.requireProfileId();
        ScenarioAttemptRow existing = scenarioMapper.findAttempt(profileId, request.clientAttemptId());
        if (existing != null) {
            return toAttemptResponse(existing, true);
        }
        if (scenarioMapper.countEnabledScenario(request.scenarioId()) == 0) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "场景不存在或已停用");
        }
        if (request.caseId() != null
                && scenarioMapper.countCaseInScenario(request.scenarioId(), request.caseId()) == 0) {
            throw new BusinessException(ApiErrorCode.VALIDATION_FAILED, "Case 不属于当前场景");
        }
        Long attemptId = scenarioMapper.insertAttemptIgnore(profileId, request);
        ScenarioAttemptRow saved = scenarioMapper.findAttempt(profileId, request.clientAttemptId());
        if (saved == null) {
            throw new IllegalStateException("场景答题写入后无法回读");
        }
        boolean duplicated = attemptId == null;
        LOGGER.info(
                "场景作答已保存, profileId={}, scenarioId={}, caseId={}, attemptId={}, result={}",
                profileId, saved.scenarioId(), saved.caseId(), saved.id(), saved.resultType()
        );
        return toAttemptResponse(saved, duplicated);
    }

    private ScenarioRow requireScenario(Long scenarioId) {
        ScenarioRow scenario = scenarioMapper.findEnabledById(scenarioId);
        if (scenario == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "场景不存在或已停用");
        }
        return scenario;
    }

    private List<ScenarioCaseResponse> cases(Long scenarioId) {
        return scenarioMapper.findCases(scenarioId).stream().map(this::toCaseResponse).toList();
    }

    private ScenarioCaseResponse toCaseResponse(ScenarioCaseRow row) {
        try {
            List<String> expectedAnalysis = objectMapper.readValue(
                    row.expectedAnalysisJson(), new TypeReference<>() { }
            );
            return new ScenarioCaseResponse(
                    row.id(), row.code(), row.title(), row.rootCause(), row.prompt(), expectedAnalysis
            );
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("场景 expectedAnalysis 数据损坏", exception);
        }
    }

    private List<ScenarioSolutionResponse> solutions(Long scenarioId) {
        return scenarioMapper.findSolutions(scenarioId).stream().map(this::toSolutionResponse).toList();
    }

    private ScenarioSolutionResponse toSolutionResponse(ScenarioSolutionRow row) {
        return new ScenarioSolutionResponse(
                row.id(), row.code(), row.name(), row.principle(), row.pros(), row.cons(), row.boundary()
        );
    }

    private ScenarioAttemptResponse toAttemptResponse(ScenarioAttemptRow row, boolean duplicated) {
        ScenarioAttemptResultType resultType = ScenarioAttemptResultType.valueOf(row.resultType());
        return new ScenarioAttemptResponse(
                row.id(), row.clientAttemptId(), row.scenarioId(), row.caseId(), row.selfRating(),
                resultType.name(), resultType.getDescription(), row.durationSeconds(), row.createdAt(), duplicated
        );
    }
}
