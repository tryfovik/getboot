# getboot-observability

可观测性 starter。

## 作用

- 提供统一的 `traceId` 生成、透传与响应回写能力
- 提供基于 `MDC` 的日志链路上下文能力
- 提供 Prometheus 指标注册与 Actuator 暴露能力
- 提供 SkyWalking TraceId 桥接能力
- 提供可扩展的 Trace SPI，便于业务自定义链路字段
- 提供日志 `logback-spring.xml` 示例，便于业务统一输出 `tid`
- 提供 `@Async` / Spring 线程池 Trace 上下文自动透传能力
- 同时支持 Servlet、WebFlux 服务端入口建链与 Reactor 上下文传播

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-observability</artifactId>
</dependency>
```

适合这几类场景：

- 想统一请求入口 `traceId`、日志链路字段和出站透传
- 想统一 Prometheus 指标与公共标签
- 想在 Dubbo、MQ、HTTP 客户端之间复用同一套 Trace 上下文

## 前置条件

- 当前没有强制外部基础设施前置条件，基础 Trace 能力引入后即可使用
- 如果要暴露 Prometheus 指标，请同时开放对应 Actuator 端点
- 如果要接 SkyWalking，请确保类路径存在 SkyWalking Trace 工具依赖并补齐 `getboot.observability.skywalking.*`

## 目录约定

- `api.context`：稳定的 Trace 上下文模型
- `api.properties`：Trace、metrics、reactor、skywalking 配置模型
- `spi`：Trace 生成、解析与上下文定制扩展点
- `spi.prometheus`：指标注册扩展点
- `support`：默认 TraceId 生成器和异步传播辅助实现
- `infrastructure.servlet.*`：Servlet 入口建链
- `infrastructure.webflux.*`：WebFlux 入口建链
- `infrastructure.reactor.*`：Reactor 上下文传播
- `infrastructure.prometheus.*`：指标与公共标签增强
- `infrastructure.skywalking.*`：SkyWalking Trace 桥接

## 配置示例

```yaml
getboot:
  observability:
    trace:
      enabled: true                          # 是否启用 Trace 过滤器
      header-name: X-Trace-Id                # Trace 请求头名称，缺失时会自动生成
      response-header-enabled: true          # 是否回写 Trace 响应头
      mdc-key: traceId                       # 日志 MDC 中使用的键名
      request-attribute-name: GETBOOT_TRACE_ID # 请求属性中保存 TraceId 的键名
      async-propagation-enabled: true        # 是否启用异步线程 Trace 上下文透传
    reactor:
      enabled: true                          # 是否启用 Reactor Trace 上下文传播
      automatic-context-propagation-enabled: true # 是否启用 Reactor 自动上下文传播钩子
    metrics:
      enabled: true                          # 是否启用 GetBoot 指标增强
      common-tags:
        app: demo-service                    # 业务系统标识
        env: prod                            # 当前部署环境
    prometheus:
      enabled: true                          # 是否启用 Prometheus 指标导出
    management:
      endpoints:
        web:
          exposure:
            include: health,info,prometheus  # 对外暴露的 Actuator 端点
      endpoint:
        health:
          show-details: always               # 健康检查详情展示策略
    skywalking:
      enabled: true                          # 是否启用 SkyWalking TraceId 桥接
      mdc-key: skywalkingTraceId             # SkyWalking TraceId 写入 MDC 时使用的键名
```

## 默认 Bean

- `TraceIdGenerator`：默认实现为 `UuidTraceIdGenerator`
- `TaskDecorator`：Bean 名称为 `getbootTraceTaskDecorator`
- `TraceTaskDecoratorBeanPostProcessor`
- Servlet 场景默认注册 `FilterRegistrationBean<TraceMdcFilter>`
- WebFlux 场景默认注册 `WebFilter traceWebFilter`
- Prometheus 场景默认注册 `MeterRegistryCustomizer<MeterRegistry>`
- SkyWalking 场景默认注册 `TraceIdResolver` 和 `TraceContextCustomizer`

## 扩展点

- `TraceIdGenerator`
- `TraceIdResolver`
- `ReactiveTraceIdResolver`
- `TraceContextCustomizer`
- `ReactiveTraceContextCustomizer`
- `ObservabilityMeterRegistryCustomizer`
- 主 `traceId` 默认优先取链路系统解析结果，例如 SkyWalking；没有时再回退到请求头与本地生成
- 当前链路 TraceId 会写入 `getboot-support` 提供的 `TraceContextHolder`，供 Dubbo、Feign、MQ 等下游组件继续透传
- `@Async`、`ThreadPoolTaskExecutor`、`SimpleAsyncTaskExecutor` 默认会自动透传 Trace 上下文；若直接使用手工线程池，可通过 `TraceContextPropagationSupport.wrap(...)` 包装任务

## 已实现技术栈

- Servlet
- WebFlux
- Reactor
- Prometheus
- SkyWalking

补充说明：

- 业务项目统一使用 `getboot.observability.trace.*`
- Prometheus 指标增强统一使用 `getboot.observability.metrics.*`
- Actuator 暴露类配置统一使用 `getboot.observability.management.*`
- SkyWalking 桥接统一使用 `getboot.observability.skywalking.*`
- 可直接参考 `src/main/resources/logback-spring.xml.example`
- 也可直接参考 `src/main/resources/getboot-observability-prometheus.yml.example` 与 `src/main/resources/getboot-observability-skywalking.yml.example`
