package com.javainterviewlab.study.attempt.dto;
import com.javainterviewlab.study.progress.dto.StudyProgressResponse;
import com.javainterviewlab.study.review.dto.ReviewTaskResponse;

/** 一次提交同时返回历史事实和当前快照。 */
public record SubmitAttemptResponse(
        QuestionAttemptResponse attempt,
        StudyProgressResponse progress,
        ReviewTaskResponse review
) {
}
