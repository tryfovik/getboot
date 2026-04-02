# getboot

`getboot` 不是“再做一个 Spring Boot 脚手架”，而是专门收掉业务团队反复造底座这件事：统一响应、异常、`traceId`、Redis 接入、分布式锁、限流、HTTP/RPC/MQ 透传、支付接入、版本组合和自动装配。

这个仓库的使用方式很简单：`继承父 pom`，`按场景引模块`，`按模块 README 接入`。

如果你的团队每起一个新服务，都还在重复下面这些动作，这个仓库就是给这种场景准备的：

- 重新拼一套父 `pom`，再重新核 Spring Boot、Spring Cloud、Redis、Dubbo、RocketMQ、Kafka、XXL-JOB、支付宝、微信支付这批依赖到底能不能一起工作
- 重新写统一响应、异常包装、`traceId` 透传、默认 `RedisTemplate`、分布式锁、限流、Webhook 验签、客户端请求头透传
- 不同团队各自接 MQ、RPC、支付、缓存，最后每个项目配置前缀、返回模型、异常语义和默认约定都不一样
- 新人接手时，先要读历史项目、问人、照着抄，才能知道一个服务到底该怎么接基础能力

它解决的不是“怎么更快起一个 demo”，而是“为什么第 5 个、第 20 个服务还在重复修同一批基础设施问题”。

## 如果不收口，这些痛点会一直重复

很多公司的真实问题不是“大家懒得搭基架”，而是根本没有稳定的公共层维护入口。结果不是不做，而是每个业务团队都各做一版。

如果团队只做一个 demo，直接从裸 Spring Boot 起步通常没什么问题。真正麻烦的是第二个、第三个、第五个服务开始之后，大家重复建设的往往已经不是业务，而是基础设施装配和团队规范本身：

- 每起一个新服务，都要重新搭一遍基础项目骨架，再重新对 Spring Boot、Spring Cloud、Redis、Dubbo、RocketMQ、Kafka、XXL-JOB、支付宝、微信支付这批依赖版本，还要反复确认它们能不能一起工作
- 没有统一基础设施维护者时，统一响应、统一异常、TraceId 透传、默认 `RedisTemplate`、Redisson、分布式锁、限流、Webhook 验签这类能力，总是在业务仓库里复制、删改、再复制
- MQ、缓存、RPC、支付这类中间件经常由不同团队分别接入，最后连消息模型、异常包装、Header 透传、配置前缀和默认约定都不一样
- HTTP、RPC、MQ、支付、微信生态这些接入虽然业务目标不同，但大量样板代码、环境桥接、自动配置和默认 Bean 设计其实高度重复
- 新人和初级开发接手时，面对的往往不是一条清晰接入路径，而是多套历史写法、多个团队口头约定和一堆要先摸清楚的中间件细节

最后常见的结果就是：

- 新项目启动不慢在业务，而是慢在重新拼底座
- 相同中间件在不同团队手里长出不同规范，跨项目协作和治理越来越重
- 基础能力沉淀不进公共层，只能继续散落在业务仓库
- 新人上手速度依赖“有人带”，而不是依赖稳定入口和可复制文档
- 升级和治理没有统一入口，团队明明已经踩过坑，经验却很难稳定复用

## 用了之后，立刻少做什么

这套仓库想提供的不是“帮你生成一个后台系统”，而是“把那批反复造、又必须造对的基础能力先收好”，让团队今天就能少掉一批重复劳动：

- 不再自己找版本号、试依赖组合、反复验证中间件能不能共存
- 不再每个项目都重写统一响应、异常、`traceId`、缓存、锁、客户端透传、支付接入这些公共层代码
- 不再一上来就被“全家桶脚手架”绑死，模块按能力拆开，按需引入
- 不再让业务代码直接耦合底层 SDK、自动配置细节和某一种实现
- 不再靠口口相传带新人，模块 README 和文档直接给出稳定接入路径
- 不再把跨团队公共约定散落在业务仓库里，而是统一收口在公共模块

简单说，它更像一个“业务服务基础能力仓库”，而不是“后台系统模板”。

## 你该不该继续看

继续看下去，通常说明你命中了下面至少两条：

- 你们已经不止一个 Spring Boot 服务，而且每起一个新服务都还在重新补公共层
- 你们没有稳定覆盖所有业务线的基础架构团队，或者有团队但公共规范还是落不下来
- 你们不想被重脚手架绑死，但又不接受每个项目都自己补 Trace、缓存、锁、支付、MQ、RPC
- 你们希望业务项目拿到的是稳定能力入口，而不是一堆底层 SDK 的接入细节
- 你们希望新人也能顺着文档独立完成接入，而不是只能找老人“照着上个项目抄”

