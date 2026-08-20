package com.javainterviewlab.interview.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javainterviewlab.common.api.ApiErrorCode;
import com.javainterviewlab.common.exception.BusinessException;
import com.javainterviewlab.interview.domain.InterviewEvaluationContext;
import com.javainterviewlab.interview.domain.InterviewMode;
import com.javainterviewlab.interview.domain.InterviewScore;
import com.javainterviewlab.interview.dto.CreateInterviewRequest;
import com.javainterviewlab.interview.dto.InterviewDimensionResponse;
import com.javainterviewlab.interview.dto.InterviewFinishResponse;
import com.javainterviewlab.interview.dto.InterviewSessionResponse;
import com.javainterviewlab.interview.dto.InterviewTurnResponse;
import com.javainterviewlab.interview.dto.SubmitInterviewTurnRequest;
import com.javainterviewlab.interview.repository.InterviewMapper;
import com.javainterviewlab.interview.repository.model.InterviewQuestionRow;
import com.javainterviewlab.interview.repository.model.InterviewSessionRow;
import com.javainterviewlab.interview.repository.model.InterviewTurnRow;
import com.javainterviewlab.study.profile.service.CurrentProfileProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** 模拟面试会话、规则评分与持久化服务。 */
@Service
public class InterviewService {

    private static final String RULE_PROVIDER = "RULE_BASED";

    private final InterviewMapper mapper;
    private final InterviewEvaluator evaluator;
    private final CurrentProfileProvider currentProfileProvider;
    private final ObjectMapper objectMapper;
    private final boolean providerEnabled;

    public InterviewService(
            InterviewMapper mapper,
            InterviewEvaluator evaluator,
            CurrentProfileProvider currentProfileProvider,
            ObjectMapper objectMapper,
            @Value("${app.interview.provider-enabled:false}") boolean providerEnabled
    ) {
        this.mapper = mapper;
        this.evaluator = evaluator;
        this.currentProfileProvider = currentProfileProvider;
        this.objectMapper = objectMapper;
        this.providerEnabled = providerEnabled;
    }

