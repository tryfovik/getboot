# getboot-support

轻量通用支撑模块，供多个 starter 复用，主要承接 Spring 上下文访问和 Trace 上下文传播的底层能力。

## 作用

- 提供 `SpringContextHolder`
- 提供 `TraceContextHolder`
- 提供 Micrometer 上下文传播所需的线程变量访问器和快照工厂
- 提供配置别名环境处理基类，给其他模块复用

## 接入方式

一般业务项目不需要单独显式引入 `getboot-support`，它通常作为其他 starter 的传递依赖存在，例如：

- `getboot-cache`
- `getboot-observability`
- `getboot-http-client`
- `getboot-rpc`
- `getboot-mq`

只有在下面两类场景，才建议直接关注或显式依赖它：

- 你正在开发新的 `getboot-*` starter
- 你需要手工使用 `TraceContextHolder` 或 `TraceContextPropagationSupport`

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-support</artifactId>
</dependency>
```

## 前置条件

当前没有额外前置条件。

## 目录约定

- `api.context`：稳定的 Spring 容器访问入口
- `api.trace`：稳定的 Trace 上下文读写入口
- `infrastructure.autoconfigure`：自动装配
- `infrastructure.environment`：启动期环境适配能力
- `infrastructure.trace`：基于 Micrometer 的上下文传播实现

## 配置示例

当前没有强制配置项，通常只作为其他 starter 的基础依赖存在。

## 默认 Bean

- `SpringContextHolder`
- `ThreadLocalAccessor<String>`：Bean 名称为 `getbootTraceContextThreadLocalAccessor`
- `ThreadLocalAccessor<?>`：Bean 名称为 `getbootMdcThreadLocalAccessor`
- `ContextRegistry`
- `ContextSnapshotFactory`

## 扩展点

- 当前没有面向业务侧的额外 SPI
- `PropertyAliasEnvironmentPostProcessorSupport` 是给其他 starter 复用的环境处理基类
- `TraceContextPropagationSupport` 提供手工包装任务的辅助方法，便于在自建线程执行时继续透传上下文

## 已实现技术栈

- Spring Boot AutoConfiguration
- Micrometer Context Propagation

补充说明：

- 该模块主要给其他 starter 复用
- 一般业务项目不需要单独显式引入
- 模块保持依赖轻量，避免把 Web、业务异常等语义带入所有基础能力
