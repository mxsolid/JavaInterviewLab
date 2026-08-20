package com.javainterviewlab.study.attempt.service;

import com.javainterviewlab.study.attempt.dto.QuestionAttemptResponse;
import com.javainterviewlab.study.attempt.dto.SubmitAttemptRequest;
import com.javainterviewlab.study.attempt.dto.SubmitAttemptResponse;
import com.javainterviewlab.study.progress.dto.StudyProgressResponse;
import com.javainterviewlab.study.progress.service.StudyProgressService;
import com.javainterviewlab.study.review.dto.ReviewTaskResponse;
import com.javainterviewlab.study.review.service.ReviewService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 提交练习事务入口。
 *
 * <p>attempt 是不可变历史，progress 是当前快照；两者必须一起提交，否则历史和学习状态无法相互解释。</p>
 */
@Service
public class StudySubmissionService {

    private final QuestionAttemptService attemptService;
    private final StudyProgressService progressService;
    private final ReviewService reviewService;

    public StudySubmissionService(
            QuestionAttemptService attemptService,
            StudyProgressService progressService,
            ReviewService reviewService
    ) {
        this.attemptService = attemptService;
        this.progressService = progressService;
        this.reviewService = reviewService;
    }

    /**
     * 提交一条新练习，并同步推进对应题目的当前快照。
     *
     * <p>同 UUID 命中已有历史时绝不能再次调用 progress 或 review；否则网络重试会虚增状态或生成重复任务。</p>
     */
    @Transactional
    public SubmitAttemptResponse submit(SubmitAttemptRequest request) {
        QuestionAttemptService.AppendAttemptResult append = attemptService.appendIdempotently(request);
        StudyProgressResponse progress = append.duplicated() ? null : progressService.applyAttempt(append.attempt());
        ReviewTaskResponse review = append.duplicated() ? null : reviewService.scheduleAfterAttempt(append.attempt(), progress);
        QuestionAttemptResponse attempt = attemptService.toResponse(append.attempt());
        return new SubmitAttemptResponse(attempt, progress, review);
    }
}
