package com.acme.monitor.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 启动延迟配置类
 * 用于模拟应用启动时间过长的场景，可用于测试监控系统对应用启动时间的监控能力
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.startup.delay")
public class StartupDelayConfig {
    /**
     * 是否启用启动延迟
     */
    private boolean enabled = false;
    
    /**
     * 延迟时间（毫秒），默认为0（无延迟）
     */
    private long duration = 0;
}