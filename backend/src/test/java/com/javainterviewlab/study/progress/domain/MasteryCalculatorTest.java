package com.javainterviewlab.study.progress.domain;

import com.javainterviewlab.study.attempt.domain.AttemptResultType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证掌握度规则不依赖数据库，避免 Service 分支修改后悄悄改变学习口径。 */
class MasteryCalculatorTest {

    @Test
    void shouldResetToUnknownWhenNotAnswered() {
        assertThat(MasteryCalculator.calculate(MasteryLevel.MASTERED, AttemptResultType.NOT_ANSWERED, 5))
                .isEqualTo(MasteryLevel.UNKNOWN);
    }

    @Test
    void shouldDowngradeByWrongRule() {
        assertThat(MasteryCalculator.calculate(MasteryLevel.MASTERED, AttemptResultType.WRONG, 1))
                .isEqualTo(MasteryLevel.BASIC);
        assertThat(MasteryCalculator.calculate(MasteryLevel.SOLID, AttemptResultType.WRONG, 1))
                .isEqualTo(MasteryLevel.SEEN);
        assertThat(MasteryCalculator.calculate(MasteryLevel.BASIC, AttemptResultType.WRONG, 1))
                .isEqualTo(MasteryLevel.UNKNOWN);
    }

    @Test
    void shouldAdvancePartialOnlyToBasic() {
        assertThat(MasteryCalculator.calculate(MasteryLevel.UNKNOWN, AttemptResultType.PARTIAL, null))
                .isEqualTo(MasteryLevel.SEEN);
        assertThat(MasteryCalculator.calculate(MasteryLevel.SEEN, AttemptResultType.PARTIAL, null))
                .isEqualTo(MasteryLevel.BASIC);
        assertThat(MasteryCalculator.calculate(MasteryLevel.MASTERED, AttemptResultType.PARTIAL, null))
                .isEqualTo(MasteryLevel.BASIC);
    }

    @Test
    void shouldUseSelfRatingForCorrectAndDefaultToBasicWhenMissing() {
        assertThat(MasteryCalculator.calculate(MasteryLevel.UNKNOWN, AttemptResultType.CORRECT, 1))
                .isEqualTo(MasteryLevel.SEEN);
        assertThat(MasteryCalculator.calculate(MasteryLevel.UNKNOWN, AttemptResultType.CORRECT, 3))
                .isEqualTo(MasteryLevel.BASIC);
        assertThat(MasteryCalculator.calculate(MasteryLevel.UNKNOWN, AttemptResultType.CORRECT, 4))
                .isEqualTo(MasteryLevel.SOLID);
        assertThat(MasteryCalculator.calculate(MasteryLevel.UNKNOWN, AttemptResultType.CORRECT, 5))
                .isEqualTo(MasteryLevel.MASTERED);
        assertThat(MasteryCalculator.calculate(MasteryLevel.UNKNOWN, AttemptResultType.CORRECT, null))
                .isEqualTo(MasteryLevel.BASIC);
    }
}
