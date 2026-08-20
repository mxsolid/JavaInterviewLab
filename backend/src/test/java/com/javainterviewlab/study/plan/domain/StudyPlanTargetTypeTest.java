package com.javainterviewlab.study.plan.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 验证路线目录的目标类型在进入同步逻辑前被严格校验。 */
class StudyPlanTargetTypeTest {

    @Test
    void shouldConvertKnownCatalogValue() {
        assertThat(StudyPlanTargetType.fromCatalogValue("TOPIC")).isEqualTo(StudyPlanTargetType.TOPIC);
    }

    @Test
    void shouldRejectUnknownCatalogValue() {
        assertThatThrownBy(() -> StudyPlanTargetType.fromCatalogValue("ARTICLE"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("targetType");
    }
}
