package com.javainterviewlab.study.attempt.dto;
import com.javainterviewlab.study.progress.dto.StudyProgressResponse;
import com.javainterviewlab.study.review.dto.ScheduledReviewResponse;

/** 一次提交的当前可靠结果；重复提交只回读状态，绝不再次推进学习快照。 */
public record SubmitAttemptResponse(
        QuestionAttemptResponse attempt,
        StudyProgressResponse progress,
        ScheduledReviewResponse review,
        boolean duplicated
) {
}