    @Transactional
    public InterviewSessionResponse create(CreateInterviewRequest request) {
        String topicCode = normalizeTopic(request);
        InterviewQuestionRow question = mapper.findQuestion(topicCode);
        if (question == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "指定范围内没有可用面试题");
        }
        Long profileId = currentProfileProvider.requireProfileId();
        Long sessionId = mapper.insertSession(profileId, question.id(), request);
        InterviewSessionRow session = mapper.findSession(profileId, sessionId);
        return new InterviewSessionResponse(
                session.id(), session.mode(), question.topicCode(), session.status(), question.id(), 1,
                question.title(), RULE_PROVIDER, providerEnabled, session.startedAt()
        );
    }

    /** 会话行锁负责 sequenceNo 并发不变量；同一 clientTurnId 在锁内回读原结果。 */
    @Transactional
    public InterviewTurnResponse submit(Long sessionId, SubmitInterviewTurnRequest request) {
        Long profileId = currentProfileProvider.requireProfileId();
        InterviewSessionRow session = requireSessionForUpdate(profileId, sessionId);
        InterviewTurnRow existing = mapper.findTurnByClientId(sessionId, request.clientTurnId());
        if (existing != null) {
            return toTurnResponse(existing, true, nextPrompt(session.questionId(), existing.sequenceNo()));
        }
        if (!"ACTIVE".equals(session.status())) {
            throw new BusinessException(ApiErrorCode.BUSINESS_RULE_VIOLATED, "面试会话已结束");
        }

        InterviewQuestionRow question = requireQuestion(session.questionId());
        int sequenceNo = mapper.countTurns(sessionId) + 1;
        String prompt = prompt(question, sequenceNo);
        if (prompt == null) {
            throw new BusinessException(ApiErrorCode.BUSINESS_RULE_VIOLATED, "当前题目的追问已全部完成");
        }
        InterviewScore score = evaluator.evaluate(new InterviewEvaluationContext(
                sessionId, sequenceNo, prompt, request.answerText(), question.rubricText()
        ));
        List<InterviewDimensionResponse> dimensions = dimensions(score);
        Long turnId = mapper.insertTurn(
                sessionId, sequenceNo, prompt, request, score, serialize(dimensions)
        );
        InterviewTurnRow saved = mapper.findTurnById(turnId);
        return new InterviewTurnResponse(
                saved.id(), saved.clientTurnId(), saved.sequenceNo(), saved.prompt(), saved.score(),
                dimensions, nextPrompt(session.questionId(), sequenceNo), false, saved.createdAt()
        );
    }

    @Transactional
    public InterviewFinishResponse finish(Long sessionId) {
        Long profileId = currentProfileProvider.requireProfileId();
        InterviewSessionRow session = requireSessionForUpdate(profileId, sessionId);
        List<InterviewTurnRow> turns = mapper.findTurns(sessionId);
        if (turns.isEmpty()) {
            throw new BusinessException(ApiErrorCode.BUSINESS_RULE_VIOLATED, "至少提交一轮回答后才能结束面试");
        }
        List<InterviewDimensionResponse> averages = averageDimensions(turns);
        double total = round(averages.stream().mapToDouble(InterviewDimensionResponse::score).sum());
        if ("ACTIVE".equals(session.status())) {
            mapper.finishSession(profileId, sessionId, total);
        }
        InterviewSessionRow finished = mapper.findSession(profileId, sessionId);
        return new InterviewFinishResponse(
                sessionId, finished.status(), total, averages, turns.size(), summary(total), finished.finishedAt()
        );
    }

    private String normalizeTopic(CreateInterviewRequest request) {
        if (request.mode() == InterviewMode.TOPIC && (request.topicCode() == null || request.topicCode().isBlank())) {
            throw new BusinessException(ApiErrorCode.VALIDATION_FAILED, "TOPIC 模式必须提供 topicCode");
        }
        return request.mode() == InterviewMode.TOPIC ? request.topicCode().trim() : null;
    }

    private InterviewSessionRow requireSessionForUpdate(Long profileId, Long sessionId) {
        InterviewSessionRow session = mapper.findSessionForUpdate(profileId, sessionId);
        if (session == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "面试会话不存在");
        }
        return session;
    }

    private InterviewQuestionRow requireQuestion(Long questionId) {
        InterviewQuestionRow question = mapper.findQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ApiErrorCode.RESOURCE_NOT_FOUND, "面试题不存在或已停用");
        }
        return question;
    }

    private String prompt(InterviewQuestionRow question, int sequenceNo) {
        return sequenceNo == 1 ? question.title() : mapper.findFollowUpPrompt(question.id(), sequenceNo - 2);
    }

    private String nextPrompt(Long questionId, int completedSequenceNo) {
        return mapper.findFollowUpPrompt(questionId, completedSequenceNo - 1);
    }

    private List<InterviewDimensionResponse> dimensions(InterviewScore score) {
        return List.of(
                new InterviewDimensionResponse("ACCURACY", "准确性", score.accuracy(), 40D, score.accuracyReason()),
                new InterviewDimensionResponse("COMPLETENESS", "完整度", score.completeness(), 25D, score.completenessReason()),
                new InterviewDimensionResponse("DEPTH", "深度", score.depth(), 20D, score.depthReason()),
                new InterviewDimensionResponse("EXPRESSION", "表达", score.expression(), 15D, score.expressionReason())
        );
    }

    private List<InterviewDimensionResponse> averageDimensions(List<InterviewTurnRow> turns) {
        return List.of(
                average("ACCURACY", "准确性", 40D, turns.stream().mapToDouble(item -> value(item.accuracyScore())).average().orElse(0D)),
                average("COMPLETENESS", "完整度", 25D, turns.stream().mapToDouble(item -> value(item.completenessScore())).average().orElse(0D)),
                average("DEPTH", "深度", 20D, turns.stream().mapToDouble(item -> value(item.depthScore())).average().orElse(0D)),
                average("EXPRESSION", "表达", 15D, turns.stream().mapToDouble(item -> value(item.expressionScore())).average().orElse(0D))
        );
    }

    private InterviewDimensionResponse average(String code, String label, double maxScore, double score) {
        return new InterviewDimensionResponse(code, label, round(score), maxScore, "按已提交轮次取平均值。");
    }

    private InterviewTurnResponse toTurnResponse(InterviewTurnRow row, boolean duplicated, String nextPrompt) {
        return new InterviewTurnResponse(
                row.id(), row.clientTurnId(), row.sequenceNo(), row.prompt(), row.score(),
                deserialize(row.feedback()), nextPrompt, duplicated, row.createdAt()
        );
    }

    private String serialize(List<InterviewDimensionResponse> dimensions) {
        try {
            return objectMapper.writeValueAsString(dimensions);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("面试评分结果无法序列化", exception);
        }
    }

    private List<InterviewDimensionResponse> deserialize(String feedback) {
        try {
            return objectMapper.readValue(feedback, new TypeReference<>() { });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("面试评分结果无法解析", exception);
        }
    }

    private String summary(double total) {
        if (total >= 80D) return "回答主线完整，可继续加强边界与追问。";
        if (total >= 60D) return "已覆盖核心主线，仍需补充依据、边界和结构。";
        return "当前回答覆盖不足，建议回到题目学习页补齐核心得分点。";
    }

    private double round(double value) {
        return Math.round(value * 100D) / 100D;
    }

    private double value(Double score) {
        return score == null ? 0D : score;
    }
}
