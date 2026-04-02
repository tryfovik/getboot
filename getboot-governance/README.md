# getboot-governance

流量治理 starter，当前提供 Sentinel 接入抽象、统一配置前缀与 `@SentinelResource` 切面自动注册。

## 作用

- 统一治理能力入口，避免业务侧直接使用 Sentinel 原生前缀
- 自动补齐 `@SentinelResource` 注解切面
- 为后续新增治理技术栈预留统一模块边界

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-governance</artifactId>
</dependency>
```

适合这几类场景：

- 想把 Sentinel 配置收敛到 `getboot.governance.*`
- 需要在业务方法上直接使用 `@SentinelResource`
- 需要继续扩展 OpenFeign、RestTemplate、Actuator 等 Sentinel 桥接配置

## 前置条件

- 需要准备 Sentinel 运行环境；如果要接控制台，还要准备 Dashboard 地址
- 至少开启 `getboot.governance.enabled=true` 与 `getboot.governance.sentinel.enabled=true`
- 如果只想使用限流注解切面，类路径中仍需要存在 Sentinel 相关依赖
- OpenFeign、RestTemplate、Actuator 相关桥接只在对应组件存在时才有意义

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
      http-method-specify: false
      block-page: /blocked.html
      transport:
        dashboard: 127.0.0.1:8858
        port: 8719
      filter:
        enabled: true
        order: -2147483648
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
- `getboot.governance.sentinel.management.endpoint.enabled` 会映射为 `management.endpoint.sentinel.enabled`
- `getboot.governance.sentinel.management.health.enabled` 会映射为 `management.health.sentinel.enabled`
- `getboot.governance.sentinel.transport.*`、`filter.*`、`block-page`、`http-method-specify` 等通用配置会映射到 `spring.cloud.sentinel.*`
- 当前模块会在类路径存在 Sentinel Aspect 时自动注册 `SentinelResourceAspect`
- 当前模块暂无独立 SPI，优先通过覆盖 `SentinelResourceAspect` 或复用 Sentinel 标准扩展点定制

## 已实现技术栈

- Sentinel

## 边界 / 补充文档

- 当前模块主要负责 Sentinel 配置桥接与切面注册，不承接完整的规则管理平台能力
- `getboot.governance.sentinel.*` 会桥接到 `spring.cloud.sentinel.*`，其中 OpenFeign、RestTemplate、Actuator 相关开关会改写到各自原生前缀
- 如果业务只需要统一限流注解和原生 Sentinel 配置，这个模块已经足够；如果还要治理更多中间件，建议继续按能力拆在各自模块内
- 可直接参考 `src/main/resources/getboot-governance-sentinel.yml.example`
