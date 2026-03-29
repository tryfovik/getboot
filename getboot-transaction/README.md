# getboot-transaction

分布式事务 starter，当前提供 Seata 接入抽象、统一配置前缀，以及与 `getboot-database` 分库分表能力的冲突保护。

## 作用

- 统一 Seata 配置前缀与模块边界
- 提供 Seata 与 ShardingSphere 组合场景的兼容性守卫
- 为后续其他分布式事务技术栈预留统一入口

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
- Seata 原生配置统一收敛到 `getboot.transaction.seata.*`
- 分布式事务统一配置根收敛在 `com.getboot.transaction.api.properties.*`
- Seata 实现相关代码统一收敛在 `com.getboot.transaction.infrastructure.seata.*`
- `getboot.transaction.enabled=true` 且 `getboot.transaction.seata.enabled=true` 时，会启用 Seata 配置桥接与兼容性检查
- 默认将 `Seata + Sharding` 视为高风险组合；若必须混用，需要显式声明 `allow-sharding-hybrid=true`
- 当前兼容检查要求 `getboot.database.sharding.transaction-type=LOCAL`，避免 ShardingSphere 与 Seata 同时接管分布式事务
- 当前模块暂无独立 SPI，优先通过替换兼容性校验 Bean 或标准 Spring Bean 覆盖进行定制

## 已实现技术栈

- Seata
