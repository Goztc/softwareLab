package com.itheima.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 异步配置类
 * 启用Spring的异步任务支持，用于RAG向量存储的异步构建
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring Boot会自动使用application.yml中的spring.task.execution配置
}