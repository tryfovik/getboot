# Module Map

这份文档回答 4 个问题：

1. 仓库里有哪些模块
2. 新项目通常按什么顺序接入
3. 每个模块解决什么问题、应该看哪个配置前缀
4. 扩展点统一应该去哪里找

## 1. 依赖方向

总体依赖方向固定为：

`Foundation -> Infrastructure Capability -> Cross-Cutting / Communication -> Ecosystem`

约束很简单：

- Foundation 保持轻量，不反向依赖业务能力模块
- Infrastructure Capability 提供底座能力
- Cross-Cutting 和 Communication 优先复用底座
- Ecosystem 模块面向具体生态，不反向定义全仓公共规范

## 2. 接入顺序建议

第一次接一个新业务服务，通常按下面顺序判断：

1. 先确定是不是 HTTP 服务
   - 是：先看 `getboot-web`
2. 再决定要不要统一 Trace、日志上下文、指标
   - 要：再看 `getboot-observability`
3. 再补底座能力
   - Redis：`getboot-cache`
   - 分布式协调：`getboot-coordination`
   - 数据访问：`getboot-database`
4. 最后再补横切和生态能力
   - 锁、限流、Webhook、鉴权、RPC、MQ、支付、微信生态

不要反过来一上来先接支付、Webhook、Dubbo，再回头补基础设施。

## 3. 模块接入矩阵

