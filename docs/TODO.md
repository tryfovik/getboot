# TODO

这份清单只保留当前还没落地的仓库级尾项；已经完成的核心桥接实现、事务兼容说明和新模块边界规划，不再回写这里。

已收敛到文档的内容：

- 新模块规划：[`docs/MODULE_ROADMAP.md`](./MODULE_ROADMAP.md)
- `Seata + ShardingSphere` 兼容说明：[`docs/SEATA_SHARDING_COMPATIBILITY.md`](./SEATA_SHARDING_COMPATIBILITY.md)
- 现有 `getboot-observability` / `getboot-transaction` / `getboot-payment` 的 P1 收口，优先回到模块 README 与模块内文档维护

## P2 常用组件储备

- [ ] 评估是否需要消息重试、延迟消息、死信治理等通用消息增强能力
- [ ] 评估是否需要独立文件上传、预签名、媒体处理支持
- [ ] 评估是否需要统一第三方账号登录或开放平台接入模块
- [ ] 评估是否需要统一邮件发送模块
- [ ] 评估是否需要统一验证码、人机校验、风控接入模块
