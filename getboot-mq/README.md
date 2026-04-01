# getboot-mq

MQ 消息能力 starter，当前提供 RocketMQ 场景下的统一发送门面、事务消息路由、TraceId 透传与属性别名适配。

## 作用

- 提供统一消息发送门面 `MqMessageProducer`
- 提供 RocketMQ 事务消息路由与 Trace 透传增强
- 暴露消息头、消息转换器与事务策略扩展点

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-mq</artifactId>
</dependency>
```

适合这几类场景：

- 想统一 RocketMQ 生产入口，不让业务代码散落 `RocketMQTemplate`
- 想把消息头、事务消息回查、Trace 透传这些横切逻辑收敛到模块内
- 计划后续补 Kafka 等实现，但希望外部仍沿用统一消息能力入口

## 前置条件

- 需要准备可访问的 RocketMQ NameServer 与生产者组配置
- 至少开启 `getboot.mq.enabled=true`
- 如果要使用事务消息，业务侧需要实现 `TopicTransactionStrategy`
- `getboot-observability` 不是前置模块；只使用 MQ Trace 透传时，当前模块也可以独立工作

## 目录约定

- `api.*`：消息模型、生产接口与配置模型
- `spi`：模块通用扩展点
- `spi.rocketmq`：RocketMQ 专属扩展点
- `support`：内部辅助入口
- `infrastructure.rocketmq.*`：RocketMQ 实现与自动装配

## 配置示例

```yaml
getboot:
  mq:
    enabled: true                    # 是否启用 GetBoot MQ 增强能力
    trace:
      enabled: true                  # 是否启用 MQ TraceId 透传
      header-name: TRACE_ID          # MQ 消息头中保存 TraceId 的键名
      mdc-key: traceId               # 日志 MDC 中保存 TraceId 的键名
    rocketmq:
      name-server: 127.0.0.1:9876    # RocketMQ NameServer 地址
      producer:
        group: demo-producer-group   # RocketMQ 生产者组
        send-message-timeout: 3000   # RocketMQ 发送超时时间，单位毫秒
```

## 默认 Bean

- `MqMessageProducer`：默认实现为 `RocketMqMessageProducer`
- `RocketMQMessageConverter`：增强版 Jackson 消息转换器
- `TopicRoutingTransactionListener`：按 topic 路由事务消息回查
- `RocketMqTraceListenerAspect`：消费端 Trace 恢复切面

## 扩展点

- 推荐业务侧优先注入 `MqMessageProducer`
- 可通过注册 `MqMessageHeadersCustomizer` Bean 定制消息发送前的头信息
- 可通过注册 `RocketMQMessageConverterCustomizer` Bean 定制 Jackson 消息转换器
- RocketMQ 事务消息路由通过实现 `TopicTransactionStrategy` 扩展
- `getboot.mq.enabled` 控制增强能力是否生效
- 能力层消息模型统一收敛到 `com.getboot.mq.api.message.*`
- 能力层生产入口统一收敛到 `com.getboot.mq.api.producer.*`
- 模块级 Trace 配置统一使用 `getboot.mq.trace.*`
- RocketMQ 原生配置统一使用 `getboot.mq.rocketmq.*`，并桥接到底层 `rocketmq.*`
- 消费端与事务回查会自动从消息头或消息体恢复 `traceId` 到 `TraceContextHolder` 与日志 `MDC`

## 已实现技术栈

- RocketMQ

## 边界 / 补充文档

- 当前统一入口主要面向消息生产、事务消息路由和 Trace 透传，不负责抽象所有消费端编程模型
- 当前内部实现只有 RocketMQ；后续如果新增 Kafka，也仍沿用 `getboot-mq` 作为统一消息模块
- `getboot.mq.trace.*` 由 `@ConfigurationProperties` 直接承接；`getboot.mq.rocketmq.*` 通过环境别名桥接到底层 RocketMQ 原生前缀
- 可直接参考 `src/main/resources/getboot-mq.yml.example`
- 如果你关心下一阶段 RocketMQ / Kafka 共存时的接口收敛，而不是当前接入方式，先看主 README，再看 [`docs/MQ_EVOLUTION_PLAN.md`](./docs/MQ_EVOLUTION_PLAN.md)
