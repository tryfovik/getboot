# getboot-lock

分布式锁 starter，当前先提供基于 Redis / Redisson 的切面式加锁实现。

## 作用

- 提供声明式分布式锁注解 `@DistributedLock`
- 提供默认锁键解析与失败处理策略
- 提供基于 Redis / Redisson 的加锁切面实现

## 目录约定

- `api.*`：注解、异常、常量与配置模型
- `spi`：锁键解析与失败处理扩展点
- `support`：默认策略实现
- `infrastructure.redis.redisson.*`：Redis / Redisson 实现与自动装配

## 配置示例

```yaml
getboot:
  lock:
    enabled: true
    redis:
      enabled: true
      key-prefix: distributed_lock
  coordination:
    redisson:
      file: classpath:redisson/redisson.yaml # Redisson 配置文件位置
```

## 默认 Bean

- `DistributedLockKeyResolver`：默认实现为 `SpelDistributedLockKeyResolver`
- `DistributedLockAcquireFailureHandler`：默认实现为 `DefaultDistributedLockAcquireFailureHandler`
- `DistributedLockAspect`：默认 Redis / Redisson 分布式锁切面

## 扩展点

- 引入模块后，会在存在 `RedissonClient` 时自动注册分布式锁切面
- 业务方法上可通过 `@DistributedLock` 进行声明式加锁
- 该模块本身没有额外配置项，锁的行为主要由注解参数决定
- `getboot-lock` 复用 `getboot-coordination` 的 Redisson 基础设施配置，统一使用 `getboot.coordination.redisson.*`
- `getboot.coordination.redisson.file` 指向 Redisson 原生 YAML 配置文件
- 模块总开关保持在 `getboot.lock.enabled`
- 当前 Redis 锁能力统一收敛在 `getboot.lock.redis.*`
- 注解、异常与配置模型统一收敛在 `com.getboot.lock.api.*`
- Redis / Redisson 实现收敛在 `com.getboot.lock.infrastructure.redis.redisson.*`
- `getboot.lock.redis.key-prefix` 用于统一拼装 Redis 锁键前缀
- 可通过注册 `DistributedLockKeyResolver` Bean 自定义锁键生成规则
- 可通过注册 `DistributedLockAcquireFailureHandler` Bean 自定义锁获取失败处理逻辑
- 后续新增数据库锁、Zookeeper 锁时，可继续在 `getboot-lock` 下扩展新的介质子树

## 已实现技术栈

- Redis Lock
- Redisson
