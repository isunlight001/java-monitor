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

### Bean详细信息端点

- 所有Bean详细信息: http://localhost:8000/api/monitor/bean/details/all
- 特定Bean详细信息: http://localhost:8000/api/monitor/bean/details/{beanName}

### 未使用Bean检测端点

- 所有Bean列表: http://localhost:8000/api/monitor/beans/all
- 可能未使用的Bean: http://localhost:8000/api/monitor/beans/unused
- 可能的孤儿Bean: http://localhost:8000/api/monitor/beans/orphans

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

## Bean初始化操作分析

系统还能分析Bean初始化过程中执行的具体操作，包括：

1. **@PostConstruct注解方法**：标记了@PostConstruct注解的方法
2. **InitializingBean接口**：实现了InitializingBean接口的Bean及其afterPropertiesSet()方法
3. **类信息**：Bean的类名、字段数量、方法数量等基本信息

这些信息有助于定位Bean初始化时具体执行了哪些操作，从而找出性能瓶颈的根本原因。

详细信息示例：
```
Bean myService initialization operations: [PostConstruct method: init, InitializingBean.afterPropertiesSet(), Class: com.example.MyService, Fields count: 5, Methods count: 12]
```

## 未使用Bean检测

系统新增了检测未使用Bean的功能，可以帮助识别Spring容器中可能无用的组件。这些组件可能是项目演进过程中遗留下来的，占用内存和启动时间。

### 检测原理

1. **未使用Bean**：指没有被其他Bean依赖的Bean，即入度为0的Bean
2. **孤儿Bean**：指既没有被其他Bean依赖，自身也没有依赖其他Bean的Bean

### 使用方法

通过以下REST API端点可以获取相关信息：

1. `GET /api/monitor/beans/all` - 获取所有Bean列表
2. `GET /api/monitor/beans/unused` - 获取可能未使用的Bean列表
3. `GET /api/monitor/beans/orphans` - 获取可能的孤儿Bean列表

### 分析建议

检测到的未使用Bean需要进一步人工分析确认：

1. 某些Bean可能是通过名称动态获取的，不会在依赖关系中体现
2. 某些Bean可能是为了扩展性而预先注册的组件
3. 某些Bean可能在特定条件下才会被使用
4. 某些Bean可能是测试或调试用途的组件

建议结合业务逻辑和代码分析来判断这些Bean是否真的无用，再决定是否移除。

### 清除无用Bean

关于如何清除无用Bean以加快应用启动时间，请参考详细指南：[清除无用Bean指南](docs/unused-beans-removal-guide.md)

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

下载完成后，取消pom.xml中关于MyPerf4J依赖的注释：

```xml
<dependency>
    <groupId>cn.myperf4j</groupId>
    <artifactId>MyPerf4J-ASM</artifactId>
    <version>3.2.0</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/MyPerf4J-ASM.jar</systemPath>
</dependency>
```

### 配置说明

MyPerf4J配置文件位于: `src/main/resources/myPerf4J.properties`

主要配置项:
- app_name: 应用名称
- metrics.log.method.file: 方法级别监控指标日志文件路径
- filter.packages.include: 需要监控的包前缀，当前配置为只监控本项目相关类
- metrics.time_slice.threshold: 统计时间片（秒），默认为10秒

### 启动时集成

MyPerf4J通过JVM参数-javaagent方式集成。有两种启动方式：

#### 方式一：使用Maven插件启动（推荐开发环境使用）

取消pom.xml中spring-boot-maven-plugin插件配置的注释：

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <!-- MyPerf4J集成说明：需要在启动时添加JVM参数 -->
    <configuration>
        <jvmArguments>
            -javaagent:lib/MyPerf4J-ASM.jar -DMyPerf4JPropFile=src/main/resources/myPerf4J.properties
        </jvmArguments>
    </configuration>
</plugin>
```

然后使用标准的Maven命令启动：

```bash
mvn spring-boot:run
```

#### 方式二：使用java命令启动（推荐生产环境使用）

编译项目：

```bash
mvn clean package
```

使用java命令启动应用：

```bash
java -javaagent:lib/MyPerf4J-ASM.jar -DMyPerf4JPropFile=src/main/resources/myPerf4J.properties -jar target/java-monitor-1.0.0.jar
```

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

字段说明：
- Method: 被监控的方法名
- Type: 方法类型
- Level: 方法层级
- TimePercent: 时间占比
- RPS: 每秒请求数
- Avg(ms): 平均响应时间(毫秒)
- Min(ms): 最小响应时间(毫秒)
- Max(ms): 最大响应时间(毫秒)
- StdDev: 响应时间标准差
- Count: 调用次数
- TP50/TP90/TP95/TP99/TP999/TP9999: 不同百分位的响应时间

### MyPerf4J在本项目中的作用

在本项目中，MyPerf4J主要用于监控以下方面：
1. Bean初始化过程中的性能表现
2. 各种监控切面的执行耗时
3. 缓存操作的性能指标
4. 数据库操作的响应时间
5. 应用启动过程中的方法调用性能

通过这些监控数据，可以帮助开发者识别性能瓶颈，优化代码执行效率。

## 目录结构

- aspect: 包含全局异常处理、性能监控切面
- config: 配置类（缓存、数据库、指标等）
- controller: 提供测试和健康检查接口
- model: 数据模型（如 User）
- service: 业务逻辑（如单元指标服务）
- resources: 配置文件、Grafana看板、Prometheus配置等
- test: 单元测试和集成测试代码