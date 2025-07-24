package com.acme.monitor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 启动延迟配置类
 * 用于模拟应用启动时间过长的场景，可用于测试监控系统对应用启动时间的监控能力
 */
@Component
@ConfigurationProperties(prefix = "app.startup.delay")
public class StartupDelayConfig {
    /**
     * 是否启用启动延迟
     */
    private boolean enabled = false;
    
    /**
     * 延迟时间（毫秒），默认65秒（超过60秒）
     */
    private long duration = 65000;

    /**
     * 获取是否启用启动延迟
     * @return 是否启用启动延迟
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置是否启用启动延迟
     * @param enabled 是否启用启动延迟
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取延迟时间
     * @return 延迟时间（毫秒）
     */
    public long getDuration() {
        return duration;
    }

    /**
     * 设置延迟时间
     * @param duration 延迟时间（毫秒）
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }
}