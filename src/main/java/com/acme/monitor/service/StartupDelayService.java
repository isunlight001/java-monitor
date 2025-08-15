package com.acme.monitor.service;

import com.acme.monitor.config.StartupDelayConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 启动延迟服务类
 * 用于在应用启动时模拟长时间初始化过程
 */
@Service
public class StartupDelayService {
    
    private static final Logger logger = LoggerFactory.getLogger(StartupDelayService.class);
    
    @Autowired
    private StartupDelayConfig startupDelayConfig;
    
    /**
     * 应用启动完成后执行初始化操作
     * 如果启用了启动延迟，则在此处模拟延迟
     */
    @PostConstruct
    public void initialize() {
        // 只有在启用延迟且延迟时间大于0时才执行延迟逻辑
        if (startupDelayConfig.isEnabled() && startupDelayConfig.getDuration() > 0) {
            logger.info("开始模拟应用启动延迟，延迟时间: {} ms", startupDelayConfig.getDuration());
            
            try {
                // 模拟启动延迟
                Thread.sleep(startupDelayConfig.getDuration());
                logger.info("模拟应用启动延迟完成");
            } catch (InterruptedException e) {
                logger.warn("启动延迟过程中被中断", e);
                Thread.currentThread().interrupt();
            }
        } else {
            logger.debug("启动延迟未启用或延迟时间为0，跳过模拟延迟");
        }
    }
}