如果你更接近下面这些场景，这个仓库就不是主解：

- 你只想一键生成一个完整后台管理系统
- 你需要的是菜单、权限、代码生成器、管理页面和默认表结构
- 你只做单次 demo、课程练手或一次性项目，不打算长期维护公共层

一句话判断：

- 你想解决“公共能力怎么长期复用”，继续看
- 你想解决“后台系统今天怎么立刻搭出来”，可以直接关掉

## 接入前后对比

| 场景 | 不用 `getboot` 时常见状态 | 用 `getboot` 后的目标状态 |
| --- | --- | --- |
| 起新服务 | 重新拼父 `pom`、重新验依赖组合 | 继承统一父 `pom`，版本组合直接复用 |
| HTTP 服务公共层 | 每个项目各写一版统一响应、异常、`traceId` | 统一走稳定模块和默认 Bean |
| Redis / 锁 / 限流 | 先接 Redis，再自己补 Redisson、锁键、限流规则 | 按模块引入，配置前缀和能力边界固定 |
| HTTP / RPC / MQ 透传 | 每个团队自己定 Header、异常语义、上下文传播 | 模块层统一透传与桥接策略 |
| 支付 / 微信生态 | 业务代码直接耦合 SDK，项目之间接法不一样 | 支付和生态能力收口到稳定门面 |
| 新人接手 | 先看历史项目、找人问、复制旧代码 | 先看根 README，再看模块 README，按路径接入 |

## 为什么不是若依这类脚手架

若依这类脚手架有自己的适用场景，它解决的是“尽快搭出一个可用的后台管理系统”。如果你的目标就是做一个带菜单、权限、代码生成、管理页面的单体后台，它当然能跑得很快。

但它和 `getboot` 解决的不是同一类问题：

| 对比项 | 若依这类脚手架 | `getboot` |
| --- | --- | --- |
| 目标 | 先搭出一个可用后台系统 | 先收口可复用的基础能力 |
| 交付物 | 菜单、权限、代码生成、管理页骨架 | 父 `pom` + 按能力拆分的 starter |
| 对业务的影响 | 很容易从第一天开始被脚手架结构反向塑形 | 业务项目按需引模块，不强制全量接入 |
| 真正复用的资产 | 更多是后台系统模板 | 更多是 Trace、缓存、锁、限流、RPC/MQ/支付接入这些公共层能力 |
| 后续演进成本 | 常常演变成给脚手架续命 | 重点是让公共能力沉到稳定模块里 |

所以这里的选择很明确：

- 不提供完整后台系统模板
- 不预设你的业务表结构、菜单模型、权限模型
- 不把代码生成器当成仓库主价值
- 只把那些跨项目反复出现、又值得长期沉淀的基础能力拆出来做成模块

## 仓库定位

- 它是能力仓库，不是后台管理脚手架
- 它提供统一父 `pom` 和一组 `getboot-*` 模块
- 外部业务项目按需引入，不强制全量接入
- 能力层优先保持稳定，实现层继续演进

## 5 分钟接入

### 1. 业务项目先继承父 `pom`

```xml
<parent>
    <groupId>com.dt</groupId>
    <artifactId>getboot-spring-boot-starter-parent</artifactId>
    <version>1.0.0</version>
</parent>
```

这样做的目的只有一个：统一版本管理，业务项目不需要自己再对一遍 Spring Boot、Spring Cloud、Redis、Dubbo、RocketMQ、Kafka、XXL-JOB、WeChat Pay、支付宝这些依赖版本。

### 2. 再按场景引入模块

