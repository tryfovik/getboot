# getboot-web

Web 基础 starter，提供统一响应模型与 Spring MVC 全局异常处理。

## 作用

- 提供 `ApiResponse`
- 提供 `PagingRequest`、`PagingResponse`
- 自动注册全局异常处理器 `GlobalExceptionHandler`

## 接入方式

适合直接对外提供 Spring MVC 接口的业务服务：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-web</artifactId>
</dependency>
```

如果你的项目是纯 WebFlux 应用，而不是 Spring MVC 服务端，这个模块就不是第一优先入口。

## 前置条件

- 业务应用需要是 Servlet / Spring MVC 场景
- `getboot-web` 自身已经携带 `spring-boot-starter-web` 依赖，通常不需要再单独补同类基础依赖

## 目录约定

- `api.request`：通用请求模型
- `api.response`：通用响应模型
- `infrastructure.autoconfigure`：Web 自动装配入口
- `infrastructure.servlet`：基于 Spring MVC 的异常处理实现

## 配置示例

当前没有强制配置项，引入后即可使用默认响应模型与异常处理。

## 默认 Bean

- `GlobalExceptionHandler`

## 扩展点

- 当前没有额外 SPI
- 若业务需要替换默认异常处理逻辑，可自行提供 `GlobalExceptionHandler` Bean
- 该模块只处理服务端 Web 响应与异常收口，不承接 Trace、鉴权、限流等横切能力

## 已实现技术栈

- Spring MVC
