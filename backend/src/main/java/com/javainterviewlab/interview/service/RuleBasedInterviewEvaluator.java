package com.javainterviewlab.interview.service;

import com.javainterviewlab.interview.domain.InterviewEvaluationContext;
import com.javainterviewlab.interview.domain.InterviewScore;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 无外部模型时的确定性评分基线。
 *
 * <p>准确性只比较数据库 rubric 中的关键词；其余维度只使用回答本身可证明的结构特征。
 * 规则刻意保持简单透明，避免把启发式结果包装成语义理解。</p>
 */
@Component
public class RuleBasedInterviewEvaluator implements InterviewEvaluator {

    private static final double ACCURACY_MAX = 40D;
    private static final double COMPLETENESS_MAX = 25D;
    private static final double DEPTH_MAX = 20D;
    private static final double EXPRESSION_MAX = 15D;
    private static final int COMPLETE_LENGTH = 240;
    private static final int MIN_EXPRESSION_LENGTH = 40;
    private static final Pattern TOKEN_SPLITTER = Pattern.compile("[\\s,，。；;、：:()（）【】]+");
    private static final Pattern SENTENCE_SPLITTER = Pattern.compile("[。！？!?;；\\n]");
    private static final Set<String> DEPTH_MARKERS = Set.of(
            "原理", "机制", "源码", "并发", "事务", "边界", "异常", "性能", "复杂度", "一致性", "权衡", "原因"
    );
    private static final Set<String> STRUCTURE_MARKERS = Set.of(
            "首先", "其次", "然后", "最后", "第一", "第二", "总结", "因此", "例如"
    );

    @Override
    public InterviewScore evaluate(InterviewEvaluationContext context) {
        String answer = normalize(context.answerText());
        Set<String> rubricTokens = rubricTokens(context.rubricText());
        long keywordHits = rubricTokens.stream().filter(answer::contains).count();
        double keywordRatio = rubricTokens.isEmpty() ? 0D : (double) keywordHits / rubricTokens.size();
        double accuracy = round(Math.min(ACCURACY_MAX, 8D + keywordRatio * 32D));

        int length = context.answerText().trim().length();
        long structureHits = STRUCTURE_MARKERS.stream().filter(answer::contains).count();
        double completeness = round(Math.min(COMPLETENESS_MAX, length / (double) COMPLETE_LENGTH * 18D + Math.min(7D, structureHits * 2.5D)));

        long depthHits = DEPTH_MARKERS.stream().filter(answer::contains).count();
        double depth = round(Math.min(DEPTH_MAX, depthHits * 3D + (length >= 160 ? 4D : 0D)));

        long sentenceCount = Arrays.stream(SENTENCE_SPLITTER.split(context.answerText()))
                .filter(value -> !value.isBlank())
                .count();
        double expression = round(Math.min(EXPRESSION_MAX,
                (length >= MIN_EXPRESSION_LENGTH ? 7D : length / (double) MIN_EXPRESSION_LENGTH * 7D)
                        + Math.min(8D, sentenceCount * 1.5D)));

        return new InterviewScore(
                accuracy, completeness, depth, expression,
                "命中评分依据关键词 " + keywordHits + "/" + rubricTokens.size() + " 个。",
                "回答 " + length + " 字，结构提示词 " + structureHits + " 个。",
                "原理、边界或权衡类信号命中 " + depthHits + " 个。",
                "识别到 " + sentenceCount + " 个表达单元。"
        );
    }

    private Set<String> rubricTokens(String rubricText) {
        Set<String> tokens = new LinkedHashSet<>();
        if (rubricText == null) {
            return tokens;
        }
        for (String token : TOKEN_SPLITTER.split(normalize(rubricText))) {
            if (token.length() >= 2 && token.length() <= 24) {
                tokens.add(token);
            }
            if (tokens.size() >= 12) {
                break;
            }
        }
        return tokens;
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private double round(double value) {
        return Math.round(value * 100D) / 100D;
    }
}
