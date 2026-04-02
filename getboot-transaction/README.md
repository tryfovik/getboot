# getboot-transaction

分布式事务 starter，当前提供 Seata 接入抽象、统一配置前缀，以及与 `getboot-database` 分库分表能力的冲突保护。

## 作用

- 统一 Seata 配置前缀与模块边界
- 提供 Seata 与 ShardingSphere 组合场景的兼容性守卫
- 为后续其他分布式事务技术栈预留统一入口

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-transaction</artifactId>
</dependency>
```

适合这几类场景：

- 想把 Seata 配置收敛到统一前缀，而不是直接在业务项目散落 `seata.*`
- 想在启用 Seata 时顺手做一层与 ShardingSphere 的兼容保护
- 后续可能新增其他事务实现，但希望对外仍保持统一模块边界

## 前置条件

- 需要准备 Seata Server、注册中心、配置中心等运行环境
- 至少开启 `getboot.transaction.enabled=true` 与 `getboot.transaction.seata.enabled=true`
- 当 `getboot.transaction.enabled=false` 且未显式声明原生 `seata.enabled` 时，会同步压低桥接后的 `seata.enabled=false`，避免底层 Seata 自动装配残留生效
- 如果与分库分表一起使用，建议同步引入 `getboot-database` 并明确事务组合策略
- 默认将 `Seata + Sharding` 视为高风险组合；若必须混用，需要显式声明 `allow-sharding-hybrid=true`

## 目录约定

- `api.properties`：对外稳定配置模型
- `support.compatibility`：事务与分库分表兼容性校验
- `infrastructure.seata.*`：Seata 接入与自动装配

## 配置示例

```yaml
getboot:
  transaction:
    enabled: true
    seata:
      enabled: true
      mode: AT
      application-id: demo-order-service
      tx-service-group: demo_tx_group
    compatibility:
      fail-fast-on-sharding-conflict: true
      allow-sharding-hybrid: false
      expected-sharding-transaction-type: LOCAL
```

## 默认 Bean

- `SeataShardingCompatibilityVerifier`：Seata 与分库分表兼容性校验器

## 扩展点

- 业务项目统一使用 `getboot.transaction.*`
- Seata 能力配置统一收敛到 `com.getboot.transaction.api.properties.*`
- Seata 实现相关代码统一收敛在 `com.getboot.transaction.infrastructure.seata.*`
- `getboot.transaction.enabled=true` 且 `getboot.transaction.seata.enabled=true` 时，会启用 Seata 配置桥接与兼容性检查
- 当前兼容检查要求 `getboot.database.sharding.transaction-type=LOCAL`，避免 ShardingSphere 与 Seata 同时接管分布式事务
- 兼容检查同时识别 `getboot.database.sharding.rules.*` 与原生 `spring.shardingsphere.rules.sharding.*`
- 当前模块暂无独立 SPI，优先通过替换 `SeataShardingCompatibilityVerifier` Bean 或标准 Spring Bean 覆盖进行定制

## 已实现技术栈

- Seata

## 边界 / 补充文档

- 当前模块主要负责 Seata 配置桥接和兼容性守卫，不试图抽象完整的分布式事务编程模型
- `getboot.transaction.seata.*` 会桥接到底层 `seata.*`，但 `mode` 只保留在模块能力层，不直接映射到原生配置
- 与 `getboot-database` 联用时，建议同时检查 ShardingSphere 规则文件与事务类型配置
- 可直接参考 `src/main/resources/getboot-transaction-seata.yml.example`
