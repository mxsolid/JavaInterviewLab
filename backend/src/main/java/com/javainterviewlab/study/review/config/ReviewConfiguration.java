package com.javainterviewlab.study.review.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** 注册复习间隔配置，保持策略类只依赖明确的配置对象。 */
@Configuration
@EnableConfigurationProperties(ReviewIntervalProperties.class)
public class ReviewConfiguration {
}
