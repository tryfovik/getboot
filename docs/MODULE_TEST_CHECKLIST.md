# 模块测试治理清单

这份清单只跟踪模块级测试治理，不替代功能路线图和风格治理清单。

基线盘点日期：`2026-04-03`

## 目标

- 每个 `getboot-*` 模块至少完成一次模块级 `mvn "-Dmaven.repo.local=D:\project\getboot\.m2-repo" -pl <module> -am test`
- 每个模块至少覆盖配置绑定 / 别名桥接、自动配置、核心 support/operator、关键异常分支中的主要路径
- 每完成一个模块，就同步做三件事：补测试、执行 Maven 验证、回填本清单打钩
- 一个模块确认没问题后单独提交，不把多个模块的补测混在一个 commit 里

## 执行顺序

1. 先清零当前完全没有测试的模块
2. 再补只有 `1-2` 个测试的薄覆盖模块
3. 最后回头加厚已有 `3+` 个测试但还没在本轮逐个验证的模块

## 当前基线

- `0` 个测试：`getboot-auth`、`getboot-cache`、`getboot-coordination`、`getboot-wechat`
- `1-2` 个测试：`getboot-database`、`getboot-exception`、`getboot-governance`、`getboot-http-client`、`getboot-idempotency`、`getboot-job`、`getboot-lock`、`getboot-mq`、`getboot-rpc`、`getboot-web`、`getboot-webhook`
- `3+` 个测试：`getboot-ai`、`getboot-limiter`、`getboot-mail`、`getboot-observability`、`getboot-payment`、`getboot-search`、`getboot-sms`、`getboot-storage`、`getboot-support`、`getboot-transaction`

## 模块清单

### 已完成确认

- [x] `getboot-mail`：当前 `4` 个测试；已补齐 SMTP 发送、模板渲染、配置绑定与自动配置验证，`2026-04-03` 已执行模块级 Maven `test`
- [x] `getboot-cache`：当前 `3` 个测试；已补齐 Redis 别名桥接、自动配置与 `RedisCacheOperator` 验证，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-auth`：当前 `3` 个测试；已补齐 Sa-Token 别名桥接、自动配置与 `SaTokenCurrentUserAccessor` 主要路径验证，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-coordination`：当前 `3` 个测试；已补齐 Redisson 别名桥接、Curator 配置绑定与 `CuratorZookeeperAutoConfiguration` 验证，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-wechat`：当前 `3` 个测试；已补齐 `WechatProperties` 绑定、服务工厂初始化与 `WechatAutoConfiguration` 条件验证，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-exception`：当前 `2` 个测试；已补齐错误码边界与 `BusinessException` 构造分支验证，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-web`：当前 `4` 个测试；已补齐统一响应边界、全局异常处理与自动配置验证，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-database`：当前 `3` 个测试；已补齐数据库别名桥接、数据源自动配置边界与 MongoDB 初始化验证，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-http-client`：当前 `3` 个测试；已补齐三类 HTTP 客户端自动配置、单客户端关闭与自定义 Bean 回退验证，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-mq`：当前 `4` 个测试；已补齐 RocketMQ/Kafka 配置桥接，并复核 Kafka 发送与监听 Trace 关键路径，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-rpc`：当前 `3` 个测试；已补齐 Dubbo 配置桥接、RPC 安全自动配置与错误配置失败边界，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-job`：当前 `2` 个测试；已补齐 XXL-JOB 自动配置、定制器生效与未初始化客户端报错边界，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-lock`：当前 `4` 个测试；已补齐 SpEL 锁键解析、Redis 自动配置与多实现锁边界验证，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-idempotency`：当前 `3` 个测试；已补齐自动配置、配置绑定与全局开关边界，并修正切面装配顺序，`2026-04-04` 已执行模块级 Maven `test`
- [x] `getboot-governance`：当前 `3` 个测试；已补齐 Sentinel 配置绑定、别名桥接与自动配置边界，`2026-04-04` 已执行模块级 Maven `test`

### P0：先清零测试空白

### P1：薄覆盖模块

- [x] `getboot-webhook`：当前 `2` 个测试；已补齐自动配置、凭证绑定与验签/限流/幂等处理器条件装配边界，`2026-04-04` 已执行模块级 Maven `test`

### P2：已有一定测试，但仍需本轮逐个确认

- [ ] `getboot-support`：当前 `3` 个测试；确认公共 support、上下文与环境桥接没有漏测区域
- [ ] `getboot-storage`：当前 `3` 个测试；确认上传、预签名、自动配置边界是否完整
- [ ] `getboot-sms`：当前 `3` 个测试；确认模板变量、供应商适配、自动配置是否完整
- [ ] `getboot-search`：当前 `4` 个测试；确认查询条件、分页排序和自动配置边界是否完整
- [ ] `getboot-ai`：当前 `5` 个测试；确认 properties、模板渲染、OpenAI 适配和门面是否完整
- [ ] `getboot-limiter`：当前 `6` 个测试；确认自动配置、配置绑定和三类限流实现是否完整
- [ ] `getboot-observability`：当前 `5` 个测试；确认 trace、prometheus、webflux、自动配置边界是否完整
- [ ] `getboot-transaction`：当前 `3` 个测试；确认 Seata 别名桥接与自动配置边界是否完整
- [ ] `getboot-payment`：当前 `18` 个测试；仍需补模块级 Maven 验证并确认支付主链路没有漏测区域

## 验收口径

- 打钩不表示“写了测试文件”，而表示该模块已经完成补测、执行模块级 Maven `test`、并确认没有明显漏掉核心路径
- 如果模块新增了配置前缀、默认 Bean、别名桥接或 SPI 扩展点，测试里必须覆盖这些对外承诺
- 如果模块强依赖第三方 SDK，优先补门面、适配层和自动配置测试，不在仓库里引入重型外部集成环境
