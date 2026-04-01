# getboot

`getboot` 是一组面向 Spring Boot 3 的基础能力模块。它不是再造一套大而全的后台脚手架，而是把业务服务里最常重复、最容易散落、又最难持续维护的一批基础能力收成可复用、可按需引入的 starter。

这个仓库的使用方式很简单：`继承父 pom`，`按场景引模块`，`按模块 README 接入`。

很多公司的现实并不是“先有一个基础架构团队把规范铺好”，而是各业务团队一边写业务，一边自己定统一响应、异常模型、缓存接入、MQ/RPC 约定和中间件装配方式。这个仓库更想解决的，就是这种没有统一底座维护者时的组织级重复建设。

## 这个仓库在解决什么问题

如果把痛点说得更直接一点，问题其实不只是“大家不想重复搭基架”，而是很多公司根本没有专门的基础架构团队，最后这些基础规范和中间件接入方式只能散落到各业务团队手里。

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

## 你能直接得到什么

这套仓库想提供的价值，不是“帮你把系统生成出来”，而是“把那批反复造、又必须造对的基础能力先收好”：

- 一个统一父 `pom`，优先解决“不想自己找版本号”和“不想自己反复验依赖组合”的问题
- 一套已经整理好的基础能力骨架，优先解决“不想每个项目都从零开打”的问题
- 一组按能力拆分的 `getboot-*` 模块，业务项目按需引入，不需要被全家桶绑定
- 把统一响应、异常、Trace、缓存、锁、客户端透传、RPC/MQ/支付接入这类跨团队公共约定收口到模块层，而不是让每个团队各写一版
- 尽量稳定的 `api` / `spi` 入口，避免业务代码直接耦合到底层 SDK、自动配置细节或某一种实现
- 模块级 README，把“为什么要引、怎么引、依赖什么、默认给你什么、还能怎么扩展”写成一致的接入方式，让新人和初级开发也能顺着文档接进去

简单说，它更像一个“业务服务基础能力仓库”，而不是“后台系统模板”。

## 适合什么团队

- 已经有多个 Spring Boot 服务，想把公共能力从业务仓库里抽出来统一维护
- 公司没有专门基础架构团队，或者基础架构团队无法覆盖所有业务线，需要把公共规范沉到仓库层统一维护
- 不想上重脚手架，但也不想每个项目都重新补 Trace、缓存、锁、Webhook、支付、RPC、MQ
- 希望业务项目拿到的是稳定能力入口，而不是一堆零散 SDK 接入细节
- 希望新人和初级开发也能按统一 README 和配置路径完成接入，而不是靠口口相传

如果你更想要的是下面这些东西，这个仓库就不是主解：

- 一键生成完整后台管理系统
- 自带业务表结构、权限系统、代码生成器
- 面向单次 demo 或一次性项目的快速搭建模板

## 为什么不做若依这类脚手架

若依这类脚手架有自己的适用场景，它解决的是“尽快搭出一个可用的后台管理系统”。如果你的目标就是做一个带菜单、权限、代码生成、管理页面的单体后台，它当然能跑得很快。

但它不是我们想解决的问题，甚至在很多团队里，它会把另一类问题越用越重：

- 它优先交付的是后台系统骨架，不是可复用的基础能力模块
- 它往往自带较强的目录约定、权限模型、表结构和生成器心智，业务项目很容易从第一天开始就被脚手架反向塑形
- 一旦服务形态从“后台管理页”扩展到 API 服务、Webhook、MQ 消费者、RPC 服务、支付链路、微信生态，很多脚手架自带的东西就开始变成包袱，而不是资产
- 你想复用的通常不是“菜单生成那一套”，而是 Trace、缓存、锁、限流、异常收口、客户端透传、支付接入这些基础能力；这类能力跟后台 UI 模板根本不是一回事
- 后面真要升级 Spring Boot、调整中间件版本、替换实现、拆服务时，团队经常发现自己维护的不是业务，也不是公共能力，而是在继续给脚手架续命

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
| 数据访问增强 | `getboot-database` | 重点看数据源预热、MyBatis-Plus、ShardingSphere |
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
  数据源预热、MyBatis-Plus、ShardingSphere
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
  OpenFeign、WebClient、RestTemplate 的 Trace 透传增强
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
- [`docs/DDD_PACKAGE_RULES.md`](./docs/DDD_PACKAGE_RULES.md)
  包结构规则
- [`docs/TODO.md`](./docs/TODO.md)
  路线图

支付模块的专项文档继续放在模块内：

- [`getboot-payment/docs/ALIPAY_MINIMAL_INTEGRATION.md`](./getboot-payment/docs/ALIPAY_MINIMAL_INTEGRATION.md)
- [`getboot-payment/docs/WECHAT_PAY_MINIMAL_INTEGRATION.md`](./getboot-payment/docs/WECHAT_PAY_MINIMAL_INTEGRATION.md)
- [`getboot-payment/docs/ALIPAY_CAPABILITY_MATRIX.md`](./getboot-payment/docs/ALIPAY_CAPABILITY_MATRIX.md)
- [`getboot-payment/docs/WECHAT_PAY_CAPABILITY_MATRIX.md`](./getboot-payment/docs/WECHAT_PAY_CAPABILITY_MATRIX.md)

## 当前路线

后续会继续围绕“新项目常见基础能力”推进，优先关注：

- `getboot-database` 评估 `MongoDB` 方向
- `getboot-ai`、`getboot-storage`、`getboot-search`、`getboot-sms` 等新能力规划

具体路线继续收敛在 [`docs/TODO.md`](./docs/TODO.md)。
