# getboot

如果你的团队已经做到第 `5` 个、第 `20` 个 Spring Boot 服务，还在重复这些动作：

- 重新拼父 `pom`，重新对 Spring Boot、Spring Cloud、Redis、Dubbo、RocketMQ、Kafka、XXL-JOB、支付 SDK 的版本组合
- 重新补统一响应、统一异常、`traceId`、默认 `RedisTemplate`、分布式锁、限流、Webhook 验签、请求头透传
- 每个项目都各接一版 MQ、RPC、支付、缓存，最后配置前缀、消息模型、异常语义和默认约定全不一样
- 新人接手先看旧项目、问老人、复制代码，才能知道一个服务到底该怎么接基础能力

那 `getboot` 要解决的，就是这件事。

`getboot` 不是后台脚手架，也不是“把所有技术栈堆到一起”的演示仓库。它是一组按能力拆分的 Spring Boot starter，用来把那些跨项目反复出现、但又必须长期维护的基础能力，稳定地收口到公共层。

一句话说清：

- 你想解决“公共能力怎么长期复用”，继续看
- 你想解决“后台系统今天怎么一键生成”，这个仓库不是主解

## 30 秒判断

### 适合继续看

- 你们已经不止一个 Spring Boot 服务，而且新服务还在重复补公共层
- 你们不想被重脚手架绑死，但也不接受每个项目都自己补 Trace、缓存、锁、MQ、RPC、支付接入
- 你们希望业务项目拿到的是稳定能力入口，而不是直接耦合底层 SDK
- 你们希望新人能顺着文档独立完成接入，而不是只能照着历史项目抄

### 不适合继续看

- 你要的是菜单、权限、代码生成器、管理页面和默认表结构
- 你只做一次性 demo、课程练手或短期项目，不打算维护公共层
- 你需要的是完整后台系统模板，而不是一组可长期复用的基础能力模块

## 用了之后，今天立刻少做什么

- 不再自己找版本号、试依赖组合、反复验证中间件能不能一起工作
- 不再每个项目都重写统一响应、异常、`traceId`、缓存、锁、客户端透传、WebSocket 会话管理、支付接入
- 不再让业务代码直接耦合底层 SDK、自动配置细节和某一种实现
- 不再把跨团队公共规范散落在业务仓库里
- 不再靠口口相传带新人，模块 README 直接给出稳定接入路径

## 它到底是什么

`getboot` 对外只做三件事：

1. 提供统一父 `pom`
   先把版本组合稳定下来，外部业务项目不需要再自己对一遍基础依赖
2. 提供一组按能力拆分的 `getboot-*` 模块
   按需引入，不强制全家桶
3. 提供稳定的能力边界
   业务优先依赖 `api` / `spi`，实现细节继续留在 `support` / `infrastructure`

这意味着：

- 仓库定位是“能力仓库”，不是后台脚手架
- 模块名表达能力，不表达技术栈
- 外部项目按需引模块，不会被整体结构反向塑形
- 后续新增实现时，优先扩 `infrastructure` 子树，不推翻能力层语义

## 它统一了什么

- 统一版本组合：先在父 `pom` 收住依赖版本，不让业务项目自己试错
- 统一配置前缀：模块级配置统一收敛在 `getboot.*`
- 统一能力边界：外部优先依赖 `api` / `spi`，不把 `support` / `infrastructure` 当对外承诺
- 统一横切语义：`traceId`、Header 透传、异常收口、默认 Bean、环境桥接优先在模块层统一
- 统一接入路径：先看模块地图，再看模块 README，不靠历史项目和口头传承

## 5 分钟接入

### 1. 业务项目先继承父 `pom`

```xml
<parent>
    <groupId>com.dt</groupId>
    <artifactId>getboot-spring-boot-starter-parent</artifactId>
    <version>1.0.0</version>
</parent>
```

这样做的目的只有一个：统一版本管理，业务项目不需要自己再去拼 Spring Boot、Spring Cloud、Redis、Dubbo、RocketMQ、Kafka、XXL-JOB、支付宝、微信支付这些依赖版本。

### 2. 从一个最小组合开始

第一次接入，最稳的起步方式通常是：

- `getboot-web`
- `getboot-observability`
- `getboot-http-client`
- `getboot-cache`
- `getboot-coordination`
- `getboot-lock`

