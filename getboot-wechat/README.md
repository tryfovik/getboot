# getboot-wechat

微信小程序 / 服务号集成 starter。

## 作用

- 统一小程序与服务号接入入口
- 自动初始化按 `appId` 组织的微信 SDK 服务实例
- 在存在 Redis 时为服务号自动启用 Redis token 存储

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-wechat</artifactId>
</dependency>
```

这个模块适合：

- 你要接微信小程序 SDK
- 你要接服务号 SDK
- 你想把多 `appId` 场景的 SDK 初始化和服务工厂统一起来

如果你要接的是支付宝 / 微信支付主链路，请优先看 `getboot-payment`，不要把微信生态接入和支付接入混成一个入口。

## 前置条件

- 至少准备一组小程序或服务号的 `appId -> secret` 配置
- 如果希望服务号 token 落 Redis，请提供 `StringRedisTemplate`，常见做法是同时引入 `getboot-cache`

## 目录约定

- `api.properties`：对外稳定配置模型
- `api.miniapp` / `api.officialaccount`：业务侧注入的能力接口
- `infrastructure.miniapp` / `infrastructure.officialaccount`：微信 SDK 初始化实现
- `support.miniapp` / `support.officialaccount`：默认能力门面实现
- `infrastructure.autoconfigure`：Spring Boot 装配入口

## 配置示例

```yaml
getboot:
  wechat:
    mini-program:
      apps:
        wx-mini-app-id: wx-mini-secret        # key 为 appId，value 为 appSecret
    official-account:
      apps:
        wx-mp-app-id: wx-mp-secret            # key 为 appId，value 为 appSecret
```

## 默认 Bean

- `RedisTemplateWxRedisOps`：存在 `StringRedisTemplate` 时自动注册
- `WechatMiniProgramServiceFactory`：小程序服务工厂
- `WechatOfficialAccountServiceFactory`：服务号服务工厂
- `Map<String, WxMaService>`：默认小程序服务映射
- `WechatMiniProgramNativeServices`：默认小程序原生 SDK 门面，同时兼容注入 `WechatMiniProgramServices`
- `Map<String, WxMpService>`：默认服务号服务映射
- `WechatOfficialAccountNativeServices`：默认服务号原生 SDK 门面，同时兼容注入 `WechatOfficialAccountServices`

## 扩展点

- 业务项目统一使用 `getboot.wechat.*`
- 小程序配置统一使用 `getboot.wechat.mini-program.*`
- 服务号配置统一使用 `getboot.wechat.official-account.*`
- 业务侧优先注入 `WechatMiniProgramServices` / `WechatOfficialAccountServices` 这两个稳定门面，只依赖 `contains` / `appIds` 等能力
- 如果项目中存在 `StringRedisTemplate`，服务号能力会自动接入 Redis 做 token 存储；未提供 Redis 时会回退到内存存储
- 若需要自定义 SDK 初始化过程，可覆盖工厂 Bean、服务映射 Bean 或能力门面 Bean

## 已实现技术栈

- WeChat Mini Program
- WeChat Official Account
