# getboot-mq

MQ 消息能力 starter，当前提供 RocketMQ 场景下的消息发送模板、事务消息路由、TraceId 透传与属性别名适配。

## 作用

- 提供统一消息发送门面 `MqMessageProducer`
- 提供 RocketMQ 事务消息路由与 Trace 透传增强
- 暴露消息头、消息转换器与事务策略扩展点

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

- 业务项目统一使用 `getboot.mq.*`
- `getboot.mq.enabled` 控制增强能力是否生效
- `getboot.mq.trace.*` 控制 MQ TraceId 透传行为
- 能力层消息模型统一收敛到 `com.getboot.mq.api.message.*`
- 能力层生产入口统一收敛到 `com.getboot.mq.api.producer.*`
- MQ Trace 公共配置统一收敛到 `com.getboot.mq.api.properties.*`
- 消息头扩展点统一收敛到 `com.getboot.mq.spi.*`
- RocketMQ 专属扩展点统一收敛到 `com.getboot.mq.spi.rocketmq.*`
- RocketMQ 原生配置统一收敛到 `getboot.mq.rocketmq.*`
- RocketMQ 实现相关代码统一收敛到 `com.getboot.mq.infrastructure.rocketmq.*`
- 即便未引入 `getboot-observability`，RocketMQ Trace 透传能力也可独立工作
- 生产端默认优先复用当前线程 `TraceContextHolder` 中的主 `traceId`
- 消费端与事务回查会自动从消息头或消息体恢复 `traceId` 到 `TraceContextHolder` 与日志 `MDC`
- 当前模块内部实现基于 RocketMQ，后续新增 Kafka、RabbitMQ 等消息技术栈时，仍沿用 `getboot-mq` 作为统一消息模块
- 推荐业务侧优先注入 `MqMessageProducer`
- 可通过注册 `MqMessageHeadersCustomizer` Bean 定制消息发送前的头信息
- 可通过注册 `RocketMQMessageConverterCustomizer` Bean 定制 Jackson 消息转换器
- RocketMQ 事务消息路由通过实现 `TopicTransactionStrategy` 扩展

## 已实现技术栈

- RocketMQ
