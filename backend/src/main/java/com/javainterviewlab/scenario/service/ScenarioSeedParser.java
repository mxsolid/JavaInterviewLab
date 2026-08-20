package com.javainterviewlab.scenario.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.scenario.domain.ScenarioSeedPack;
import com.javainterviewlab.scenario.domain.ScenarioSeedPack.SeedCase;
import com.javainterviewlab.scenario.domain.ScenarioSeedPack.SeedScenario;
import com.javainterviewlab.scenario.domain.ScenarioSeedPack.SeedSolution;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

/** 将场景 JSON 转换为完整校验后的内容模型。 */
@Component
public class ScenarioSeedParser {

    private final ObjectMapper objectMapper;

    public ScenarioSeedParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ScenarioSeedPack parse(byte[] bytes) {
        try {
            JsonNode root = objectMapper.readTree(bytes);
            if (root == null || !root.isObject() || !root.path("scenarios").isArray()) {
                throw validation("场景种子必须包含 scenarios 数组");
            }
            String seedPack = required(root, "seedPack", "场景种子");
            String version = required(root, "version", "场景种子");
            List<SeedScenario> scenarios = parseScenarios(root.path("scenarios"));
            return new ScenarioSeedPack(seedPack, version, sha256(bytes), List.copyOf(scenarios));
        } catch (BusinessException exception) {
            throw exception;
        } catch (IOException exception) {
            throw validation("场景种子 JSON 无法解析");
        }
    }

    private List<SeedScenario> parseScenarios(JsonNode node) {
        List<SeedScenario> scenarios = new ArrayList<>();
        Set<String> externalKeys = new HashSet<>();
        for (JsonNode item : node) {
            String externalKey = required(item, "externalKey", "场景");
            if (!externalKeys.add(externalKey)) {
                throw validation("场景 externalKey 重复：" + externalKey);
            }
            int starLevel = item.path("starLevel").asInt(0);
            if (starLevel < 1 || starLevel > 5) {
                throw validation("场景 starLevel 必须在 1~5：" + externalKey);
            }
            List<SeedSolution> solutions = parseSolutions(item.path("solutions"), externalKey);
            Set<String> solutionCodes = new HashSet<>();
            solutions.forEach(solution -> solutionCodes.add(solution.code()));
            List<SeedCase> cases = parseCases(item.path("cases"), externalKey, solutionCodes);
            scenarios.add(new SeedScenario(
                    externalKey,
                    required(item, "title", externalKey),
                    starLevel,
                    required(item, "summary", externalKey),
                    List.copyOf(cases),
                    List.copyOf(solutions)
            ));
        }
        return scenarios;
    }

    private List<SeedSolution> parseSolutions(JsonNode node, String scenarioKey) {
        if (!node.isArray()) {
            throw validation("场景 solutions 必须是数组：" + scenarioKey);
        }
        List<SeedSolution> solutions = new ArrayList<>();
        Set<String> codes = new HashSet<>();
        for (JsonNode item : node) {
            String code = required(item, "code", scenarioKey + " solution");
            if (!codes.add(code)) {
                throw validation("场景 solution code 重复：" + scenarioKey + "/" + code);
            }
            solutions.add(new SeedSolution(
                    code,
                    required(item, "name", scenarioKey + "/" + code),
                    required(item, "principle", scenarioKey + "/" + code)
            ));
        }
        return solutions;
    }

    private List<SeedCase> parseCases(JsonNode node, String scenarioKey, Set<String> solutionCodes) {
        if (!node.isArray()) {
            throw validation("场景 cases 必须是数组：" + scenarioKey);
        }
        List<SeedCase> cases = new ArrayList<>();
        Set<String> codes = new HashSet<>();
        for (JsonNode item : node) {
            String code = required(item, "code", scenarioKey + " case");
            if (!codes.add(code)) {
                throw validation("场景 case code 重复：" + scenarioKey + "/" + code);
            }
            List<String> candidateSolutions = stringList(item.path("candidateSolutions"), "candidateSolutions");
            for (String solutionCode : candidateSolutions) {
                if (!solutionCodes.contains(solutionCode)) {
                    throw validation("Case 引用不存在的 solution：" + scenarioKey + "/" + code);
                }
            }
            cases.add(new SeedCase(
                    code,
                    required(item, "title", scenarioKey + "/" + code),
                    required(item, "rootCause", scenarioKey + "/" + code),
                    candidateSolutions,
                    stringList(item.path("expectedAnalysis"), "expectedAnalysis")
            ));
        }
        return cases;
    }

    private List<String> stringList(JsonNode node, String field) {
        if (!node.isArray()) {
            throw validation(field + " 必须是数组");
        }
        List<String> values = new ArrayList<>();
        Set<String> unique = new HashSet<>();
        for (JsonNode item : node) {
            if (!item.isTextual() || item.asText().isBlank() || !unique.add(item.asText())) {
                throw validation(field + " 包含空值或重复值");
            }
            values.add(item.asText());
        }
        return List.copyOf(values);
    }

    private String required(JsonNode node, String field, String context) {
        JsonNode value = node.path(field);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw validation(context + " 缺少字段：" + field);
        }
        return value.asText().trim();
    }

    private String sha256(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", exception);
        }
    }

    private BusinessException validation(String message) {
        return new BusinessException(ApiErrorCode.CONTENT_VALIDATION_FAILED, message);
    }
}
