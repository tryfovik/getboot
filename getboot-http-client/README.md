# getboot-http-client

HTTP 客户端 Trace 透传 starter。

## 作用

- 提供 OpenFeign 出站 `traceId` 自动透传能力
- 提供 WebClient 出站 `traceId` 自动透传能力
- 提供 RestTemplate 出站 `traceId` 自动透传能力
- 通过统一扩展点，便于业务补充 HTTP 出站请求头

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-http-client</artifactId>
</dependency>
```

适合这几类场景：

- 业务项目已经在用 OpenFeign、WebClient 或 RestTemplate
- 想统一补 `traceId`、租户标识、应用标识等出站请求头
- 想把 HTTP 客户端透传逻辑从业务代码里拿掉

## 前置条件

- 项目里至少要实际使用一种 HTTP 客户端能力，模块增强才有落点
- 如果你想获得完整链路 Trace 体验，通常会和 `getboot-observability` 一起使用

## 目录约定

- `api.properties`：三类 HTTP 客户端的 Trace 配置模型
- `spi.feign`：OpenFeign 请求定制扩展点
- `spi.webclient`：WebClient 请求定制扩展点
- `spi.resttemplate`：RestTemplate 请求定制扩展点
- `infrastructure.feign.*`：OpenFeign 自动装配与实现
- `infrastructure.webclient.*`：WebClient 自动装配与实现
- `infrastructure.resttemplate.*`：RestTemplate 自动装配与实现

## 配置示例

```yaml
getboot:
  http-client:
    openfeign:
      trace:
        enabled: true                     # 是否启用 OpenFeign TraceId 透传
        header-name: X-Trace-Id           # OpenFeign 出站请求头名称
    webclient:
      trace:
        enabled: true                     # 是否启用 WebClient TraceId 透传
        header-name: X-Trace-Id           # WebClient 出站请求头名称
    resttemplate:
      trace:
        enabled: true                     # 是否启用 RestTemplate TraceId 透传
        header-name: X-Trace-Id           # RestTemplate 出站请求头名称
```

## 默认 Bean

- `RequestInterceptor`：Bean 名称为 `getbootTraceFeignRequestInterceptor`
- `ExchangeFilterFunction`：Bean 名称为 `getbootTraceWebClientFilterFunction`
- `WebClientCustomizer`：Bean 名称为 `getbootTraceWebClientCustomizer`
- `ClientHttpRequestInterceptor`：Bean 名称为 `getbootTraceRestTemplateInterceptor`
- `RestTemplateCustomizer`：Bean 名称为 `getbootTraceRestTemplateCustomizer`

## 扩展点

- `OpenFeignTraceRequestCustomizer`
- `WebClientTraceRequestCustomizer`
- `RestTemplateTraceRequestCustomizer`
- 即便未引入 `getboot-observability`，HTTP 客户端透传能力也可独立工作；只是没有统一链路上下文时，会退回到请求头透传逻辑
- 当前模块只处理出站 HTTP 客户端能力，不承接服务端 Web 能力，也不承接 Dubbo、gRPC 等 RPC 技术栈

## 已实现技术栈

- OpenFeign
- WebClient
- RestTemplate

补充说明：

- 业务项目统一使用 `getboot.http-client.*`
- OpenFeign 配置使用 `getboot.http-client.openfeign.trace.*`
- WebClient 配置使用 `getboot.http-client.webclient.trace.*`
- RestTemplate 配置使用 `getboot.http-client.resttemplate.trace.*`
