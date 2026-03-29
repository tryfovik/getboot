# DDD Package Rules

仓库内所有 `getboot-*` 模块统一遵循固定分层目录：

- `api`
- `spi`
- `support`
- `infrastructure`

目标：

- 别人看到目录就知道什么是对外能力，什么是实现细节
- 后续新增技术栈时，只新增 `infrastructure` 子树，不推翻外部接口
- 对外稳定入口始终落在 `api` / `spi`

## 0. 模块分组

从仓库整体设计出发，模块先按职责分组，再在模块内按 `api + spi + support + infrastructure` 分层。

### 0.1 Foundation Modules

仓库底座，不承载某一类中间件能力，而是给其他模块提供共性支撑。

- `getboot-support`
- `getboot-exception`
- `getboot-web`

规则：

- 应保持轻量、稳定、低依赖
- 尽量不反向依赖其他能力模块
- 顶层目录同样要追求一眼可读，不保留历史散装命名
- 优先表达为“稳定模型/工具”与“Spring 接入实现”两层视图

推荐形态：

- `getboot-support`：`api.* + infrastructure.*`
- `getboot-exception`：`api.code + api.exception`
- `getboot-web`：`api.request + api.response + infrastructure.*`

### 0.2 Infrastructure Capability Modules

提供通用基础设施能力，是更上层能力模块的底盘。

- `getboot-coordination`
- `getboot-cache`
- `getboot-database`
- `getboot-observability`

规则：

- 负责统一接入基础设施组件
- 对上暴露稳定能力接口，不泄露具体实现细节

### 0.3 Cross-Cutting Capability Modules

建立在基础设施能力之上，对业务开发提供横切能力。

- `getboot-auth`
- `getboot-limiter`
- `getboot-lock`
- `getboot-governance`
- `getboot-transaction`
- `getboot-webhook`

规则：

- 对业务直接可用
- 优先复用 Foundation 与 Infrastructure Capability 模块

### 0.4 Communication Modules

负责应用与外部系统之间的通信与调度。

- `getboot-http-client`
- `getboot-rpc`
- `getboot-mq`
- `getboot-job`

规则：

- 强调协议/通信能力，而不是业务逻辑
- Trace、安全、配置桥接等横切能力应尽量复用已有模块

### 0.5 Ecosystem Modules

面向特定生态或平台的接入模块。

- `getboot-wechat`
- `getboot-payment`

规则：

- 不把平台 SDK 直接暴露为仓库通用规范
- 尽量通过 `api` / `support` 提供稳定门面

## 0.6 依赖方向

总体依赖方向固定为：

`Foundation -> Infrastructure Capability -> Cross-Cutting / Communication -> Ecosystem`

允许同层模块在必要时协作，但必须避免形成循环依赖。

更具体的约束：

- `support` / `exception` 不应依赖业务能力模块
- `coordination` / `cache` / `database` / `observability` 不应依赖 `mq` / `rpc` / `job` / `auth`
- `lock` / `limiter` 可以依赖 `coordination`
- `transaction` 可以依赖 `database`
- `webhook` 可以依赖 `lock` / `limiter`
- `rpc` / `mq` / `http-client` / `job` 应尽量只依赖底座与可观测性等通用能力

## 1. 总规则

模块根包代表能力边界，不代表具体技术栈。

示例：

- `com.getboot.lock.*` 表示分布式锁能力
- `com.getboot.mq.*` 表示消息能力
- `com.getboot.rpc.*` 表示远程调用能力

所有技术栈绑定实现必须放在 `infrastructure` 下，不允许直接挂在模块根包。

正确示例：

- `com.getboot.lock.infrastructure.redis.redisson.*`
- `com.getboot.limiter.infrastructure.slidingwindow.redisson.*`
- `com.getboot.rpc.infrastructure.dubbo.*`
- `com.getboot.mq.infrastructure.rocketmq.*`

错误示例：

- `com.getboot.lock.redisson.*`
- `com.getboot.mq.rocketmq.*`
- `com.getboot.rpc.dubbo.*`

Foundation 模块同样适用“稳定层优先”的思路，只是不强制为了凑齐目录而制造空的 `spi` / `support`。

## 2. 各层职责

### 2.1 `api`

对外能力入口，业务代码可直接依赖。

允许出现的子目录：

- `annotation`
- `exception`
- `model`
- `properties`
- `request`
- `response`
- `resolver`
- `validator`
- `security`
- `accessor`
- `producer`
- `consumer`
- `context`

规则：

- 去掉具体技术栈后仍成立的类型，优先放 `api`
- 类名中如果直接带技术栈词汇，默认不放 `api`
- 业务方需要注入或直接依赖的服务门面，优先在 `api` 中定义接口

### 2.2 `spi`

模块扩展点。

规则：

- 业务方或其他模块通过注册 Bean 扩展行为的接口，统一放 `spi`
- `spi` 不承载具体技术实现
- 扩展点如果是模块通用能力，放在模块根 `spi`
- 扩展点如果直接暴露底层技术栈类型，则放在 `spi.<tech>`
- 不再把真实扩展点留在 `infrastructure.<tech>.spi`

