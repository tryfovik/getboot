# getboot-mq 演进规划

这份文档对应 `docs/TODO.md` 里 `getboot-mq` 的两项工作：

- 增加 `Kafka` 实现子树
- 梳理能力层接口，确保 RocketMQ / Kafka 可以共存，不重做外部 API

它不是当前接入文档。当前怎么引、怎么配、现在已经提供什么能力，先看 [`../README.md`](../README.md)；这份文档只回答“下一步怎么把 `getboot-mq` 演进成多实现模块”。

## 1. 当前状态

当前 `getboot-mq` 已经稳定暴露给业务方的核心入口主要有：

- `MqMessage`
  通用消息基类
- `MqMessageProducer`
  对外统一发送入口
- `MqSendReceipt`
  发送回执
- `MqTransactionReceipt`
  事务消息回执
- `MqMessageHeadersCustomizer`
  发送前消息头扩展点
- `MqTraceProperties`
  MQ Trace 透传配置

当前唯一实现是：

- `infrastructure.rocketmq.*`

当前专属扩展点是：

- `spi.rocketmq.TopicTransactionStrategy`
- `spi.rocketmq.RocketMQMessageConverterCustomizer`

当前自动装配主要提供：

- `MqMessageProducer`
  默认实现为 `RocketMqMessageProducer`
- `RocketMQMessageConverter`
- `TopicRoutingTransactionListener`
- `RocketMqTraceListenerAspect`

## 2. 当前已经稳定的能力边界

下一阶段不应推翻的内容：

- 模块名继续保持 `getboot-mq`
- 业务消息模型继续优先收敛在 `MqMessage`
- 业务侧优先注入 `MqMessageProducer`
- 通用头扩展继续通过 `MqMessageHeadersCustomizer`
- Trace 透传配置继续保持在 `getboot.mq.trace.*`
- 不同技术栈实现继续收敛在 `infrastructure` 子树

也就是说，后续即使新增 Kafka，也不应让业务方重新切回原生 `RocketMQTemplate` / `KafkaTemplate` 当主入口。

## 3. 当前接口里已经暴露出的 RocketMQ 偏置

`MqMessageProducer` 虽然名字是通用能力接口，但当前已经混入了几类 RocketMQ 偏置语义。

### 3.1 `topic + tag`

当前接口支持：

- `send(String topic, String tag, T message)`
- `asyncSend(String topic, String tag, T message)`
- `sendWithDelay(String topic, String tag, T message, int delayLevel)`
- `sendBatch(String topic, String tag, List<T> messages)`
- `sendOrderly(String topic, String tag, T message, String hashKey)`
- `sendTransaction(String topic, String tag, T message, Object arg)`

问题不在“能不能用”，而在：

- `tag` 是 RocketMQ 强概念，不是所有 MQ 都有一等价物
- 当前 `destination` 实际采用 `topic:tag` 拼接，也带有 RocketMQ 心智

### 3.2 `delayLevel`

当前延迟发送接口使用：

```java
sendWithDelay(..., int delayLevel)
```

这直接绑定了 RocketMQ 的延迟级别模型。Kafka 没有等价的标准 `delayLevel` 语义，通常需要：

- 延迟主题
- 定时调度
- 外部延迟队列

因此这里不能简单假设“Kafka 也支持同一个延迟参数语义”。

### 3.3 `TopicTransactionStrategy`

当前事务消息扩展点：

- `TopicTransactionStrategy`

直接返回：

- `RocketMQLocalTransactionState`

这说明当前事务消息 SPI 还不是通用 SPI，而是 RocketMQ 专属 SPI。

### 3.4 事务回执

`MqTransactionReceipt` 里目前有：

- `transactionId`

这对 RocketMQ 合理，但对 Kafka 而言通常没有完全对应的统一事务消息 ID 语义。这个字段还能继续保留，但需要明确：

- 它是“渠道可提供时返回”的回执字段
- 不是所有实现都必须保证语义完全一致

## 4. 下一阶段的目标

`getboot-mq` 下一阶段的目标不是“现在就抽象出完美 MQ 标准”，而是：

- 不推翻现有业务入口
- 允许 RocketMQ / Kafka 两种实现共存
- 把当前已经明显偏 RocketMQ 的能力显式分层
- 先收敛真正跨技术栈稳定的部分，再把强技术栈绑定能力放回各自实现或专属 SPI

一句话概括：

- `api` 保留“通用消息能力”
- `spi` 保留“通用扩展点”
- `spi.rocketmq` / `spi.kafka` 只保留各自技术栈专属扩展
- `infrastructure.rocketmq` / `infrastructure.kafka` 负责实现差异

## 5. 推荐的模块形态

目标目录：

```text
com.getboot.mq
├── api
│   ├── message
│   ├── model
│   ├── producer
│   └── properties
├── spi
├── spi.rocketmq
├── spi.kafka
├── support
└── infrastructure
    ├── rocketmq
    └── kafka
```

其中：

- `spi.kafka` 只在 Kafka 确实需要业务方可扩展点时再新增
- 如果某类逻辑只是实现内部协作，不必强行抽成公开 SPI

## 6. 能力层应该继续稳定的部分

### 6.1 消息模型

`MqMessage` 当前仍然适合作为通用消息基类保留。原因：

- `bizKey`
- `version`
- `messageId`
- `traceId`
- `sendTime`
- `retryCount`
- `maxRetries`
- `sourceSystem`

这些字段不绑定某个 MQ 技术栈，后续 RocketMQ / Kafka 都可以复用。

