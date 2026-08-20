package com.javainterviewlab.interview;

import com.javainterviewlab.interview.domain.InterviewEvaluationContext;
import com.javainterviewlab.interview.domain.InterviewScore;
import com.javainterviewlab.interview.service.RuleBasedInterviewEvaluator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedInterviewEvaluatorTest {

    private final RuleBasedInterviewEvaluator evaluator = new RuleBasedInterviewEvaluator();

    @Test
    void shouldKeepFourDimensionsWithinStableMaximums() {
        InterviewScore score = evaluator.evaluate(new InterviewEvaluationContext(
                1L, 1, "HashMap 为什么扩容",
                "首先说明扩容机制，然后从源码解释桶位置变化，最后补充并发边界与性能权衡。",
                "扩容 源码 桶位置 并发 边界 性能 权衡"
        ));

        assertThat(score.accuracy()).isBetween(0D, 40D);
        assertThat(score.completeness()).isBetween(0D, 25D);
        assertThat(score.depth()).isBetween(0D, 20D);
        assertThat(score.expression()).isBetween(0D, 15D);
        assertThat(score.total()).isBetween(0D, 100D);
        assertThat(score.accuracyReason()).contains("关键词");
    }
}
