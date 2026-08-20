package com.javainterviewlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * 后端服务启动入口。
 *
 * <p>包根保持在此处，后续内容域、学习域和场景域均可被同一个组件扫描范围覆盖。</p>
 */
@SpringBootApplication
public class JavaInterviewLabApplication {

    /**
     * 提供统一系统时钟。
     *
     * <p>学习路线、复习和统计都通过注入读取时间，测试可以替换 Clock 而无需依赖机器当前日期。</p>
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    public static void main(String[] args) {
        SpringApplication.run(JavaInterviewLabApplication.class, args);
    }
}
