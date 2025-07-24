# Java Monitor 分布式单元化监控系统

一个用于分布式单元化架构的监控系统，支持参数级联过滤，适用于微服务架构下的可观测性需求。

## 功能特性

- **多维度监控指标采集**：
  - 单元可用性监控 (unit_up)
  - RTO/RPO 指标 (unit_rto_seconds, unit_rpo_seconds)
  - 资源利用率监控 (CPU、内存、磁盘IO、网络)
  - 业务指标 (QPS、延迟、成功率)
  - 分布式链路追踪 (RTT、错误率、MQ延迟)
  - 基础设施监控 (JVM、Tomcat、数据库连接池)
  - 安全指标 (登录失败次数、WAF拦截)
  - 成本监控 (每日成本、资源闲置率)

- **参数级联过滤**：
  - 支持通过 `-Dunit.id=U01` 指定单元ID
  - 自动为所有指标添加单元标签

- **缓存支持**：
  - 两级缓存架构 (Caffeine + Redis)
  - 自动缓存穿透、击穿保护

- **可视化大屏**：
  - 集成 Grafana 4K 大屏展示
  - Prometheus 指标导出

- **自动告警规则生成**：
  - 根据业务特点自动生成 Prometheus 告警规则

- **启动时间模拟**：
  - 支持模拟应用启动时间过长场景
  - 可配置开关控制是否启用延迟

## 技术架构

- **后端框架**：Spring Boot 2.7.18
- **缓存**：Caffeine (本地) + Redis (分布式)
- **监控**：Micrometer + Prometheus + Spring Boot Actuator
- **数据库**：H2 (运行时) + JPA
- **构建工具**：Maven
- **其他**：Lombok、JSON Schema Validator

## 一键启动

```bash
mvn spring-boot:run
```

- 访问 Prometheus 指标：http://localhost:8080/actuator/prometheus
- 支持参数过滤：`mvn spring-boot:run -Dspring-boot.run.arguments=--unit.id=U01`

## 可视化大屏

1. 启动 Prometheus + Grafana（推荐 docker-compose）
2. Prometheus 配置 job 指向本服务 8080 端口
3. Grafana 导入 `src/main/resources/grafana-dashboard.json` 即可获得4K大屏

## 主要监控指标

- 单元可用性 unit_up
- RTO/RPO unit_rto_seconds, unit_rpo_seconds
- 资源利用率 unit_cpu_pct, unit_mem_pct, unit_disk_io_mb, unit_net_bps
- 业务 unit_qps_total, unit_latency_p99_ms, unit_success_rate
- 分布式链路 unit_rtt_ms, unit_error_rate, unit_mq_lag
- 基础设施 jvm_memory_used_bytes, tomcat_threads_busy, hikaricp_connections_active, db_query_latency_ms
- 安全 login_fail_total, waf_block_total
- 成本 unit_daily_cost_yuan, unit_idle_pct

## 阈值规则（自动生成 prometheus_rules.yml）

- unit_up == 0          → 红色
- unit_latency_p99 > 300ms → 橙色
- unit_cpu_pct > 80%    → 黄色
- unit_mq_lag > 1000    → 红色闪烁

## API 接口

### 健康检查接口

- `GET /api/health/status` - 获取当前单元健康状态和主要指标

### 缓存测试接口

- `GET /api/cache/user/{id}` - 获取用户信息（带缓存）
- `POST /api/cache/user` - 创建用户（更新缓存）
- `DELETE /api/cache/user/{id}` - 删除用户（清除缓存）
- `DELETE /api/cache/user/clear` - 清空所有用户缓存

## 配置说明

系统主要配置项在 `src/main/resources/application.yml` 中：

```yaml
server:
  port: 8080

spring:
  # 数据库配置
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: ""
    
  # JPA配置
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        
  # Redis配置
  redis:
    host: localhost
    port: 6379
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        
  # 缓存类型
  cache:
    type: redis

# 应用名称
application:
  name: java-monitor
  
# 监控配置
management:
  endpoints:
    web:
      exposure:
        include: '*'
  metrics:
    tags:
      unit_id: ${unit.id:U01}  # 默认单元ID为U01，可通过启动参数修改
  endpoint:
    prometheus:
      enabled: true

# 启动延迟配置（用于模拟启动时间过长）
app:
  startup:
    delay:
      # 是否启用启动延迟模拟
      enabled: false
      # 延迟时间（毫秒），默认65秒（超过60秒）
      duration: 65000
```

## 开发指南

### 环境要求

- JDK 1.8+
- Maven 3.x
- Redis 服务（可选，用于Redis缓存测试）

### 构建项目

```bash
mvn clean package
```

### 运行项目

```bash
mvn spring-boot:run
```

或者指定单元ID运行：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--unit.id=UNIT02
```

### 运行测试

```bash
mvn test
```

### 模拟启动时间过长

要模拟应用启动时间过长（超过60秒）的情况，可以修改配置文件中的启动延迟设置：

```yaml
app:
  startup:
    delay:
      enabled: true
      duration: 65000  # 65秒
```

或者在启动时通过命令行参数指定：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--app.startup.delay.enabled=true --app.startup.delay.duration=65000"
```

---

> 本系统为单元化+分布式架构监控演示，所有代码带中文注释，支持参数级联过滤。