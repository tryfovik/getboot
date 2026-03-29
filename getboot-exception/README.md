# getboot-exception

通用异常与错误码模块，提供最基础的错误码约定和业务异常模型。

## 作用

- 提供 `BusinessException`
- 提供统一错误码接口 `ErrorCode`
- 提供默认错误码枚举 `CommonErrorCode`

## 接入方式

如果你的业务项目、领域模块或共享模型模块需要统一错误码和业务异常，可以直接引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-exception</artifactId>
</dependency>
```

多数 Web、鉴权、RPC、支付场景下，它也会作为其他 starter 的传递依赖出现。

## 前置条件

当前没有额外前置条件。

## 目录约定

- `api.code`：稳定错误码接口与默认错误码集合
- `api.exception`：稳定业务异常模型

## 配置示例

当前没有强制配置项。

## 默认 Bean

当前不提供自动装配 Bean，属于纯模型模块。

## 扩展点

- 业务侧可以自行实现 `ErrorCode` 接口扩展本项目的错误码体系
- 推荐业务代码统一抛出 `BusinessException`，并尽量绑定明确的错误码

## 已实现技术栈

- 无技术栈耦合，属于纯 Java 基础模型
