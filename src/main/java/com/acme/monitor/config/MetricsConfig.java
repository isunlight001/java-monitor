package com.acme.monitor.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import javax.annotation.PostConstruct;

/**
 * Prometheus 监控配置
 */
@Configuration
public class MetricsConfig {
    @Value("${unit.id:U01}")
    private String unitId;

    private final MeterRegistry meterRegistry;

    public MetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void customizeCommonTags() {
        // 全局打上单元标签
        meterRegistry.config().commonTags("unit_id", unitId);
    }
}
