package com.acme.monitor.config;

import com.acme.monitor.aspect.BeanInitMonitor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean初始化监控配置类
 * 
 * 用于注册BeanInitMonitor到Spring容器中，监控所有Bean的初始化耗时
 */
@Configuration
public class BeanInitMonitorConfig {
    
    /**
     * 注册Bean初始化监控器
     * 该监控器会监控所有Spring Bean的初始化过程，记录初始化耗时超过50ms的Bean
     * 
     * @return BeanInitMonitor实例
     */
    @Bean
    public BeanInitMonitor beanInitMonitor() {
        return new BeanInitMonitor();
    }
}