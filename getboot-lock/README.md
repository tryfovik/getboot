# getboot-lock

声明式分布式锁 starter，当前支持 `redis.redisson`、`database.jdbc`、`zookeeper.curator` 三种实现。

## 作用

- 提供统一注解入口 `@DistributedLock`
- 提供统一锁键解析 SPI `DistributedLockKeyResolver`
- 提供统一失败处理 SPI `DistributedLockAcquireFailureHandler`
- 提供 Redis、JDBC、ZooKeeper 三套锁切面实现

## 接入方式

业务项目继承父 `pom` 后，引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-lock</artifactId>
</dependency>
```

## 当前支持实现

| `getboot.lock.type` | 实现 | 前置条件 | 说明 |
| --- | --- | --- | --- |
| `redis` | `infrastructure.redis.redisson.*` | Spring 容器中存在 `RedissonClient` | 默认实现 |
| `database` | `infrastructure.database.jdbc.*` | Spring 容器中存在单个 `DataSource` | 适合没有 Redis 锁基础设施的场景 |
| `zookeeper` | `infrastructure.zookeeper.curator.*` | Spring 容器中存在 `CuratorFramework` | 使用 ZooKeeper 会话语义，不支持显式租约 |

## 配置说明

通用开关与选择器：

```yaml
getboot:
  lock:
    enabled: true
    type: redis
```

Redis / Redisson：

```yaml
getboot:
  lock:
    enabled: true
    type: redis
    redis:
      enabled: true
      key-prefix: distributed_lock
  coordination:
    redisson:
      file: classpath:redisson/redisson.yaml
```

JDBC 数据库锁：

```yaml
getboot:
  lock:
    enabled: true
    type: database
    database:
      enabled: true
      key-prefix: distributed_lock
      table-name: distributed_lock
      lease-ms: 30000
      retry-interval-ms: 100
      initialize-schema: false
```

`database.initialize-schema=true` 只适合本地或简单环境快速启动。生产环境建议自行管理建表 SQL，并参考 [`getboot-lock-database.sql.example`](./src/main/resources/getboot-lock-database.sql.example)。

ZooKeeper / Curator：

```yaml
getboot:
  lock:
    enabled: true
    type: zookeeper
    zookeeper:
      enabled: true
      key-prefix: distributed_lock
      base-path: /getboot/lock
  coordination:
    zookeeper:
      enabled: true
      connect-string: 127.0.0.1:2181
      namespace: getboot
```

## 锁语义

- 完整锁键格式统一为 `<key-prefix>:<scene>#<resolved-key>`
- `key` 优先于 `keyExpression`
- `waitTime=Integer.MAX_VALUE` 表示一直等待
- 获取锁失败时会先调用 `DistributedLockAcquireFailureHandler`
- 如果自定义失败处理器没有抛异常，框架仍会抛出 `DistributedLockException`，业务方法不会继续执行

不同实现的租约语义：

- `redis`：`expireTime=-1` 使用 Redisson 默认 watchdog 语义
- `database`：`expireTime=-1` 回退到 `getboot.lock.database.lease-ms`
- `zookeeper`：只支持 `expireTime=-1`，使用 ZooKeeper 会话语义，不接受显式租约毫秒数

## 默认 Bean

- `DistributedLockKeyResolver` 默认实现为 `SpelDistributedLockKeyResolver`
- `DistributedLockAcquireFailureHandler` 默认实现为 `DefaultDistributedLockAcquireFailureHandler`
- `DistributedLockAspect` 在 `type=redis` 时注册
- `JdbcDistributedLockAspect` 在 `type=database` 时注册
- `ZookeeperDistributedLockAspect` 在 `type=zookeeper` 时注册

## 目录约定

- `api.*`：注解、常量、异常、配置模型
- `spi.*`：锁键解析与失败处理扩展点
- `support.*`：共享锁键拼装、等待时间与失败处理辅助
- `infrastructure.redis.redisson.*`：Redis / Redisson 实现
- `infrastructure.database.jdbc.*`：JDBC 数据库锁实现
- `infrastructure.zookeeper.curator.*`：ZooKeeper / Curator 实现

## 已实现技术栈

- Redis Lock
- Redisson
- JDBC Database Lock
- ZooKeeper Curator Lock

## 补充文档

- 配置示例：[`getboot-lock.yml.example`](./src/main/resources/getboot-lock.yml.example)
- 建表示例：[`getboot-lock-database.sql.example`](./src/main/resources/getboot-lock-database.sql.example)
