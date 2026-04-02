# Seata 与 ShardingSphere 兼容说明

这份文档只回答一件事：`getboot-transaction` 为什么默认对 `Seata + ShardingSphere` 组合保持保守态度，以及当前模块到底怎么判定冲突。

## 当前结论

- 默认把 `Seata + ShardingSphere` 视为高风险组合
- 默认行为是失败即停，不悄悄放过
- 真要混用，必须显式开启 `allow-sharding-hybrid=true`
- 即使显式放行，也要求 ShardingSphere 事务类型保持 `LOCAL`

## 守卫什么时候生效

同时满足下面条件时，`SeataShardingCompatibilityVerifier` 会开始校验：

1. `getboot.transaction.enabled=true`
2. `getboot.transaction.seata.enabled=true`
3. 检测到任一分库分表信号：
   - `getboot.database.sharding.enabled=true`
   - 存在 `getboot.database.sharding.rules.*`
   - 存在原生 `spring.shardingsphere.rules.sharding.*`

这次收敛后，兼容性检测不再只依赖显式开关，直接写原生 `spring.shardingsphere.rules.sharding.*` 也会被识别。

## 默认失败策略

如果检测到 ShardingSphere，同时又没有显式放行，默认会抛出异常并终止启动。

相关配置：

```yaml
getboot:
  transaction:
    compatibility:
      fail-fast-on-sharding-conflict: true
```

如果你只想先给出告警、不阻断启动，可以把它调成 `false`，但这只是降级为告警，不代表组合天然安全。

## 允许混用的最小条件

只有同时满足下面条件，守卫才会放行：

```yaml
getboot:
  transaction:
    compatibility:
      allow-sharding-hybrid: true
      expected-sharding-transaction-type: LOCAL
  database:
    sharding:
      transaction-type: LOCAL
```

等价的原生写法也会被识别：

```yaml
spring:
  shardingsphere:
    props:
      default-transaction-type: LOCAL
```

## 配置桥接的行为边界

### 1. `getboot.transaction.seata.*` 会桥接到 `seata.*`

这意味着业务项目可以继续只写 GetBoot 前缀，而底层 Seata 仍然能拿到原生配置。

### 2. `mode` 不会桥接到 `seata.mode`

`getboot.transaction.seata.mode` 目前只是 GetBoot 能力层的模式标识，不直接映射到底层 Seata 原生属性。

### 3. 全局关闭会压低桥接出的 `seata.enabled`

当：

```yaml
getboot:
  transaction:
    enabled: false
```

且业务项目没有显式声明原生 `seata.enabled` 时，环境桥接会补出：

```yaml
seata:
  enabled: false
```

这样可以避免“GetBoot 事务模块明明关了，但底层 Seata 自动装配还在启动”的残留行为。

## 推荐接入姿势

### 纯 Seata，不和分库分表混用

```yaml
spring:
  application:
    name: demo-order-service

getboot:
  transaction:
    enabled: true
    seata:
      enabled: true
      tx-service-group: demo_tx_group
```

### Seata + ShardingSphere，明确接受混用风险

```yaml
spring:
  application:
    name: demo-order-service
  shardingsphere:
    props:
      default-transaction-type: LOCAL

getboot:
  transaction:
    enabled: true
    seata:
      enabled: true
      tx-service-group: demo_tx_group
    compatibility:
      allow-sharding-hybrid: true
      expected-sharding-transaction-type: LOCAL
```

## 建议

- 如果没有明确业务约束，不要默认启用 `Seata + ShardingSphere` 混用
- 如果必须混用，至少把事务类型、规则文件和灰度验证策略一起纳入上线检查
- 遇到组合定制需求，优先覆盖 `SeataShardingCompatibilityVerifier`，不要直接把校验逻辑散落回业务项目
