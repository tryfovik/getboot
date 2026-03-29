# getboot-rpc

RPC 远程调用增强 starter，当前提供 Dubbo 场景下的请求签名校验、调用方凭证解析、Trace 透传与序列化安全配置。

## 作用

- 提供 Dubbo Trace 透传与认证增强
- 提供调用方凭证解析和请求签名校验
- 提供 Dubbo 序列化安全配置初始化

## 目录约定

- `api.properties`：对外稳定配置模型
- `api.resolver`：调用方密钥解析接口
- `spi`：能力层签名扩展点
- `spi.dubbo`：Dubbo 专属扩展点
- `support.authentication`：默认认证实现
- `infrastructure.dubbo.*`：Dubbo 实现与自动装配

## 配置示例

```yaml
getboot:
  rpc:
    trace:
      enabled: true               # 是否启用 Dubbo TraceId 透传
      mdc-key: traceId            # 日志 MDC 中使用的键名
    dubbo:
      application:
        name: demo-service          # Dubbo 应用名称
    security:
      authentication:
        enabled: true               # 是否启用 RPC 鉴权
        allowed-clock-skew-seconds: 300 # 时间戳允许偏差，单位秒
        excluded-service-prefixes:
          - org.apache.dubbo.       # 不参与鉴权的服务前缀
        consumer:
          app-id: consumer-app      # 消费方应用标识
          app-secret: consumer-secret # 消费方签名密钥
        provider:
          required: true            # 提供方是否强制要求鉴权
          credentials:
            consumer-app: consumer-secret # 提供方认可的调用方凭证
      serialization:
        enabled: true               # 是否启用 Dubbo 序列化安全控制
        check-status: STRICT        # 校验级别
        check-serializable: true    # 是否要求对象实现 Serializable
        allowed-prefixes:
          - com.getboot             # 允许反序列化的包前缀
```

## 默认 Bean

- `RpcAuthenticationSigner`：默认实现为 `DefaultRpcAuthenticationSigner`
- `RpcCallerSecretResolver`：默认实现为 `PropertiesRpcCallerSecretResolver`
- `RpcSecurityConfigurationValidator`：RPC 安全配置校验器
- `RpcSerializationSecurityInitializer`：Dubbo 序列化安全初始化器

## 扩展点

- 业务项目统一使用 `getboot.rpc.*`
- Dubbo 原生配置统一收敛到 `getboot.rpc.dubbo.*`
- 安全配置统一使用 `getboot.rpc.security.*`
- Trace 透传配置统一使用 `getboot.rpc.trace.*`
- RPC 安全与 Trace 配置模型统一收敛到 `com.getboot.rpc.api.properties.*`
- 调用方密钥解析接口统一收敛到 `com.getboot.rpc.api.resolver.*`
- RPC 能力层签名扩展点统一收敛到 `com.getboot.rpc.spi.*`
- Dubbo 认证扩展点统一收敛到 `com.getboot.rpc.spi.dubbo.*`
- Dubbo 实现相关代码统一收敛到 `com.getboot.rpc.infrastructure.dubbo.*`
- 即便未引入 `getboot-observability`，Dubbo Trace 透传能力也可独立工作
- 当前线程存在 `traceId` 时，会自动通过 Dubbo attachment 向下游服务透传
- 服务提供端收到上游 `traceId` 后，会自动回填到 `TraceContextHolder` 与日志 `MDC`
- 当前模块内部实现基于 Dubbo，后续新增 gRPC 等 RPC 技术栈时，仍沿用 `getboot-rpc` 作为统一远程调用模块
- 可通过注册 `RpcAuthenticationSigner` Bean 自定义签名算法
- 可通过注册 `RpcAuthenticationAttachmentCustomizer` Bean 增加消费端认证附件
- 可通过注册 `RpcAuthenticationValidationHook` Bean 追加提供端认证校验逻辑
- 可通过注册 `RpcCallerSecretResolver` Bean 替换默认调用方密钥解析方式
- 默认签名器、默认密钥解析器与配置校验器属于模块内部默认实现，不作为正式对外层承诺

## 已实现技术栈

- Dubbo
