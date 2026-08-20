package com.javainterviewlab.study.review.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 简单复习策略的间隔配置。
 *
 * <p>所有间隔集中于配置，避免 Service 中散落 {@code plusDays(7)}，也便于按学习产品调整。</p>
 */
@ConfigurationProperties(prefix = "app.review.interval")
public class ReviewIntervalProperties {

    private Duration unknown = Duration.ofDays(1);
    private Duration seen = Duration.ofDays(3);
    private Duration basic = Duration.ofDays(7);
    private Duration solid = Duration.ofDays(14);
    private Duration mastered = Duration.ofDays(30);

    public Duration getUnknown() { return unknown; }
    public void setUnknown(Duration unknown) { this.unknown = unknown; }
    public Duration getSeen() { return seen; }
    public void setSeen(Duration seen) { this.seen = seen; }
    public Duration getBasic() { return basic; }
    public void setBasic(Duration basic) { this.basic = basic; }
    public Duration getSolid() { return solid; }
    public void setSolid(Duration solid) { this.solid = solid; }
    public Duration getMastered() { return mastered; }
    public void setMastered(Duration mastered) { this.mastered = mastered; }
}
