package com.acme.monitor.service;

import com.acme.monitor.config.StartupDelayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class StartupDelayServiceTest {

    @Autowired
    private StartupDelayService startupDelayService;
    
    @Autowired
    private StartupDelayConfig startupDelayConfig;

    @Test
    public void testStartupDelayConfig() {
        // 验证配置类已正确加载
        assertNotNull(startupDelayConfig);
        // 默认情况下启动延迟应该是禁用的
        assertFalse(startupDelayConfig.isEnabled());
        // 默认延迟时间应该是65秒
        assertEquals(65000L, startupDelayConfig.getDuration());
    }

    @Test
    public void testStartupDelayService() {
        // 验证服务类已正确加载
        assertNotNull(startupDelayService);
    }
}