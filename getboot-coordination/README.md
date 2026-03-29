# getboot-coordination

分布式协调基础 starter，当前先承接 Redisson 依赖收口和配置桥接。

## 作用

- 提供统一的 Redisson 接入能力
- 为分布式锁、限流、幂等控制等能力提供基础设施支撑

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-coordination</artifactId>
</dependency>
```

这个模块通常不是孤立接入，而是作为下面这些能力的底座：

- `getboot-lock`
- `getboot-limiter`
- `getboot-webhook`

## 前置条件

- 业务环境需要有可访问的 Redis
- 需要准备 Redisson 原生 YAML 配置文件，并通过 `getboot.coordination.redisson.file` 指向它

## 目录约定

- `infrastructure.redisson.environment`：Redisson 配置别名与环境适配

当前模块没有额外抽顶层 `api/spi`，因为现阶段主要职责是收口 Redisson 基础设施，不制造空抽象。

## 配置示例

```yaml
getboot:
  coordination:
    redisson:
      file: classpath:redisson/redisson.yaml  # Redisson 配置文件位置
```

`classpath:redisson/redisson.yaml` 示例：

```yaml
singleServerConfig:
  address: "redis://127.0.0.1:6379"  # Redis 单机地址
  password: null                     # Redis 密码，没有可设为 null
  database: 0                        # Redis 数据库索引
threads: 16                          # Redisson 业务线程数
nettyThreads: 32                     # Redisson 网络线程数
codec: !<org.redisson.codec.JsonJacksonCodec> {}  # 序列化编解码器
transportMode: "NIO"                 # 网络传输模式
```

## 默认 Bean

- 当前模块本身不额外定义新的能力层 Bean
- `RedissonClient` 由引入的 `redisson-spring-boot-starter` 按其自动配置提供

## 扩展点

- 当前没有额外的 `getboot` 侧公开 SPI
- 统一通过 `getboot.coordination.redisson.file` 指向 Redisson 原生 YAML
- 后续如果新增 ZooKeeper 等实现，会优先放在能力模块的实现子树下，而不是推翻外部接入方式

## 已实现技术栈

- Redisson

补充说明：

- Redisson 基础设施配置统一使用 `getboot.coordination.redisson.*`
- 当前模块仅承载 Redisson 配置桥接，代码统一收敛在 `com.getboot.coordination.infrastructure.redisson.*`
- 可直接参考 `src/main/resources/getboot-coordination-redisson.yml.example` 与 `src/main/resources/redisson/redisson.yaml.example`
