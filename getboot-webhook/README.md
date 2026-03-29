# getboot-webhook

Webhook / 事件回调接入安全 starter，用于统一处理验签、限流、幂等和请求体缓存。

## 作用

- 提供请求体缓存、签名校验、限流、幂等与分布式锁编排入口
- 收敛 Webhook 安全配置与应用密钥解析方式
- 提供请求校验扩展钩子，便于接入方补充业务规则

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
- `WebhookRequestProcessor`：满足 Redis、Redisson、限流条件时自动注册默认处理器

## 扩展点

- 启用后会自动注册请求体缓存过滤器
- `WebhookRequestProcessor` 负责统一处理验签、限流、幂等与分布式锁编排
- 该模块依赖 Redis 与 Redisson 环境，请确保业务应用已提供 `getboot.cache.redis.*` 与 `getboot.coordination.redisson.*` 基础配置
- 业务项目统一使用 `getboot.webhook.security.*`
- 模块自身保持在 Webhook 能力层，对 Redis、Redisson、限流与分布式锁能力只通过依赖模块复用，不额外暴露实现前缀
- 能力层配置、处理入口与密钥解析接口统一收敛在 `com.getboot.webhook.api.*`
- Servlet 请求体缓存与 Spring 自动装配统一收敛在 `com.getboot.webhook.infrastructure.*`
- 可通过注册 `AppSecretResolver` 替换默认密钥解析方式
- 可通过注册 `WebhookRequestValidationHook` Bean 追加自定义请求校验逻辑

## 已实现技术栈

- Servlet Filter
- Redis / Redisson 组合编排
