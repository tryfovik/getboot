# getboot-limiter

分布式限流 starter，当前先提供基于 Redis / Redisson 的滑动窗口实现。

## 作用

- 提供统一限流能力接口、规则模型与注解切面
- 提供基于 Redis / Redisson 的滑动窗口实现
- 为后续令牌桶、漏桶等算法预留模块边界

## 目录约定

- `api.*`：能力层接口、注解、异常与规则模型
- `spi`：限流注册表定制扩展点
- `support`：内部 AOP 与辅助能力
- `infrastructure.slidingwindow.redisson.*`：滑动窗口 Redisson 实现

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
```

## 默认 Bean

- `SlidingWindowRedisSupport`：滑动窗口 Redis 底层支持类
- `RateLimiterRegistry`：默认滑动窗口规则注册表
- `RateLimiter`：默认滑动窗口限流执行器
- `RateLimitAspect`：存在 AOP 条件时自动注册注解切面

## 扩展点

- `getboot-limiter` 复用 `getboot-coordination` 的 Redisson 基础设施配置，统一使用 `getboot.coordination.redisson.*`
- `getboot.coordination.redisson.file` 指向 Redisson 原生 YAML 配置文件
- 模块总开关保持在 `getboot.limiter.enabled`
- 当前算法能力统一收敛在 `getboot.limiter.sliding-window.*`
- 能力层接口、注解、异常与规则模型统一收敛在 `com.getboot.limiter.api.*`
- 对外扩展优先依赖能力层 `RateLimiter` / `RateLimiterRegistry`，当前实现收敛在 `com.getboot.limiter.infrastructure.slidingwindow.redisson.*`
- 当前实现是真正的滑动窗口计数，不再直接暴露 Redisson `RRateLimiter` 令牌桶语义
- 可通过注册 `RateLimiterRegistryCustomizer` Bean 在启动阶段动态补充或改写滑动窗口规则
- 后续新增令牌桶、漏桶等算法时，可继续在 `getboot-limiter` 下扩展新的能力子树

## 已实现技术栈

- Sliding Window
- Redis / Redisson
