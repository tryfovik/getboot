# getboot-sms

短信发送 starter，当前第一版先把业务里最常见、最适合统一的那层收口：

- 提供统一 `SmsOperator` 门面，覆盖单发、批量发送和验证码短信
- 收敛签名路由、验证码模板配置和模板变量序列化
- 先内置阿里云短信实现，避免业务代码直接耦合 SDK

## 作用

适合这几类场景：

- 业务里反复发送验证码、登录提醒、通知类短信
- 不想在每个项目里重复拼 `signName`、`templateCode`、模板变量 JSON
- 想把验证码短信的模板、签名和变量命名统一收口到配置层

## 接入方式

业务项目继承父 `pom` 后，引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-sms</artifactId>
</dependency>
```

当前第一版只内置阿里云短信实现，配置好 AccessKey 和模板即可使用。

## 前置条件

- 当前没有强制配套 `getboot-*` 模块
- 需要准备可用的阿里云短信签名、模板和 AccessKey

## 目录约定

- `api.*`
  对外稳定能力入口、请求模型、响应模型和配置
- `spi`
  供应商客户端、签名路由、模板参数序列化扩展点
- `support`
  默认签名解析、验证码模板组装和统一辅助方法
- `infrastructure.aliyun.*`
  阿里云短信 SDK 适配与自动装配

## 配置示例

```yaml
getboot:
  sms:
    enabled: true
    type: aliyun
    default-sign-name: GetBoot
    scene-sign-names:
      notice: NoticeSign
    verification-scenes:
      login:
        sign-name: VerifySign
        template-code: SMS_LOGIN_001
        code-param-name: code
        expire-minutes-param-name: expireMinutes
        extra-params:
          product: getboot
    aliyun:
      enabled: true
      endpoint: dysmsapi.aliyuncs.com
      region-id: cn-hangzhou
      access-key-id: your-access-key-id
      access-key-secret: your-access-key-secret
      connect-timeout: 5000
      read-timeout: 5000
```

- `default-sign-name`
  没有显式指定签名，且 `scene-sign-names` 也没有命中时的默认签名
- `scene-sign-names`
  按业务场景路由签名，例如通知、营销、验证码场景走不同签名
- `verification-scenes`
  验证码短信模板配置，统一约定模板、签名和变量名
- `aliyun.endpoint`
  阿里云短信服务接入点，默认值与官方文档一致为 `dysmsapi.aliyuncs.com`

## 使用示例

```java
@Service
public class SmsApplicationService {

    private final SmsOperator smsOperator;

    public SmsApplicationService(SmsOperator smsOperator) {
        this.smsOperator = smsOperator;
    }

    public SmsSendResponse sendNotice(String phoneNumber) {
        SmsSendRequest request = new SmsSendRequest();
        request.setScene("notice");
        request.setPhoneNumber(phoneNumber);
        request.setTemplateCode("SMS_NOTICE_001");
        request.getTemplateParams().put("title", "GetBoot");
        return smsOperator.send(request);
    }

    public SmsSendResponse sendLoginCode(String phoneNumber, String code) {
        SmsVerificationCodeRequest request = new SmsVerificationCodeRequest();
        request.setScene("login");
        request.setPhoneNumber(phoneNumber);
        request.setCode(code);
        request.setExpireMinutes(5);
        return smsOperator.sendVerificationCode(request);
    }
}
```

## 默认 Bean

- `SmsSignResolver`
  默认实现为 `DefaultSmsSignResolver`
- `SmsTemplateParamSerializer`
  默认实现为 `FastjsonSmsTemplateParamSerializer`
- `com.aliyun.dysmsapi20170525.Client`
  当 `getboot.sms.aliyun.access-key-id/access-key-secret` 存在时自动注册
- `SmsProviderClient`
  当容器内存在阿里云短信 `Client` 时，默认实现为 `AliyunSmsProviderClient`
- `SmsOperator`
  当存在 `SmsProviderClient` 时，默认实现为 `DefaultSmsOperator`

## 扩展点

- 可注册 `SmsSignResolver` Bean，自定义场景签名路由
- 可注册 `SmsTemplateParamSerializer` Bean，自定义模板变量序列化
- 可注册 `SmsProviderClient` Bean，替换默认供应商实现

## 已实现技术栈

- `aliyun`

## 边界 / 补充文档

- 当前第一版只覆盖单发、批量发送和验证码模板场景，不承接营销短信编排、回执消费、发送状态轮询和供应商降级链路
- 当前没有内置自动重试。阿里云官方文档明确短信发送接口不支持幂等，超时后更适合先看回执再判断是否补发
- 当前只内置阿里云实现，腾讯云 / 华为云后续再补
- 配置示例可直接参考 [`getboot-sms.yml.example`](./src/main/resources/getboot-sms.yml.example)