```xml
<dependencies>
    <dependency>
        <groupId>com.dt</groupId>
        <artifactId>getboot-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.dt</groupId>
        <artifactId>getboot-observability</artifactId>
    </dependency>
    <dependency>
        <groupId>com.dt</groupId>
        <artifactId>getboot-http-client</artifactId>
    </dependency>
    <dependency>
        <groupId>com.dt</groupId>
        <artifactId>getboot-cache</artifactId>
    </dependency>
    <dependency>
        <groupId>com.dt</groupId>
        <artifactId>getboot-coordination</artifactId>
    </dependency>
    <dependency>
        <groupId>com.dt</groupId>
        <artifactId>getboot-lock</artifactId>
    </dependency>
</dependencies>
```

### 3. 再按模块 README 填配置

```yaml
spring:
  application:
    name: demo-service

getboot:
  observability:
    trace:
      enabled: true
      header-name: X-Trace-Id
      response-header-enabled: true
    metrics:
      enabled: true
      common-tags:
        app: demo-service
        env: local
    prometheus:
      enabled: true
  http-client:
    openfeign:
      trace:
        enabled: true
        header-name: X-Trace-Id
    resttemplate:
      trace:
        enabled: true
        header-name: X-Trace-Id
  cache:
    redis:
      host: 127.0.0.1
      port: 6379
      database: 0
  coordination:
    redisson:
      file: classpath:redisson/redisson.yaml
  lock:
    enabled: true
    redis:
      enabled: true
      key-prefix: demo_lock

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
```

`classpath:redisson/redisson.yaml` 示例：

```yaml
singleServerConfig:
  address: "redis://127.0.0.1:6379"
  database: 0
threads: 8
nettyThreads: 16
codec: !<org.redisson.codec.JsonJacksonCodec> {}
transportMode: "NIO"
```

编译基线是 `Java 17+`。如果你只想先判断该引哪些模块，先看 [`docs/MODULE_MAP.md`](./docs/MODULE_MAP.md)。

## 常见选型捷径

| 场景 | 建议模块 | 说明 |
| --- | --- | --- |
| 普通 HTTP API 服务 | `getboot-web` + `getboot-observability` + `getboot-http-client` | 先把统一响应、Trace 和出站透传打通 |
| Redis 缓存 | `getboot-cache` | 统一走 `getboot.cache.redis.*` |
| 分布式协调底座 | `getboot-coordination` | 锁、限流、Webhook 通常先依赖它 |
| 分布式锁 | `getboot-coordination` + `getboot-lock` | 能力层和基础设施层分开治理 |
| 分布式限流 | `getboot-coordination` + `getboot-limiter` | 注解式限流入口直接落在公共层 |
| 幂等去重 | `getboot-idempotency` | 适合下单、支付、回调防重 |
| Webhook 安全编排 | `getboot-webhook` | 复用限流和幂等能力，不再散落在业务项目里 |
| 对象存储 | `getboot-storage` | 统一上传、下载、删除、预签名 URL |
| 短信 / 邮件 | `getboot-sms` / `getboot-mail` | 通知类基础能力统一走门面 |
| AI / 搜索 | `getboot-ai` / `getboot-search` | 检索增强和模型调用收口到稳定入口 |
| RocketMQ / Kafka / MQTT | `getboot-mq` | 统一生产入口、Trace 透传和配置桥接 |
| Dubbo | `getboot-rpc` | Trace 透传、认证和序列化安全收口 |
| WebSocket 推送 | `getboot-websocket` | 统一会话注册、按用户 / 会话推送 |
| 分布式事务 | `getboot-transaction` + `getboot-database` | 重点看 Seata 与分库分表组合边界 |
| 微信生态 / 支付 | `getboot-wechat` / `getboot-payment` | 微信生态接入和支付主链路分开治理 |

完整矩阵看 [`docs/MODULE_MAP.md`](./docs/MODULE_MAP.md)。

## 仓库里有什么

### Foundation

- [`getboot-support`](./getboot-support/README.md)：通用支撑、环境别名、Trace 上下文传播
- [`getboot-exception`](./getboot-exception/README.md)：错误码与业务异常
- [`getboot-web`](./getboot-web/README.md)：统一响应模型与全局异常处理

### Infrastructure Capability

