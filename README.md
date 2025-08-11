# Java Monitor System

单元化+分布式监控系统

## 项目概述

Java Monitor 是一个用于分布式单元化架构的监控系统，支持参数级联过滤，适用于微服务架构下的可观测性需求。

## 系统功能

主要功能:
- 提供 Prometheus 格式的监控指标输出
- 支持单元化架构下的参数过滤
- 提供 Grafana 可视化大屏集成
- 集成多种监控维度（可用性、资源、业务、链路、安全、成本等）
- 自动生成 Prometheus 告警规则

## 技术架构

设计模式:
- AOP: 用于全局异常处理和性能监控
- 缓存模式: 使用 Caffeine + Redis 的两级缓存
- 配置模式: Spring Boot 配置类管理组件行为

## 技术选型

- 后端: Spring Boot 2.7.18
- 数据库: H2（运行时）、Redis、JPA
- 监控: Prometheus + Micrometer + Spring Boot Actuator
- 构建工具: Maven
- 其他: Lombok、JSON Schema Validator、Caffeine、Spring AOP

## 使用说明

### 启动应用

```bash
mvn spring-boot:run
```

或者

```bash
mvn clean package
java -jar target/java-monitor-1.0.0.jar
```

### 监控端点

- 健康检查: http://localhost:8000/actuator/health
- 指标监控: http://localhost:8000/actuator/prometheus
- 其他 Actuator 端点: http://localhost:8000/actuator

### Bean初始化监控端点

- 所有Bean初始化时间: http://localhost:8000/api/monitor/bean/all
- 慢Bean列表(默认50ms以上): http://localhost:8000/api/monitor/bean/slow
- 慢Bean列表(自定义阈值): http://localhost:8000/api/monitor/bean/slow?thresholdMs=100
- 最慢的前N个Bean: http://localhost:8000/api/monitor/bean/top
- 最慢的前N个Bean(自定义数量): http://localhost:8000/api/monitor/bean/top?topN=20

## Bean初始化阶段耗时监控

本系统提供详细的Bean初始化阶段耗时监控功能，可以分别监控Bean在不同阶段的耗时情况：

1. **Before Initialization阶段**：Spring调用postProcessBeforeInitialization方法前的耗时
2. **After Initialization阶段**：Spring调用postProcessAfterInitialization方法的耗时
3. **总耗时**：Bean从开始初始化到初始化完成的总耗时

当Bean初始化时间超过50ms时，系统会自动记录警告日志，显示各阶段的详细耗时信息。

日志示例：
```
Bean myService total init cost: 75ms (beforeInit: 30ms, afterInit: 45ms)
```

在应用关闭时，系统还会打印所有Bean的初始化耗时汇总信息，便于分析启动性能瓶颈。

## MyPerf4J性能监控集成

本项目已集成MyPerf4J性能监控工具，用于监控应用启动过程中的性能瓶颈。

### MyPerf4J特性

- 高性能: 单线程支持每秒 1600 万次响应时间的记录，每次记录只花费 63 纳秒
- 无侵入: 采用 JavaAgent 方式，对应用程序完全无侵入，无需修改应用代码
- 低内存: 采用内存复用的方式，整个生命周期只产生极少的临时对象，不影响应用程序的 GC
- 高实时: 支持秒级统计，最低统计粒度为 1 秒，并且是全量统计，不丢失任何一次记录

### 获取MyPerf4J

由于网络问题，MyPerf4J依赖需要手动下载:

1. 访问MyPerf4J的GitHub发布页面: https://github.com/LinShunKang/MyPerf4J/releases/tag/v3.2.0
2. 下载 `MyPerf4J-ASM-3.2.0.jar` 文件
3. 在项目根目录下创建 `lib` 目录
4. 将下载的jar包放入 `lib` 目录中

或者使用命令行下载(如果网络允许):

```bash
# 创建lib目录
mkdir lib

# 下载MyPerf4J jar包
wget https://github.com/LinShunKang/MyPerf4J/releases/download/v3.2.0/MyPerf4J-ASM-3.2.0.jar -O lib/MyPerf4J-ASM.jar
```

### 配置说明

MyPerf4J配置文件位于: `src/main/resources/myPerf4J.properties`

主要配置项:
- app_name: 应用名称
- metrics.log.method.file: 方法级别监控指标日志文件路径
- filter.packages.include: 需要监控的包前缀
- metrics.time_slice.threshold: 统计时间片（秒）

### 启动时集成

MyPerf4J通过JVM参数-javaagent方式集成:

```bash
java -javaagent:lib/MyPerf4J-ASM.jar -DMyPerf4JPropFile=src/main/resources/myPerf4J.properties -jar target/java-monitor-1.0.0.jar
```

在使用Maven插件启动时，需要取消pom.xml中的注释并配置正确的参数。

### 查看监控数据

启动应用后，MyPerf4J会将方法级别的性能监控数据输出到日志文件中:

- 方法监控日志: `./logs/myperf4j/method_metrics.log`

日志格式示例:
```
MyPerf4J Method Metrics [2020-01-01 12:49:57, 2020-01-01 12:49:58]
Method[6]                            Type        Level  TimePercent      RPS  Avg(ms)  Min(ms)  Max(ms)    StdDev    Count     TP50     TP90     TP95     TP99    TP999   TP9999
DemoServiceImpl.getId2(long)      General      Service      322.50%     6524     0.49        0        1     0.50      6524        0        1        1        1        1        1
DemoServiceImpl.getId3(long)      General      Service      296.10%     4350     0.68        0        1     0.47      4350        1        1        1        1        1        1
```

监控指标说明:
- RPS: 每秒请求数
- Avg: 平均响应时间(ms)
- Min: 最小响应时间(ms)
- Max: 最大响应时间(ms)
- TP50-TP9999: 不同百分位的响应时间
- Count: 调用次数

通过分析这些数据，可以快速定位应用启动过程中的性能瓶颈。

## 目录结构

- aspect: 包含全局异常处理、性能监控切面
- config: 配置类（缓存、数据库、指标等）
- controller: 提供测试和健康检查接口
- model: 数据模型（如 User）
- service: 业务逻辑（如单元指标服务）
- resources: 配置文件、Grafana看板、Prometheus配置等
- test: 单元测试和集成测试代码