示例：

- `com.getboot.rpc.spi.*`
- `com.getboot.rpc.spi.dubbo.*`
- `com.getboot.httpclient.spi.feign.*`
- `com.getboot.observability.spi.prometheus.*`

### 2.3 `support`

能力层辅助工具。

规则：

- 放模块内部默认实现、内部工具类、内部适配门面
- `support` 不作为正式对外承诺层
- 业务方若需要直接注入，应优先依赖 `api` / `spi`
- `support` 可以实现 `api`，但 `api` 不允许反向依赖 `support`

示例：

- 默认实现类
- 内部 builder / helper / facade

### 2.4 `infrastructure`

技术实现层。

规则：

- 所有技术栈绑定实现必须放这里
- 自动配置、环境映射、技术配置、监听器、拦截器、适配器都放这里

## 3. `infrastructure` 命名规则

`infrastructure` 后的层级，从“能力子域/介质/算法”一路收窄到技术栈。

### 3.1 只有技术栈差异

适用于 `mq`、`rpc`、`auth`：

```text
com.getboot.mq.infrastructure.rocketmq.*
com.getboot.rpc.infrastructure.dubbo.*
com.getboot.auth.infrastructure.satoken.*
```

### 3.2 先有能力子域，再有技术栈

适用于 `database`、`observability`、`http-client`：

```text
com.getboot.database.infrastructure.datasource.*
com.getboot.database.infrastructure.mybatisplus.*
com.getboot.database.infrastructure.sharding.*

com.getboot.observability.infrastructure.prometheus.*
com.getboot.observability.infrastructure.skywalking.*
com.getboot.observability.infrastructure.reactor.*

com.getboot.httpclient.infrastructure.feign.*
com.getboot.httpclient.infrastructure.webclient.*
com.getboot.httpclient.infrastructure.resttemplate.*
```

### 3.3 先有介质/算法，再有技术栈

适用于 `lock`、`limiter`：

```text
com.getboot.lock.infrastructure.redis.redisson.*
com.getboot.lock.infrastructure.database.jdbc.*
com.getboot.lock.infrastructure.zookeeper.curator.*

com.getboot.limiter.infrastructure.slidingwindow.redisson.*
com.getboot.limiter.infrastructure.tokenbucket.redis.*
com.getboot.limiter.infrastructure.leakybucket.redis.*
```

## 4. 典型目录模板

```text
com.getboot.xxx
├── api
│   ├── annotation
│   ├── exception
│   ├── model
│   ├── properties
│   └── ...
├── spi
├── support
├── infrastructure
│   └── <subtree>
│       ├── autoconfigure
│       ├── environment
│       ├── properties
│       ├── support
│       └── ...
```

## 5. 自动配置与环境映射规则

### 5.1 AutoConfiguration

自动配置类必须放 `infrastructure.*.autoconfigure`

示例：

- `com.getboot.lock.infrastructure.redis.redisson.autoconfigure.*`
- `com.getboot.auth.infrastructure.satoken.autoconfigure.*`
- `com.getboot.mq.infrastructure.rocketmq.autoconfigure.*`

### 5.2 EnvironmentPostProcessor

配置前缀映射类必须放 `infrastructure.*.environment`

示例：

- `com.getboot.auth.infrastructure.satoken.environment.*`
- `com.getboot.rpc.infrastructure.dubbo.environment.*`
- `com.getboot.mq.infrastructure.rocketmq.environment.*`

### 5.3 Properties

规则如下：

- 不绑定具体技术栈的能力层配置：放 `api.properties`
- 明显绑定某个实现的配置：放 `infrastructure.*.properties`

### 5.4 SPI

规则如下：

- 模块真实扩展点统一放在外层 `spi`
- 技术栈专属扩展点统一放在 `spi.<tech>`
- `infrastructure` 内允许有内部协作接口，但不再使用 `spi` 命名对外暴露

## 6. 判断标准

看到一个类时，按下面问题判断它应放哪里：

1. 去掉技术栈后，这个类还能成立吗？
   - 能：优先放 `api` / `spi` / `support`
   - 不能：放 `infrastructure`
2. 新增 Kafka / gRPC / Zookeeper / Seata 时，这个类还会复用吗？
   - 会：优先放 `api` / `spi` / `support`
   - 不会：放 `infrastructure`
3. 类名里是否带技术栈词汇？
   - 带：默认放 `infrastructure`

## 7. 当前统一目标

全仓模块最终都要满足下面的可读性要求：

- 看 `api`，知道模块对外提供什么能力
- 看 `spi`，知道模块允许怎么扩展
- 看 `support`，知道模块提供了哪些稳定辅助入口
- 看 `infrastructure`，知道当前接了什么技术栈
- 新增实现时，只增加新的 `infrastructure` 子树，不改已有外部入口
