# TODO

这份清单只记录“还没做完，但已经明确要推进”的事项。

原则：

- 已完成的结构收敛，不再写进 TODO
- TODO 只保留下一阶段真正会继续做的内容
- 每一项都尽量写成可执行动作，而不是口号

## P1 限流能力继续扩展

- [ ] 在 `getboot-limiter` 增加 `token bucket` 实现子树
- [ ] 在 `getboot-limiter` 增加 `leaky bucket` 实现子树
- [ ] 把限流规则模型继续抽象到能兼容多算法实现
- [ ] 为不同限流实现补统一示例与 README

## P1 分布式锁继续扩展

- [ ] 在 `getboot-lock` 增加数据库分布式锁实现子树
- [ ] 在 `getboot-lock` 增加 ZooKeeper 分布式锁实现子树
- [ ] 统一不同锁实现的默认失败策略、Key 解析策略与扩展点文档

## P1 现有能力补更多实现

- [ ] 在 `getboot-mq` 增加 `Kafka` 实现子树
- [ ] 梳理 `getboot-mq` 的能力层接口，确保 RocketMQ / Kafka 可以共存，不重做外部 API
- [ ] 在 `getboot-database` 评估并预留 `MongoDB` 实现方向
- [ ] 在 `getboot-http-client` 继续评估是否补充更通用的客户端封装或新的实现子树
- [ ] 在 `getboot-observability` 继续补可观测性桥接实现，保持 `api/spi/support` 稳定

## P1 治理与事务继续扩展

- [ ] 继续完善 `getboot-governance` 的 Sentinel 能力抽象
- [ ] 继续完善 `getboot-transaction` 的 Seata 接入能力
- [ ] 持续补强 Seata 与分库分表场景下的兼容保护与说明

## P1 新组件规划

- [ ] 规划 `getboot-storage` 模块，统一对象存储能力边界
- [ ] 在 `getboot-storage` 下预留 `OSS` / `MinIO` / `S3` 实现方向
- [ ] 规划 `getboot-search` 模块，统一搜索能力边界
- [ ] 在 `getboot-search` 下预留 `Elasticsearch` / `OpenSearch` 实现方向
- [ ] 规划 `getboot-ai` 模块，统一模型调用、提示词、向量检索与工具编排能力边界
- [ ] 在 `getboot-ai` 下预留主流模型、Embedding、Rerank 与向量存储接入方向
- [ ] 规划 `getboot-sms` 模块，统一短信发送能力边界
- [ ] 在 `getboot-sms` 下预留主流云厂商实现方向
- [ ] 规划 `getboot-idempotency` 或复用现有能力抽出更通用的幂等模块

## P2 常用组件储备

- [ ] 评估是否需要消息重试、延迟消息、死信治理等通用消息增强能力
- [ ] 评估是否需要独立文件上传 / 预签名 / 媒体处理支持
- [ ] 评估是否需要统一第三方账号登录或开放平台接入模块
- [ ] 评估是否需要统一邮件发送模块
- [ ] 评估是否需要统一验证码、人机校验、风控接入模块

