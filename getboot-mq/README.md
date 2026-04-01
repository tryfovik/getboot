# getboot-mq

MQ 消息能力 starter，当前支持 `rocketmq` 与 `kafka` 两类实现，统一承接消息发送门面、TraceId 透传与实现前缀桥接。

## 作用

- 提供统一消息发送门面 `MqMessageProducer`
- 提供统一消息头扩展点 `MqMessageHeadersCustomizer`
- 提供 RocketMQ / Kafka 两套 Trace 恢复实现
- 保留 RocketMQ 事务消息路由增强

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-mq</artifactId>
</dependency>
```

适合这几类场景：

- 想统一 RocketMQ / Kafka 生产入口，不让业务代码散落 `RocketMQTemplate` / `KafkaTemplate`
- 想把消息头、Trace 透传和监听侧 Trace 恢复这些横切逻辑收敛到模块内
- 想在不重做业务发送 API 的前提下切换或并存不同 MQ 实现

## 当前支持实现

| `getboot.mq.type` | 实现 | 前置条件 | 说明 |
| --- | --- | --- | --- |
| `rocketmq` | `infrastructure.rocketmq.*` | Spring 容器中存在 RocketMQ 基础设施 | 支持事务消息、延迟级别、按 topic 路由事务回查 |
| `kafka` | `infrastructure.kafka.*` | Spring 容器中存在 Spring Kafka 基础设施 | 支持通用发送、异步发送、按 key 有序发送、监听侧 Trace 恢复 |

## 前置条件

- 至少开启 `getboot.mq.enabled=true`
- 通过 `getboot.mq.type` 选择当前启用的实现，默认是 `rocketmq`
- RocketMQ 事务消息仍需要业务侧实现 `TopicTransactionStrategy`
- Kafka 发送 `MqMessage` 对象时，需要底层生产者自行配置合适的序列化器，例如 `JsonSerializer`
- `getboot-observability` 不是前置模块；只使用 MQ Trace 透传时，当前模块也可以独立工作

## 目录约定

- `api.*`：消息模型、生产接口与配置模型
- `spi`：模块通用扩展点
- `spi.rocketmq`：RocketMQ 专属扩展点
- `support`：目标地址与 Trace 支撑
- `infrastructure.rocketmq.*`：RocketMQ 实现与自动装配
- `infrastructure.kafka.*`：Kafka 实现与自动装配

## 配置示例

通用开关与选择器：

```yaml
getboot:
  mq:
    enabled: true
    type: rocketmq
    trace:
      enabled: true
      header-name: TRACE_ID
      mdc-key: traceId
```

RocketMQ：

```yaml
getboot:
  mq:
    enabled: true
    type: rocketmq
    rocketmq:
      enabled: true
      name-server: 127.0.0.1:9876
      producer:
        group: demo-producer-group
        send-message-timeout: 3000
```

Kafka：

```yaml
getboot:
  mq:
    enabled: true
    type: kafka
    kafka:
      enabled: true
      bootstrap-servers: 127.0.0.1:9092
      producer:
        key-serializer: org.apache.kafka.common.serialization.StringSerializer
        value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

## 默认 Bean

- `MqMessageProducer`
  `type=rocketmq` 时注册 `RocketMqMessageProducer`
- `MqMessageProducer`
  `type=kafka` 时注册 `KafkaMqMessageProducer`
- `RocketMQMessageConverter`
  仅在 `type=rocketmq` 时注册增强版 Jackson 消息转换器
- `TopicRoutingTransactionListener`
  仅在 `type=rocketmq` 时注册
- `RocketMqTraceListenerAspect`
  仅在 `type=rocketmq` 时注册
- `KafkaMqTraceListenerAspect`
  仅在 `type=kafka` 时注册

## 扩展点

- 推荐业务侧优先注入 `MqMessageProducer`
- 可通过注册 `MqMessageHeadersCustomizer` Bean 定制消息发送前的头信息
- 可通过注册 `RocketMQMessageConverterCustomizer` Bean 定制 RocketMQ Jackson 消息转换器
- RocketMQ 事务消息路由通过实现 `TopicTransactionStrategy` 扩展
- 能力层消息模型统一收敛到 `com.getboot.mq.api.message.*`
- 能力层生产入口统一收敛到 `com.getboot.mq.api.producer.*`
- 模块级 Trace 配置统一使用 `getboot.mq.trace.*`
- RocketMQ 原生配置统一使用 `getboot.mq.rocketmq.*`，并桥接到底层 `rocketmq.*`
- Kafka 原生配置统一使用 `getboot.mq.kafka.*`，并桥接到底层 `spring.kafka.*`

## 语义差异

- `destination` 在能力层统一表示逻辑目标地址，当前格式仍兼容 `topic[:tag]`
- `rocketmq`：`tag`、`delayLevel`、事务消息都属于已实现能力
- `kafka`：会把 `topic[:tag]` 中的 `topic` 作为真实 topic，`tag` 放入消息头 `GETBOOT_MQ_TAG`
- `kafka`：`sendWithDelay(...)` 与 `sendTransaction(...)` 当前不支持，会直接抛出 `UnsupportedOperationException`
- `kafka`：`sendOrderly(...)` 会把 `hashKey` 映射为 Kafka message key，利用分区键保证同 key 顺序

## 已实现技术栈

- RocketMQ
- Kafka

## 边界 / 补充文档

- 当前统一入口主要面向消息生产、RocketMQ 事务消息路由和 Trace 透传，不负责抽象所有消费端编程模型
- Kafka 这轮只补通用发送能力与监听侧 Trace 恢复，不把 RocketMQ 的延迟级别和事务语义错误上浮成“通用能力”
- 可直接参考 [`getboot-mq.yml.example`](./src/main/resources/getboot-mq.yml.example)