### 6.2 发送回执

`MqSendReceipt` 目前只有：

- `destination`
- `messageId`

这个模型仍然可以保留，因为它表达的是最小成功回执，而不是底层发送元数据全量镜像。

### 6.3 通用头扩展

`MqMessageHeadersCustomizer` 应继续作为模块通用 SPI 保留。原因：

- 它不暴露 RocketMQ 或 Kafka 类型
- 它描述的是“发送前补充通用头信息”
- 后续 Kafka 实现也可以复用相同扩展点，只是在内部决定如何映射到底层 header

## 7. 需要重新分层但不宜直接删除的部分

### 7.1 `MqMessageProducer`

当前不建议直接推翻 `MqMessageProducer`，因为 README、业务接入心智和已有调用都已经围绕它展开。

但建议把它视为两层语义的混合体：

- 通用发送能力
- RocketMQ 偏置能力

下一阶段应先做语义划分，再决定如何演进接口。

推荐分组：

通用能力：

- `send(destination, message)`
- `asyncSend(destination, message)`
- `sendBatch(topic, tag, messages)` 暂时保留，但后续最好补一个更通用的批量发送入口
- `sendOrderly(..., hashKey)` 可解释为“按分区键有序发送”，仍有机会保留

RocketMQ 偏置能力：

- `topic + tag` 这一组重载
- `sendWithDelay(..., delayLevel)`
- `sendTransaction(..., arg)` 当前配套的事务策略 SPI

短期建议：

- 保留现有接口，不做破坏性修改
- 在文档里明确哪些方法是“当前通用入口”，哪些只是“RocketMQ 已实现能力”

中期建议：

- 为通用能力补一个更明确的消息目标模型，而不是长期依赖 `topic:tag`
- 延迟与事务能力逐步下沉到技术栈专属扩展

## 8. `destination` 语义规划

当前 `destination` 被 RocketMQ 实现解释为：

```text
topic[:tag]
```

如果未来 Kafka 也要共存，建议不要立刻推翻这个字符串入口，但要在模块内部明确：

- `destination` 是能力层的“逻辑目标地址”
- RocketMQ 可以继续映射为 `topic[:tag]`
- Kafka 默认应优先映射为 `topic`

也就是说，后续要避免把 `destination` 继续等同于 RocketMQ 原生格式。

更稳妥的下一步是补一个内部或能力层的目标描述模型，例如：

- 逻辑主题
- 可选子通道 / 标签
- 额外路由键

但这一步是否放到 `api`，需要等 Kafka 实现真正进入时再决定，不应先凭空制造 API。

## 9. 延迟与事务能力的处理原则

### 9.1 延迟消息

延迟消息不应继续被视为完全通用能力。建议原则：

- `sendWithDelay(..., delayLevel)` 暂时保留兼容
- README 与后续设计中明确它是 RocketMQ 当前实现能力
- 如果 Kafka 后续需要延迟能力，优先在实现层新增 Kafka 自己的方案，不强行把 `delayLevel` 套过去

换句话说：

- 保持兼容
- 不强行抽象错误的通用语义

### 9.2 事务消息

事务消息同样不应继续伪装成完全通用能力。

建议原则：

- 当前 `TopicTransactionStrategy` 明确保留在 `spi.rocketmq`
- 如果 Kafka 后续需要事务支持，应评估是否新增 `spi.kafka`
- 不把 `RocketMQLocalTransactionState` 上浮到通用 `spi`

同时，`MqTransactionReceipt` 可以先保留，但文档上要明确它只是“能力层事务回执”，不是所有技术栈都保证字段语义完全等价。

## 10. Trace 透传继续统一

`getboot.mq.trace.*` 当前是很适合继续保留的统一入口：

- `enabled`
- `header-name`
- `mdc-key`

后续建议：

- RocketMQ 与 Kafka 都复用同一套能力层 Trace 配置
- 各自实现决定如何映射 header / record header / listener context
- 消费端 Trace 恢复逻辑继续留在各自实现子树

也就是说，Trace 是可以继续保持统一能力层的，不需要因为新增 Kafka 而分裂配置根。

## 11. 推荐的落地顺序

建议按下面顺序推进：

1. 先把当前 API 分成“通用稳定部分”和“RocketMQ 专属部分”的文档边界
2. 再增加 `infrastructure.kafka.*`
3. Kafka 实现优先只覆盖真正通用的发送能力与 Trace 透传
4. 对延迟、事务、专属回调这类强绑定能力，先留在技术栈专属 SPI
5. 最后再决定是否需要新的能力层目标模型或更细的生产接口拆分

原因：

- 先把边界讲清楚，能避免为了 Kafka 盲目改坏现有 RocketMQ 接口
- 先做通用能力，能验证 `getboot-mq` 作为多实现模块是否站得住
- 专属能力后补，更符合“能力层稳定、实现层演进”的仓库原则

## 12. 完成标准

`getboot-mq` 这一段 TODO 可以认为真正完成，至少满足：

- 模块下新增 `infrastructure.kafka.*` 实现子树
- RocketMQ / Kafka 可以在不重做业务入口的前提下共存
- 文档已经明确哪些是通用能力、哪些是 RocketMQ 专属能力
- `spi.rocketmq` 继续承接 RocketMQ 专属扩展，不把专属类型抬进通用 `spi`
- `getboot.mq.trace.*` 继续作为统一 Trace 配置根
- README 能明确说明“当前已支持实现”和“不同实现的前置条件 / 语义差异”
