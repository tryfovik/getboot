# getboot-governance

流量治理 starter，当前提供 Sentinel 接入抽象、统一配置前缀与 `@SentinelResource` 切面自动注册。

## 作用

- 统一治理能力入口，避免业务侧直接使用 Sentinel 原生前缀
- 自动补齐 `@SentinelResource` 注解切面
- 为后续新增治理技术栈预留统一模块边界

## 目录约定

- `api.properties`：对外稳定配置模型
- `infrastructure.sentinel.*`：Sentinel 接入、配置桥接与自动装配

## 配置示例

```yaml
getboot:
  governance:
    enabled: true
    sentinel:
      enabled: true
      eager: false
      web-context-unify: true
      transport:
        dashboard: 127.0.0.1:8858
        port: 8719
      filter:
        enabled: true
      openfeign:
        enabled: true
      rest-template:
        enabled: true
      management:
        endpoint:
          enabled: true
        health:
          enabled: true
```

## 默认 Bean

- `SentinelResourceAspect`：在类路径存在相关依赖时自动注册

## 扩展点

- 业务项目统一使用 `getboot.governance.*`
- Sentinel 原生配置统一收敛到 `getboot.governance.sentinel.*`
- 治理统一配置根收敛在 `com.getboot.governance.api.properties.*`
- Sentinel 实现相关代码统一收敛在 `com.getboot.governance.infrastructure.sentinel.*`
- `getboot.governance.enabled=true` 且 `getboot.governance.sentinel.enabled=true` 时，会启用治理能力桥接
- `getboot.governance.sentinel.openfeign.enabled` 会映射为 `feign.sentinel.enabled`
- `getboot.governance.sentinel.rest-template.enabled` 会映射为 `resttemplate.sentinel.enabled`
- 当前模块会在类路径存在 Sentinel Aspect 时自动注册 `SentinelResourceAspect`
- 当前模块暂无独立 SPI，优先通过覆盖 `SentinelResourceAspect` 或复用 Sentinel 标准扩展点定制

## 已实现技术栈

- Sentinel