- [`getboot-cache`](./getboot-cache/README.md)：Redis 接入与缓存门面
- [`getboot-coordination`](./getboot-coordination/README.md)：Redisson / Curator 基础设施
- [`getboot-database`](./getboot-database/README.md)：数据访问增强、MongoDB 启动校验、ShardingSphere
- [`getboot-storage`](./getboot-storage/README.md)：对象存储
- [`getboot-sms`](./getboot-sms/README.md)：短信发送
- [`getboot-mail`](./getboot-mail/README.md)：邮件发送
- [`getboot-ai`](./getboot-ai/README.md)：Chat / Embedding / Rerank
- [`getboot-search`](./getboot-search/README.md)：索引写入与基础查询
- [`getboot-observability`](./getboot-observability/README.md)：Trace、指标、Prometheus、SkyWalking

### Cross-Cutting Capability

- [`getboot-auth`](./getboot-auth/README.md)：Sa-Token 鉴权能力
- [`getboot-limiter`](./getboot-limiter/README.md)：分布式限流
- [`getboot-lock`](./getboot-lock/README.md)：分布式锁
- [`getboot-idempotency`](./getboot-idempotency/README.md)：幂等去重与结果复用
- [`getboot-governance`](./getboot-governance/README.md)：Sentinel 流量治理
- [`getboot-transaction`](./getboot-transaction/README.md)：Seata 分布式事务
- [`getboot-webhook`](./getboot-webhook/README.md)：Webhook / 回调安全编排

### Communication

- [`getboot-http-client`](./getboot-http-client/README.md)：OpenFeign、WebClient、RestTemplate 出站透传
- [`getboot-rpc`](./getboot-rpc/README.md)：Dubbo 调用增强
- [`getboot-mq`](./getboot-mq/README.md)：RocketMQ / Kafka / MQTT 消息能力
- [`getboot-websocket`](./getboot-websocket/README.md)：WebSocket 长连接与推送
- [`getboot-job`](./getboot-job/README.md)：XXL-JOB 执行器与客户端

### Ecosystem

- [`getboot-wechat`](./getboot-wechat/README.md)：微信小程序 / 服务号接入
- [`getboot-payment`](./getboot-payment/README.md)：支付宝 / 微信支付主链路

## 为什么不是若依这类脚手架

它们解决的不是同一类问题。

| 对比项 | 若依这类脚手架 | `getboot` |
| --- | --- | --- |
| 目标 | 先搭出一个可用后台系统 | 先收口可复用的基础能力 |
| 交付物 | 菜单、权限、代码生成、管理页面骨架 | 父 `pom` + 一组按能力拆分的 starter |
| 对业务项目的影响 | 很容易从第一天开始被脚手架结构反向塑形 | 业务项目按需引模块，不强制全量接入 |
| 长期价值 | 更偏后台模板 | 更偏跨项目公共层资产 |

所以这里的取舍很明确：

- 不提供完整后台系统模板
- 不预设你的业务表结构、菜单模型、权限模型
- 不把代码生成器当成仓库主价值
- 只把那些跨项目反复出现、又值得长期沉淀的基础能力拆出来做成模块

## 文档怎么读

推荐阅读顺序：

1. 先看当前这份 `README.md`
2. 再看 [`docs/MODULE_MAP.md`](./docs/MODULE_MAP.md)，判断应该引哪些模块
3. 然后看目标模块自己的 `README.md`，按模块入口完成接入
4. 如果你要开发或维护模块，再看 [`DEVELOPMENT.md`](./DEVELOPMENT.md)
5. 如果你要补路线图或判断新能力边界，看 [`docs/MODULE_ROADMAP.md`](./docs/MODULE_ROADMAP.md)
6. 如果你要判断某类能力值不值得单拆模块，看 [`docs/COMMON_CAPABILITY_ASSESSMENT.md`](./docs/COMMON_CAPABILITY_ASSESSMENT.md)
7. 如果你要动目录分层规则，看 [`docs/DDD_PACKAGE_RULES.md`](./docs/DDD_PACKAGE_RULES.md)
8. 如果你要碰 `Seata + ShardingSphere` 组合，看 [`docs/SEATA_SHARDING_COMPATIBILITY.md`](./docs/SEATA_SHARDING_COMPATIBILITY.md)

如果你只记一条接入路径，记这个就够了：

`继承父 pom -> 按场景引模块 -> 按模块 README 填配置`
