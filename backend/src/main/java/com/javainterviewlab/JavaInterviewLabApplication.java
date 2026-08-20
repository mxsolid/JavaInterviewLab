package com.javainterviewlab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后端服务启动入口。
 *
 * <p>包根保持在此处，后续内容域、学习域和场景域均可被同一个组件扫描范围覆盖。</p>
 */
@SpringBootApplication
public class JavaInterviewLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaInterviewLabApplication.class, args);
    }
}
