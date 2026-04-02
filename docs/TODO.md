# TODO

这份清单只记录“还没做完，但已经明确要推进”的事项。

原则：

- 已完成的结构收敛，不再写回 TODO
- TODO 只保留下一个阶段真实会继续做的内容
- 每一项尽量写成可执行动作，而不是口号

## P1 现有能力补更多实现

- [ ] 在 `getboot-http-client` 继续评估是否补充更通用的客户端封装或新的实现子树
  当前方向评估见 [`getboot-http-client/docs/HTTP_CLIENT_EVOLUTION_PLAN.md`](../getboot-http-client/docs/HTTP_CLIENT_EVOLUTION_PLAN.md)。
- [ ] 在 `getboot-observability` 继续补可观测性桥接实现，保持 `api/spi/support` 稳定

## P1 治理与事务继续扩展

- [ ] 继续完善 `getboot-governance` 的 `Sentinel` 能力抽象
- [ ] 继续完善 `getboot-transaction` 的 `Seata` 接入能力
- [ ] 持续补强 `Seata` 与分库分表场景下的兼容保护与说明

## P1 新组件规划

- [ ] 规划 `getboot-storage` 模块，统一对象存储能力边界
- [ ] 在 `getboot-storage` 下预留 `OSS / MinIO / S3` 实现方向
- [ ] 规划 `getboot-search` 模块，统一搜索能力边界
- [ ] 在 `getboot-search` 下预留 `Elasticsearch / OpenSearch` 实现方向
- [ ] 规划 `getboot-ai` 模块，统一模型调用、提示词、向量检索与工具编排能力边界
- [ ] 在 `getboot-ai` 下预留主流模型、`Embedding`、`Rerank` 与向量存储接入方向
- [ ] 规划 `getboot-sms` 模块，统一短信发送能力边界
- [ ] 在 `getboot-sms` 下预留主流云厂商实现方向
- [ ] 规划 `getboot-idempotency`，或复用现有能力抽出更通用的幂等模块

## P2 常用组件储备

- [ ] 评估是否需要消息重试、延迟消息、死信治理等通用消息增强能力
- [ ] 评估是否需要独立文件上传、预签名、媒体处理支持
- [ ] 评估是否需要统一第三方账号登录或开放平台接入模块
- [ ] 评估是否需要统一邮件发送模块
- [ ] 评估是否需要统一验证码、人机校验、风控接入模块
