# getboot-coordination

分布式协调基础 starter，当前承接 `redisson` 与 `zookeeper.curator` 两类基础设施接入。

## 作用

- 提供统一的 `RedissonClient` 接入能力
- 提供统一的 `CuratorFramework` 接入能力
- 为分布式锁、限流、Webhook 等能力提供协调基础设施

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-coordination</artifactId>
</dependency>
```

这个模块通常不是孤立接入，而是作为这些能力的底座：

- `getboot-lock`
- `getboot-limiter`
- `getboot-webhook`

## 当前支持实现

| 能力 | 前置条件 | 默认 Bean | 配置前缀 |
| --- | --- | --- | --- |
| `redisson` | 准备 Redisson 原生 YAML | `RedissonClient` | `getboot.coordination.redisson.*` |
| `zookeeper.curator` | 准备可访问的 ZooKeeper 集群 | `CuratorFramework` | `getboot.coordination.zookeeper.*` |

## 目录约定

- `infrastructure.redisson.environment`：Redisson 配置别名与环境适配
- `infrastructure.zookeeper.curator.*`：Curator 配置模型与自动配置

当前模块没有额外抽顶层 `api/spi`，因为现阶段主要职责是收口协调基础设施，不制造空抽象。

## 配置示例

Redisson：

```yaml
getboot:
  coordination:
    redisson:
      file: classpath:redisson/redisson.yaml
```

`classpath:redisson/redisson.yaml` 示例：

```yaml
singleServerConfig:
  address: "redis://127.0.0.1:6379"
  password: null
  database: 0
threads: 16
nettyThreads: 32
codec: !<org.redisson.codec.JsonJacksonCodec> {}
transportMode: "NIO"
```

ZooKeeper / Curator：

```yaml
getboot:
  coordination:
    zookeeper:
      enabled: true
      connect-string: 127.0.0.1:2181
      namespace: getboot
      session-timeout-ms: 60000
      connection-timeout-ms: 15000
      retry:
        base-sleep-time-ms: 1000
        max-retries: 3
        max-sleep-ms: 8000
```

## 默认 Bean

- `RedissonClient` 由引入的 `redisson-spring-boot-starter` 按其自动配置提供
- `CuratorFramework` 由当前模块的 `CuratorZookeeperAutoConfiguration` 提供

## 扩展点

- 当前没有额外的 `getboot` 侧公开 SPI
- `RedissonClient` 与 `CuratorFramework` 都可以通过覆盖同名 Bean 自定义

## 已实现技术栈

- Redisson
- ZooKeeper Curator

## 补充说明

- Redisson 基础设施配置统一使用 `getboot.coordination.redisson.*`
- ZooKeeper 基础设施配置统一使用 `getboot.coordination.zookeeper.*`
- 代码分别收敛在 `com.getboot.coordination.infrastructure.redisson.*` 与 `com.getboot.coordination.infrastructure.zookeeper.curator.*`
- 可直接参考 [`getboot-coordination-redisson.yml.example`](./src/main/resources/getboot-coordination-redisson.yml.example)、[`redisson.yaml.example`](./src/main/resources/redisson/redisson.yaml.example)、[`getboot-coordination-zookeeper.yml.example`](./src/main/resources/getboot-coordination-zookeeper.yml.example)