| 模块 | 对外语义 | 什么时候引入 | 常见搭配 | 关键配置前缀 | 当前实现 | 模块文档 |
| --- | --- | --- | --- | --- | --- | --- |
| `getboot-support` | 通用支撑 | 只有在开发新 starter 或要手工使用上下文透传时，才建议直接关注 | `getboot-observability` / `getboot-http-client` / `getboot-rpc` / `getboot-mq` | 无强制配置 | `autoconfigure` / `environment` / `trace` | [getboot-support/README.md](../getboot-support/README.md) |
| `getboot-exception` | 错误码与异常 | 纯领域模块或共享模型模块需要统一错误码时 | `getboot-web` / `getboot-auth` / `getboot-rpc` / `getboot-payment` | 无强制配置 | 纯 `api` 模型 | [getboot-exception/README.md](../getboot-exception/README.md) |
| `getboot-web` | Web 通用模型与异常收口 | 业务项目要直接对外提供 Spring MVC 接口时 | `getboot-observability` / `getboot-auth` | 无强制配置 | `servlet` | [getboot-web/README.md](../getboot-web/README.md) |
| `getboot-cache` | Redis 缓存能力 | 需要统一 Redis 接入、默认模板和缓存门面时 | `getboot-coordination` / `getboot-lock` / `getboot-webhook` | `getboot.cache.redis.*` | `redis` | [getboot-cache/README.md](../getboot-cache/README.md) |
| `getboot-coordination` | 分布式协调底座 | 需要 Redisson / Curator 协调基础设施，或者准备接锁、限流、Webhook 时 | `getboot-lock` / `getboot-limiter` / `getboot-webhook` | `getboot.coordination.redisson.*` / `getboot.coordination.zookeeper.*` | `redisson` / `zookeeper.curator` | [getboot-coordination/README.md](../getboot-coordination/README.md) |
| `getboot-database` | 数据访问增强 | 需要数据源预热、MongoDB 启动校验、MyBatis-Plus、ShardingSphere 时 | `getboot-transaction` | `getboot.database.*` | `datasource` / `mongodb` / `mybatisplus` / `sharding` | [getboot-database/README.md](../getboot-database/README.md) |
| `getboot-storage` | 对象存储能力 | 需要统一对象上传、下载、删除、元数据读取和预签名 URL 时 | 无强制配套模块；HTTP 上传场景可搭配 `getboot-web` | `getboot.storage.*` | `minio` | [getboot-storage/README.md](../getboot-storage/README.md) |
| `getboot-sms` | 短信发送能力 | 需要统一验证码、通知短信、模板变量与供应商接入时 | 无强制配套模块；验证码或登录场景可搭配 `getboot-web` | `getboot.sms.*` | `aliyun` | [getboot-sms/README.md](../getboot-sms/README.md) |
| `getboot-search` | 搜索能力 | 需要统一索引写入、文档删除、基础查询、分页、排序和高亮结果时 | 无强制配套模块；检索增强场景可按需搭配 `getboot-ai` / `getboot-storage` | `getboot.search.*` | `elasticsearch` | [getboot-search/README.md](../getboot-search/README.md) |
| `getboot-observability` | 可观测性 | 需要统一 Trace、日志上下文、指标、SkyWalking 时 | `getboot-http-client` / `getboot-rpc` / `getboot-mq` | `getboot.observability.trace.*` / `metrics.*` / `prometheus.*` / `management.*` / `skywalking.*` | `servlet` / `webflux` / `reactor` / `prometheus` / `skywalking` | [getboot-observability/README.md](../getboot-observability/README.md) |
| `getboot-auth` | 认证能力 | 业务项目使用 Sa-Token，想把能力入口收敛到稳定门面时 | `getboot-web` / `getboot-cache` | `getboot.auth.satoken.*` | `satoken` | [getboot-auth/README.md](../getboot-auth/README.md) |
| `getboot-limiter` | 分布式限流 | 需要在业务方法上直接声明限流规则时 | `getboot-coordination` | `getboot.limiter.*` + `getboot.coordination.redisson.*` | `slidingwindow.redisson` / `tokenbucket.redisson` / `leakybucket.redisson` | [getboot-limiter/README.md](../getboot-limiter/README.md) |
| `getboot-lock` | 分布式锁 | 需要声明式锁注解和默认锁键解析策略时 | `getboot-coordination` | `getboot.lock.*` + `getboot.coordination.redisson.*` / `getboot.coordination.zookeeper.*` | `redis.redisson` / `database.jdbc` / `zookeeper.curator` | [getboot-lock/README.md](../getboot-lock/README.md) |
| `getboot-idempotency` | 幂等去重 | 需要声明式幂等键、重复请求结果复用、下单/支付/回调防重时 | `getboot-cache` | `getboot.idempotency.*` | `redis` | [getboot-idempotency/README.md](../getboot-idempotency/README.md) |
| `getboot-governance` | 流量治理 | 需要 Sentinel 接入抽象和统一配置前缀时 | `getboot-http-client` | `getboot.governance.*` | `sentinel` | [getboot-governance/README.md](../getboot-governance/README.md) |
| `getboot-transaction` | 分布式事务 | 需要 Seata，且想把配置和兼容保护收口到模块内时 | `getboot-database` | `getboot.transaction.*` | `seata` | [getboot-transaction/README.md](../getboot-transaction/README.md) |
| `getboot-webhook` | 回调安全编排 | 需要统一处理回调验签、限流和幂等防重时 | `getboot-idempotency` / `getboot-limiter` | `getboot.webhook.security.*` | `servlet` | [getboot-webhook/README.md](../getboot-webhook/README.md) |
| `getboot-http-client` | 出站 HTTP 客户端 | 需要 Feign、WebClient、RestTemplate 出站透传请求头和 `traceId` 时 | `getboot-observability` | `getboot.http-client.*` | `headers.common` / `feign` / `webclient` / `resttemplate` | [getboot-http-client/README.md](../getboot-http-client/README.md) |
| `getboot-rpc` | 远程调用 | 需要 Dubbo Trace 透传、请求签名校验、序列化安全时 | `getboot-observability` | `getboot.rpc.trace.*` / `getboot.rpc.security.*` / `getboot.rpc.dubbo.*` | `dubbo` | [getboot-rpc/README.md](../getboot-rpc/README.md) |
| `getboot-mq` | 消息能力 | 需要 RocketMQ / Kafka 统一生产入口、Trace 透传，或 RocketMQ 事务消息路由时 | `getboot-observability` | `getboot.mq.*` | `rocketmq` / `kafka` | [getboot-mq/README.md](../getboot-mq/README.md) |
| `getboot-job` | 调度能力 | 需要 XXL-JOB 执行器自动装配和管理端客户端时 | 无强制配套模块 | `getboot.job.*` | `xxl` | [getboot-job/README.md](../getboot-job/README.md) |
| `getboot-wechat` | 微信生态接入 | 需要小程序或服务号 SDK 接入时 | `getboot-cache` 可选，用于 Redis token 存储 | `getboot.wechat.*` | `miniapp` / `officialaccount` | [getboot-wechat/README.md](../getboot-wechat/README.md) |
| `getboot-payment` | 支付能力 | 需要支付宝或微信支付主链路、渠道增强能力时 | `getboot-wechat` 不是前置，只有你同时要微信生态 SDK 才再引 | `getboot.payment.*` | `alipay` / `wechatpay` | [getboot-payment/README.md](../getboot-payment/README.md) |

## 4. SPI 查找规则

扩展点的查找规则固定为：

- 模块通用扩展点：优先看 `spi`
- 技术栈专属扩展点：优先看 `spi.<tech>`
- `infrastructure` 不再作为扩展点主入口

如果某个模块 README 没有写扩展点，优先确认两件事：

- 这个模块当前是否真的暴露了稳定 SPI
- 这项能力是不是仍然只允许通过覆盖标准 Spring Bean 定制

## 5. 已明确的后续扩展方向

- 新模块规划
  统一收敛在 [`docs/MODULE_ROADMAP.md`](./MODULE_ROADMAP.md)
- 通用组件储备评估
  统一收敛在 [`docs/COMMON_CAPABILITY_ASSESSMENT.md`](./COMMON_CAPABILITY_ASSESSMENT.md)
- `Seata + ShardingSphere` 组合说明
  统一收敛在 [`docs/SEATA_SHARDING_COMPATIBILITY.md`](./SEATA_SHARDING_COMPATIBILITY.md)

更细仓库级尾项看 [`docs/TODO.md`](./TODO.md)。
