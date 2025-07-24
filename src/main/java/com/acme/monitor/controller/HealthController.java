package com.acme.monitor.controller;

import com.acme.monitor.service.UnitMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

/**
 * 健康检查与业务指标接口
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    @Autowired
    private UnitMetricsService unitMetricsService;

    /**
     * 获取当前单元健康状态
     */
    @GetMapping("/status")
    public Map<String, Object> status() {
        return unitMetricsService.getUnitStatus();
    }
}
