# getboot-auth

认证基础 starter，当前提供 Sa-Token 集成增强。

## 作用

- 统一认证能力入口，避免业务侧直接耦合 Sa-Token 细节
- 提供当前登录用户访问门面 `CurrentUserAccessor`
- 通过能力层前缀收敛 Sa-Token 配置
- 提供可复用的 WebFlux Sa-Token 认证过滤能力，供网关或响应式入口统一接入

## 目录约定

- `api.accessor`：对外稳定的当前用户访问接口
- `support`：内部辅助类与能力层适配
- `infrastructure.satoken.*`：Sa-Token 实现与自动装配

## 配置示例

```yaml
getboot:
  auth:
    satoken:
      token-name: satoken             # Token 名称
      timeout: 2592000                # Token 有效期，单位秒
      is-share: true                  # 是否共享 token
      active-timeout: -1              # 最近操作有效期，-1 表示不限制
      is-log: true                    # 是否输出 Sa-Token 日志
      webflux:
        filter:
          enabled: true
          include-paths:
            - /api/admin/**
          exclude-paths:
            - /api/admin/system/ping
          skip-options-request: true
          unauthorized-message: Unauthorized
```

## 默认 Bean

- `CurrentUserAccessor`：默认实现为 `SaTokenCurrentUserAccessor`
- `SaTokenWebFluxAuthChecker`：默认实现为 `DefaultSaTokenWebFluxAuthChecker`
- `SaReactorFilter`：当 `getboot.auth.satoken.webflux.filter.enabled=true` 时自动注册

## 扩展点

- 业务项目统一使用 `getboot.auth.*`
- Sa-Token 原生配置统一收敛到 `getboot.auth.satoken.*`
- 认证能力层入口统一收敛在 `com.getboot.auth.api.*`
- 当前 Sa-Token 实现相关代码统一收敛在 `com.getboot.auth.infrastructure.satoken.*`
- 推荐业务侧直接注入 `CurrentUserAccessor` 接口获取当前登录用户，避免直接耦合具体认证实现
- 若需要替换当前登录用户解析逻辑，可自行提供 `CurrentUserAccessor` Bean
- 若需要覆盖 WebFlux 入口的登录态、角色或权限校验逻辑，可自行提供 `SaTokenWebFluxAuthChecker` Bean
- `SaReactorFilter` 的路径命中、预检放行和失败返回由 `getboot.auth.satoken.webflux.filter.*` 统一控制

## 已实现技术栈

- Sa-Token
- Sa-Token Reactor
