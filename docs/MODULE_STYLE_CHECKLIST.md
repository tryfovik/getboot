# 模块风格治理清单

这份清单只跟踪“注释语言、Lombok 使用、模块内可读性”这条治理线，不替代功能路线图。

## 目标

按模块逐个确认以下事项：

- 类注释使用中文
- 字段注释使用中文
- 手写方法注释使用中文
- 日志、异常消息、技术性对外错误描述使用英文
- 纯数据载体默认统一使用 Lombok，不再混用手写 getter / setter
- Lombok 配置统一跟随根目录 `lombok.config`，不在模块内各自漂移
- 模块 README 与代码实现一致
- 模块级 Maven 验证通过

## 验收口径

- 勾选一个模块，表示该模块已经完成上面整组检查，而不是只改了其中一项
- 纯数据载体如果使用 Lombok，字段注释视为访问器语义说明，不再为 Lombok 生成的 getter / setter 重复补注释
- 存在业务逻辑、条件分支、校验、转换、副作用的手写方法，必须补中文方法注释
- 如果模块边界、配置前缀、默认 Bean 或扩展点变化，必须同步模块 README

## 检查顺序

先从最近活跃和新落地的模块开始，再向存量模块扩展：

1. `getboot-web`
2. `getboot-sms`
3. `getboot-storage`
4. `getboot-search`
5. 其余模块按模块地图顺序推进

## 模块清单

### Foundation

- [x] `getboot-support`
- [x] `getboot-exception`
- [x] `getboot-web`

### Infrastructure Capability

- [x] `getboot-cache`
- [x] `getboot-coordination`
- [x] `getboot-database`
- [x] `getboot-storage`
- [x] `getboot-sms`
- [x] `getboot-search`
- [x] `getboot-observability`

### Cross-Cutting Capability

- [x] `getboot-auth`
- [x] `getboot-limiter`
- [x] `getboot-lock`
- [x] `getboot-idempotency`
- [x] `getboot-governance`
- [x] `getboot-transaction`
- [x] `getboot-webhook`

### Communication

- [x] `getboot-http-client`
- [x] `getboot-rpc`
- [x] `getboot-mq`
- [x] `getboot-job`

### Ecosystem

- [x] `getboot-wechat`
- [x] `getboot-payment`
