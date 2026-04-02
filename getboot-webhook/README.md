# getboot-webhook

Webhook / 事件回调接入安全 starter，用于统一处理验签、限流、幂等和请求体缓存。

## 作用

- 提供请求体缓存、签名校验、限流与幂等编排入口
- 收敛 Webhook 安全配置与应用密钥解析方式
- 提供请求校验扩展钩子，便于接入方补充业务规则

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-webhook</artifactId>
</dependency>
```

适合这几类场景：

- 想统一第三方事件回调的验签、限流和幂等处理
- 想把请求体缓存和签名校验从具体业务控制器里抽出来
- 想通过统一的请求校验钩子扩展业务规则，而不是在每个回调入口重复写模板代码

## 前置条件

- 至少开启 `getboot.webhook.security.enabled=true`
- 至少准备一组 `getboot.webhook.security.credentials`
- 模块自身已经依赖 `getboot-idempotency`、`getboot-limiter`，其中 `getboot-idempotency` 会继续复用 `getboot-cache`
- 默认处理器要求运行时存在 `IdempotencyStore` 与 `RateLimiter`
- 因此仍需要准备 Redis 与限流对应配置；如果这些底层 Bean 不存在，默认 `WebhookRequestProcessor` 不会注册

## 目录约定

- `api.*`：能力层配置、处理入口与密钥解析接口
- `spi`：Webhook 请求校验扩展点
- `support`：默认处理器、校验器与签名辅助类
- `infrastructure.servlet.*`：Servlet 请求体缓存实现
- `infrastructure.autoconfigure`：Spring Boot 自动装配入口

## 配置示例

```yaml
getboot:
  cache:
    redis:
      host: 127.0.0.1               # Redis 主机地址
      port: 6379                    # Redis 端口
  coordination:
    redisson:
      file: classpath:redisson/redisson.yaml # Redisson 配置文件位置
  webhook:
    security:
      enabled: true                 # 是否启用事件请求安全处理
      credentials:
        - name: 默认应用            # 配置项名称，仅用于区分和说明
          app-key: demo-app         # 调用方应用标识
          app-secret: demo-secret   # 调用方签名密钥
```

## 默认 Bean

- `AppSecretResolver`：默认实现为 `PropertiesAppSecretResolver`
- `WebhookRequestValidator`：默认请求校验器
- `FilterRegistrationBean<CachingRequestBodyFilter>`：请求体缓存过滤器注册器
- `WebhookRequestProcessor`：满足幂等存储与限流条件时自动注册默认处理器

## 扩展点

- 启用后会自动注册请求体缓存过滤器，确保后续验签逻辑能够重复读取请求体
- `WebhookRequestProcessor` 负责统一处理验签、限流与幂等编排
- 业务项目统一使用 `getboot.webhook.security.*`
- 能力层配置、处理入口与密钥解析接口统一收敛在 `com.getboot.webhook.api.*`
- Servlet 请求体缓存与 Spring 自动装配统一收敛在 `com.getboot.webhook.infrastructure.*`
- 可通过注册 `AppSecretResolver` Bean 替换默认密钥解析方式
- 可通过注册 `WebhookRequestValidationHook` Bean 追加自定义请求校验逻辑

## 已实现技术栈

- Servlet Filter
- RateLimiter + IdempotencyStore 组合编排

## 边界 / 补充文档

- 当前模块只承接 Servlet 场景下的 Webhook 安全编排，不覆盖 WebFlux 处理链
- 模块自身不定义统一回调 Controller 协议，只提供安全处理入口与组合能力
- `getboot.webhook.security.*` 由模块自己的 `@ConfigurationProperties` 承接，没有额外原生前缀桥接
- 可直接参考 `src/main/resources/getboot-webhook.yml.example`
