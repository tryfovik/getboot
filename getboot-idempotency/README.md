# getboot-idempotency

声明式幂等 starter，当前第一版先把最容易反复散落在业务里的那一层收口：

- 提供统一注解入口 `@Idempotent`
- 基于 Redis 存储幂等状态与成功结果
- 同 key 重复请求在成功后直接复用缓存结果
- 同 key 并发请求在首个请求执行期间直接拦截，避免业务重复执行

## 接入方式

业务项目继承父 `pom` 后，引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-idempotency</artifactId>
</dependency>
```

当前实现依赖 `getboot-cache` 提供的默认 RedisTemplate，因此只要业务里已经接入 Redis，就可以直接启用。

## 配置说明

```yaml
getboot:
  idempotency:
    enabled: true
    type: redis
    default-ttl-seconds: 300
    redis:
      enabled: true
      key-prefix: getboot:idempotency
```

- `default-ttl-seconds`
  注解没有单独指定 `ttlSeconds` 时使用的默认 TTL
- `redis.key-prefix`
  最终 key 统一拼成 `<key-prefix>:<scene>#<resolved-key>`

## 使用示例

```java
@Service
public class OrderApplicationService {

    @Idempotent(scene = "order:create", keyExpression = "#request.requestNo", ttlSeconds = 600)
    public OrderCreateResponse create(OrderCreateRequest request) {
        return doCreate(request);
    }
}
```

`key` 与 `keyExpression` 二选一即可，`key` 优先级更高。`scene` 为空时默认回退到 `类名.方法名`。

## 第一版语义

- 首次请求先写入 `PROCESSING` 状态，占住幂等键
- 业务执行成功后，把结果写成 `COMPLETED` 状态并继续保留 TTL
- 后续相同 key 请求如果命中 `COMPLETED`，直接返回第一次成功结果
- 后续相同 key 请求如果命中 `PROCESSING`，抛出 `IdempotencyException`
- 业务执行异常时删除占位记录，允许后续请求重试

这版先解决“不要重复跑业务”和“成功结果可以复用”两件事，还没有扩展到 JDBC 存储、等待中的重复请求轮询返回、结果序列化策略定制等更重的能力。

## 默认 Bean

- `IdempotencyKeyResolver`
  默认实现是 `SpelIdempotencyKeyResolver`
- `IdempotencyDuplicateRequestHandler`
  默认实现是 `DefaultIdempotencyDuplicateRequestHandler`
- `IdempotencyStore`
  当 `type=redis` 且容器内存在 `getbootRedisTemplate` 时，默认实现是 `RedisIdempotencyStore`
- `IdempotencyAspect`
  当存在 `IdempotencyStore` 时自动注册

## 目录约定

- `api.*`
  注解、异常、模型、配置
- `spi.*`
  幂等键解析、重复请求处理、存储扩展点
- `support.*`
  默认 SpEL 键解析、默认重复请求处理、AOP 切面
- `infrastructure.redis.*`
  Redis 存储与自动装配

## 补充文档

- 配置示例：[`getboot-idempotency.yml.example`](./src/main/resources/getboot-idempotency.yml.example)
