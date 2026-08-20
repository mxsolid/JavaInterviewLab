package com.javainterviewlab.study.progress.domain;

import com.javainterviewlab.study.attempt.domain.AttemptResultType;

/** 把一次结果转换为掌握度的纯规则，不访问数据库，便于独立验证。 */
public final class MasteryCalculator {
    private MasteryCalculator() { }
    /** CORRECT 未自评时保守落在 BASIC，避免把缺少主观评分直接抬高为熟练。 */
    public static MasteryLevel calculate(MasteryLevel current, AttemptResultType result, Integer rating) {
        return switch (result) {
            case NOT_ANSWERED -> MasteryLevel.UNKNOWN;
            case WRONG -> switch (current) {
                case MASTERED -> MasteryLevel.BASIC;
                case SOLID -> MasteryLevel.SEEN;
                default -> MasteryLevel.UNKNOWN;
            };
            case PARTIAL -> switch (current) {
                case UNKNOWN -> MasteryLevel.SEEN;
                case SEEN -> MasteryLevel.BASIC;
                default -> MasteryLevel.BASIC;
            };
            case CORRECT -> rating == null ? MasteryLevel.BASIC : switch (rating) {
                case 1, 2 -> MasteryLevel.SEEN;
                case 3 -> MasteryLevel.BASIC;
                case 4 -> MasteryLevel.SOLID;
                case 5 -> MasteryLevel.MASTERED;
                default -> throw new IllegalArgumentException("selfRating 必须在 1 到 5 之间");
            };
        };
    }
}
