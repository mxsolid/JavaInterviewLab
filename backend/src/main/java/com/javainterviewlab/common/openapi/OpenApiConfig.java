package com.javainterviewlab.common.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI javaInterviewLabOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Java Interview Lab API")
                .version("V0.1.1")
                .description("Java 后端面试学习系统接口"));
    }
}
