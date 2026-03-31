# getboot-limiter

分布式限流 starter，对外主入口只有一个：`@RateLimit`。

业务方法直接在注解上选择算法、速率、周期和 key 解析方式，不需要先配一套命名规则再引用。

## 作用

- 提供单注解限流入口 `@RateLimit`
- 在注解上直接声明滑动窗口、令牌桶或漏桶算法
- 基于 Redis / Redisson 提供分布式限流实现

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-limiter</artifactId>
</dependency>
```

这个模块依赖 `getboot-coordination` 提供的 Redisson 基础设施，因此需要准备：

```yaml
getboot:
  coordination:
    redisson:
      file: classpath:redisson/redisson.yaml
```

## 最小用法

最简单的写法是直接对方法做整体限流，不区分调用方：

```java
@RateLimit(rate = 10, interval = 1, intervalUnit = TimeUnit.MINUTES)
public void createOrder() {
}
```

如果要按业务 key 限流，可以在注解里直接写固定 key 或 SpEL：

```java
@RateLimit(
        scene = "send-sms",
        keyExpression = "#phone",
        algorithm = LimiterAlgorithm.TOKEN_BUCKET,
        rate = 1,
        interval = 60,
        intervalUnit = TimeUnit.SECONDS,
        timeout = 0,
        timeoutUnit = TimeUnit.SECONDS
)
public void sendSms(String phone) {
}
```

漏桶也是同一个注解，只是算法不同：

```java
@RateLimit(
        scene = "webhook-callback",
        keyExpression = "#eventId",
        algorithm = LimiterAlgorithm.LEAKY_BUCKET,
        rate = 10,
        interval = 1,
        intervalUnit = TimeUnit.SECONDS
)
public void handleWebhook(String eventId) {
}
```

## 注解说明

- `scene`
  限流场景名；为空时默认使用 `类名.方法名`
- `key`
  固定限流 key；配置后优先使用
- `keyExpression`
  动态限流 key 的 SpEL 表达式，例如 `#userId`、`#request.phone`、`#p0`、`#args[0]`
- `algorithm`
  限流算法，当前支持 `SLIDING_WINDOW`、`TOKEN_BUCKET`、`LEAKY_BUCKET`
- `rate`
  单个周期内的容量或配额
- `interval` / `intervalUnit`
  周期大小和单位
- `permits`
  单次调用申请的许可数，默认 `1`
- `timeout` / `timeoutUnit`
  获取许可时的最大等待时间，默认 `0`，即立即失败
- `message`
  获取许可失败时的提示信息

## 可选配置

这个模块默认零规则配置即可使用。

如果只是想开启或关闭模块，通常只需要：

```yaml
getboot:
  limiter:
    enabled: true
```

下面这些配置只属于高级可选项，不是接入前提：

- `getboot.limiter.sliding-window.key-prefix`
  自定义滑动窗口 Redis key 前缀
- `getboot.limiter.token-bucket.key-prefix`
  自定义令牌桶 Redis key 前缀
- `getboot.limiter.leaky-bucket.key-prefix`
  自定义漏桶 Redis key 前缀

## 默认 Bean

- `RateLimitAspect`
  方法级限流切面
- `RateLimiterRegistry`
  内部统一路由入口，按算法分发到具体实现
- `SlidingWindowRedisSupport`
  滑动窗口底层支持
- `TokenBucketRedisSupport`
  令牌桶底层支持
- `LeakyBucketRedisSupport`
  漏桶底层支持

## 边界

- 对外推荐只使用 `@RateLimit`
- `RateLimiterRegistry` 更偏内部能力拼装与高级编程式接入，不作为主文档入口
- 当前滑动窗口实现基于 Redis 有序集合
- 当前令牌桶实现基于 Redisson `RRateLimiter`
- 当前漏桶实现基于 Redis 状态字符串与 Redisson 分布式锁