例如一个典型 HTTP 业务服务，通常会先从下面这些模块开始：

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
</dependencies>
```

如果还需要 Redis、锁、限流、Webhook、支付、Dubbo、MQ，再继续补对应模块，不需要一开始就把整仓全引进来。

### 3. 按模块 README 填配置

每个模块 README 都应该回答同一组问题：

- 这个模块解决什么问题
- 业务项目怎么引入
- 前置条件和配套模块是什么
- 配置项和默认 Bean 是什么
- 扩展点在哪里

如果你要快速判断先看哪个模块，先看 [`docs/MODULE_MAP.md`](./docs/MODULE_MAP.md)。

如果是新人、初级开发，或者第一次接这个仓库，建议就按这条路径走：先看 `MODULE_MAP` 判断模块，再看目标模块 README 填配置，不需要一上来先读源码，也不需要先把每个中间件细节全部摸透。

## 一个最小业务服务组合

下面这组组合适合“普通 Spring MVC 服务 + Trace + Redis + 分布式锁”，也适合作为第一次接这个仓库时的起步路径：

- `getboot-web`
- `getboot-observability`
- `getboot-http-client`
- `getboot-cache`
- `getboot-coordination`
- `getboot-lock`

Maven 依赖：

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

`application.yml` 示例：

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

接入完成后，这套组合通常就能提供：

- `getboot-web` 的统一响应模型和异常收口
- `getboot-observability` 的 `traceId` 建链、日志上下文和指标能力
- `getboot-http-client` 的 Feign / RestTemplate / WebClient 出站透传
- `getboot-cache` 的 `CacheOperator` 与默认 `RedisTemplate`
- `getboot-coordination` 的 Redisson 基础设施
- `getboot-lock` 的声明式分布式锁

## 常见接入场景

| 场景 | 建议模块 | 接入提示 |
| --- | --- | --- |
| 普通 HTTP API 服务 | `getboot-web` + `getboot-observability` + `getboot-http-client` | 先把统一响应、Trace 和出站调用透传打通 |
| Redis 缓存 | `getboot-cache` | 统一走 `getboot.cache.redis.*`，不要把原生 Redis 配置散在各处 |
| 分布式协调基础设施 | `getboot-coordination` | 锁、限流、Webhook 这类能力通常先依赖它 |
| 分布式锁 | `getboot-coordination` + `getboot-lock` | `getboot-lock` 负责能力层，`getboot-coordination` 负责 Redisson / Curator 接入 |
| 分布式限流 | `getboot-coordination` + `getboot-limiter` | 主入口是 `@RateLimit`，注解上直接选择滑动窗口、令牌桶或漏桶 |
| Webhook 安全编排 | `getboot-webhook` | 需要先准备 Redis / Redisson 环境，模块内部会复用缓存、锁、限流能力 |
| Dubbo 服务 | `getboot-rpc` + `getboot-observability` | 重点看 RPC 认证、Trace 透传和序列化安全 |
| RocketMQ / Kafka | `getboot-mq` + `getboot-observability` | 重点看统一生产入口、Trace 透传；RocketMQ 额外支持事务消息路由 |
| 数据访问增强 | `getboot-database` | 重点看数据源预热、MongoDB 启动校验、MyBatis-Plus、ShardingSphere |
| Seata 分布式事务 | `getboot-transaction` + `getboot-database` | 先确认分库分表与事务组合策略 |
| Sa-Token 鉴权 | `getboot-auth` | 统一从 `CurrentUserAccessor` 进入，不要让业务层直接耦合实现 |
| 微信小程序 / 服务号 | `getboot-wechat` | 这是微信生态接入，不等于支付接入 |
| 支付宝 / 微信支付 | `getboot-payment` | 这是支付主链路；如果还要接小程序或服务号，再单独引入 `getboot-wechat` |

## 当前模块清单

### Foundation

- [`getboot-support`](./getboot-support/README.md)
  通用支撑能力，例如 Spring 上下文访问、Trace 上下文传播、环境别名处理基类
- [`getboot-exception`](./getboot-exception/README.md)
  错误码与业务异常
- [`getboot-web`](./getboot-web/README.md)
  统一响应模型与全局异常处理

### Infrastructure Capability

- [`getboot-cache`](./getboot-cache/README.md)
  Redis 接入、默认 `RedisTemplate`、缓存操作门面
- [`getboot-coordination`](./getboot-coordination/README.md)
  Redisson / Curator 基础设施接入
- [`getboot-database`](./getboot-database/README.md)
  数据源预热、MongoDB 启动校验、MyBatis-Plus、ShardingSphere
- [`getboot-observability`](./getboot-observability/README.md)
  Trace、日志上下文、Prometheus、SkyWalking、Reactor 传播

### Cross-Cutting Capability

- [`getboot-auth`](./getboot-auth/README.md)
  Sa-Token 集成增强
- [`getboot-limiter`](./getboot-limiter/README.md)
  分布式限流
- [`getboot-lock`](./getboot-lock/README.md)
  分布式锁
- [`getboot-governance`](./getboot-governance/README.md)
  Sentinel 流量治理抽象
- [`getboot-transaction`](./getboot-transaction/README.md)
  Seata 分布式事务抽象
- [`getboot-webhook`](./getboot-webhook/README.md)
  Webhook / 事件回调安全能力

### Communication

- [`getboot-http-client`](./getboot-http-client/README.md)
  OpenFeign、WebClient、RestTemplate 的出站请求头增强与 Trace 透传
- [`getboot-rpc`](./getboot-rpc/README.md)
  Dubbo 安全增强、Trace 透传、配置适配
- [`getboot-mq`](./getboot-mq/README.md)
  RocketMQ / Kafka 生产、RocketMQ 事务消息、Trace 透传与配置适配
- [`getboot-job`](./getboot-job/README.md)
  XXL-JOB 执行器和管理端客户端

### Ecosystem

- [`getboot-wechat`](./getboot-wechat/README.md)
  小程序和服务号接入能力
- [`getboot-payment`](./getboot-payment/README.md)
  支付主链路、支付宝能力、微信支付能力

## 对外边界

这套仓库对外只承诺两层：

- `api`
  放稳定接口、模型、注解和业务项目应直接依赖的能力入口
- `spi`
  放允许业务方或其他模块注册 Bean 进行扩展的稳定扩展点

除此之外，其他包都视为内部实现：

- `support`
  默认实现、内部 facade、内部 helper，不承诺给外部项目直接依赖
- `infrastructure`
  自动配置、环境适配、SDK 接入、监听器、适配器等实现细节，不承诺给外部项目直接依赖

简单说：

- 业务项目优先依赖 `api` / `spi`
- 不要把 `support` / `infrastructure` 当成稳定承诺层
- 后续版本优先保证 `api` / `spi` 稳定，实现层允许继续演进

## 文档怎么读

根目录只保留两个主入口：

- `README.md`
  面向仓库使用者，回答“为什么要用、怎么接、先看哪里”
- [`DEVELOPMENT.md`](./DEVELOPMENT.md)
  面向仓库维护者，回答“怎么开发、怎么写文档、怎么验收”

`docs/` 只放技术参考和路线图：

- [`docs/MODULE_MAP.md`](./docs/MODULE_MAP.md)
  模块选型、接入顺序、配置前缀、模块文档入口
- [`docs/MODULE_ROADMAP.md`](./docs/MODULE_ROADMAP.md)
  下一批模块的边界规划与落地顺序
- [`docs/COMMON_CAPABILITY_ASSESSMENT.md`](./docs/COMMON_CAPABILITY_ASSESSMENT.md)
  常用通用能力是否值得单拆模块的评估结论
- [`docs/DDD_PACKAGE_RULES.md`](./docs/DDD_PACKAGE_RULES.md)
  包结构规则
- [`docs/SEATA_SHARDING_COMPATIBILITY.md`](./docs/SEATA_SHARDING_COMPATIBILITY.md)
  `Seata + ShardingSphere` 组合约束与配置建议
- [`docs/TODO.md`](./docs/TODO.md)
  路线图

支付模块的专项文档继续放在模块内：

- [`getboot-payment/docs/ALIPAY_MINIMAL_INTEGRATION.md`](./getboot-payment/docs/ALIPAY_MINIMAL_INTEGRATION.md)
- [`getboot-payment/docs/WECHAT_PAY_MINIMAL_INTEGRATION.md`](./getboot-payment/docs/WECHAT_PAY_MINIMAL_INTEGRATION.md)
- [`getboot-payment/docs/ALIPAY_CAPABILITY_MATRIX.md`](./getboot-payment/docs/ALIPAY_CAPABILITY_MATRIX.md)
- [`getboot-payment/docs/WECHAT_PAY_CAPABILITY_MATRIX.md`](./getboot-payment/docs/WECHAT_PAY_CAPABILITY_MATRIX.md)

## 当前路线

后续会继续围绕“新项目常见基础能力”推进，优先关注：

- 现有核心模块优先在各自 README 与模块内文档继续收口，不再把泛化演进项挂在根级 TODO
- 新模块边界与落地顺序，优先看 [`docs/MODULE_ROADMAP.md`](./docs/MODULE_ROADMAP.md)
- 常用能力是否单拆模块，优先看 [`docs/COMMON_CAPABILITY_ASSESSMENT.md`](./docs/COMMON_CAPABILITY_ASSESSMENT.md)
- 仓库级剩余尾项，优先看 [`docs/TODO.md`](./docs/TODO.md)

具体路线继续收敛在 [`docs/TODO.md`](./docs/TODO.md)。
