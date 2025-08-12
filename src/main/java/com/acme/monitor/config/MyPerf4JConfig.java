package com.acme.monitor.config;

import org.springframework.context.annotation.Configuration;

/**
 * MyPerf4J配置类
 * 
 * 用于集成MyPerf4J性能监控工具与Spring Boot Actuator
 * MyPerf4J是一个针对高并发、低延迟应用设计的高性能Java性能监控和统计工具
 */
//@Configuration
public class MyPerf4JConfig {
    
    /**
     * MyPerf4J通过Java Agent方式工作，无需在应用代码中显式调用
     * 此配置类主要用于标记和未来可能的扩展
     * 
     * 集成说明：
     * 1. MyPerf4J通过-javaagent参数启动
     * 2. 配置文件通过-DMyPerf4JPropFile参数指定
     * 3. 监控指标会自动输出到指定的日志文件中
     * 4. 可以通过分析日志文件了解应用启动过程中的性能瓶颈
     */
    
}