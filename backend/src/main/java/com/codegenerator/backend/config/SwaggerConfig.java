package com.codegenerator.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Swagger配置类
 * 用于配置API文档生成
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {
    
    /**
     * 创建API文档的Docket实例
     * @return Docket对象
     */
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                // 指定扫描的包路径
                .apis(RequestHandlerSelectors.basePackage("com.codegenerator.backend.controller"))
                // 指定路径规则
                .paths(PathSelectors.any())
                .build();
    }
}