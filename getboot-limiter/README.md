# getboot-limiter

分布式限流 starter，当前提供基于 Redis / Redisson 的滑动窗口与令牌桶实现，并通过统一注册表按规则路由算法。

## 作用

- 提供统一限流能力接口、规则模型与注解切面
- 提供统一 `RateLimiterRegistry`，按命名规则在不同算法实现之间路由
- 提供基于 Redis / Redisson 的滑动窗口与令牌桶实现
- 为后续漏桶等算法预留模块边界

## 目录约定

- `api.*`：能力层接口、注解、异常与规则模型
- `spi`：限流注册表定制与算法扩展点
- `support`：内部注册表、AOP 与辅助能力
- `infrastructure.slidingwindow.redisson.*`：滑动窗口 Redisson 实现
- `infrastructure.tokenbucket.redisson.*`：令牌桶 Redisson 实现

## 配置示例

```yaml
getboot:
  coordination:
    redisson:
      file: classpath:redisson/redisson.yaml # Redisson 配置文件位置
  limiter:
    enabled: true                    # 是否启用限流能力
    sliding-window:
      enabled: true                  # 是否启用滑动窗口实现
      default-timeout: 5             # 默认等待时间，单位秒
      key-prefix: rate_limiter       # Redis 中的滑动窗口 key 前缀
      limiters:
        login:
          rate: 10                   # 时间窗口内允许的请求数
          interval: 1                # 时间窗口大小
          interval-unit: MINUTES     # 时间窗口单位，可选 SECONDS / MINUTES / HOURS / DAYS
    token-bucket:
      enabled: true                  # 是否启用令牌桶实现
      default-timeout: 2             # 默认等待时间，单位秒
      key-prefix: rate_limiter_token_bucket
      limiters:
        sms:
          rate: 30                   # 每个时间窗口补充的令牌数
          interval: 1                # 令牌补充周期
          interval-unit: SECONDS     # 支持 MILLISECONDS / SECONDS / MINUTES / HOURS / DAYS
```

## 默认 Bean

- `SlidingWindowRedisSupport`：滑动窗口 Redis 底层支持类
- `TokenBucketRedisSupport`：令牌桶 Redis 底层支持类
- `RateLimiterRegistry`：统一算法路由注册表
- `RateLimiterAlgorithmHandler`：滑动窗口与令牌桶算法处理器
- `RateLimiter`：滑动窗口兼容限流执行器
- `RateLimitAspect`：存在 AOP 条件时自动注册注解切面

## 扩展点

- `getboot-limiter` 复用 `getboot-coordination` 的 Redisson 基础设施配置，统一使用 `getboot.coordination.redisson.*`
- `getboot.coordination.redisson.file` 指向 Redisson 原生 YAML 配置文件
- 模块总开关保持在 `getboot.limiter.enabled`
- 滑动窗口配置收敛在 `getboot.limiter.sliding-window.*`
- 令牌桶配置收敛在 `getboot.limiter.token-bucket.*`
- 能力层接口、注解、异常与规则模型统一收敛在 `com.getboot.limiter.api.*`
- 统一注册表会按 `LimiterRule.algorithm` 路由算法；放在算法子树下的预定义规则会自动补对应算法
- 可通过注册 `RateLimiterRegistryCustomizer` Bean 在启动阶段动态补充或改写命名规则
- 如需新增算法，可额外注册 `RateLimiterAlgorithmHandler` Bean，保持外部 `RateLimiterRegistry` 不变
- 当前滑动窗口实现是真正的滑动窗口计数，不暴露令牌桶语义
- 当前令牌桶实现基于 Redisson `RRateLimiter`，桶容量与单周期补充速率保持一致
- 对外跨算法访问优先依赖 `RateLimiterRegistry`；`RateLimiter` Bean 继续保留为滑动窗口兼容入口

## 已实现技术栈

- Sliding Window
- Token Bucket
- Redis / Redisson
