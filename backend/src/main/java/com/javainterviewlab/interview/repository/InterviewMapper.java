package com.javainterviewlab.interview.repository;

import com.javainterviewlab.interview.domain.InterviewScore;
import com.javainterviewlab.interview.dto.CreateInterviewRequest;
import com.javainterviewlab.interview.dto.SubmitInterviewTurnRequest;
import com.javainterviewlab.interview.repository.model.InterviewQuestionRow;
import com.javainterviewlab.interview.repository.model.InterviewSessionRow;
import com.javainterviewlab.interview.repository.model.InterviewTurnRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/** 模拟面试会话和轮次的数据访问。 */
@Mapper
public interface InterviewMapper {

    InterviewQuestionRow findQuestion(@Param("topicCode") String topicCode);

    InterviewQuestionRow findQuestionById(@Param("questionId") Long questionId);

    Long insertSession(
            @Param("profileId") Long profileId,
            @Param("questionId") Long questionId,
            @Param("request") CreateInterviewRequest request
    );

    InterviewSessionRow findSession(@Param("profileId") Long profileId, @Param("sessionId") Long sessionId);

    InterviewSessionRow findSessionForUpdate(@Param("profileId") Long profileId, @Param("sessionId") Long sessionId);

    InterviewTurnRow findTurnByClientId(
            @Param("sessionId") Long sessionId,
            @Param("clientTurnId") UUID clientTurnId
    );

    int countTurns(@Param("sessionId") Long sessionId);

    String findFollowUpPrompt(@Param("questionId") Long questionId, @Param("offset") int offset);

    Long insertTurn(
            @Param("sessionId") Long sessionId,
            @Param("sequenceNo") int sequenceNo,
            @Param("prompt") String prompt,
            @Param("request") SubmitInterviewTurnRequest request,
            @Param("score") InterviewScore score,
            @Param("feedback") String feedback
    );

    InterviewTurnRow findTurnById(@Param("id") Long id);

    List<InterviewTurnRow> findTurns(@Param("sessionId") Long sessionId);

    int finishSession(
            @Param("profileId") Long profileId,
            @Param("sessionId") Long sessionId,
            @Param("totalScore") double totalScore
    );
}
