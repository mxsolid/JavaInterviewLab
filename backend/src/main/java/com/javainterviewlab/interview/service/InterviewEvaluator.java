package com.javainterviewlab.interview.service;

import com.javainterviewlab.interview.domain.InterviewEvaluationContext;
import com.javainterviewlab.interview.domain.InterviewScore;

/**
 * 面试评估变化点接口。
 *
 * <p>P03 只固定本地调用契约。P06 可提供规则实现；外部 LLM 必须是可选实现，不能成为启动依赖。</p>
 */
public interface InterviewEvaluator {

    InterviewScore evaluate(InterviewEvaluationContext context);
}
