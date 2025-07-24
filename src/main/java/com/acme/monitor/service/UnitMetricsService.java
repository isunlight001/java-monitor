package com.acme.monitor.service;

import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 单元与分布式监控指标服务
 */
@Service
public class UnitMetricsService {
    /**
     * 获取当前单元健康状态和主要指标
     */
    public Map<String, Object> getUnitStatus() {
        Map<String, Object> map = new HashMap<>();
        map.put("unit_up", 1);
        map.put("unit_rto_seconds", 12);
        map.put("unit_rpo_seconds", 30);
        map.put("unit_cpu_pct", 67);
        map.put("unit_mem_pct", 72);
        map.put("unit_disk_io_mb", 120);
        map.put("unit_net_bps", 800);
        map.put("unit_qps_total", 3200);
        map.put("unit_latency_p99_ms", 210);
        map.put("unit_success_rate", 99.87);
        map.put("unit_daily_cost_yuan", 1234.56);
        map.put("unit_idle_pct", 12.3);
        return map;
    }
}