# getboot-mail

邮件 starter，当前第一版先把最常见、最适合统一的邮件发送能力收口：

- 提供统一 `MailOperator` 门面，覆盖模板变量渲染、附件发送和 SMTP 发送
- 收口默认发件人、默认编码、默认正文类型和 SMTP 连接配置
- 先内置标准 `SMTP` 实现，避免业务代码直接散落 `JavaMailSender`、`MimeMessageHelper` 和模板替换逻辑

## 作用

适合这几类场景：

- 业务里需要统一发送注册通知、告警邮件、账单邮件这类 SMTP 邮件
- 不想在每个项目里重复写主题模板、正文模板、附件拼装和收件人组装
- 想把 SMTP 主机、端口、账号、默认发件人和超时统一放到稳定配置前缀里

## 接入方式

业务项目继承父 `pom` 后，引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-mail</artifactId>
</dependency>
```

当前第一版只内置 `SMTP` 实现，配置好主机和账号后即可直接使用。

## 前置条件

- 当前没有强制配套 `getboot-*` 模块
- 需要准备可访问的 SMTP 服务和有效账号

## 目录约定

- `api.*`
  对外稳定能力入口、请求模型、响应模型和配置
- `spi`
  邮件模板渲染扩展点
- `support`
  默认模板渲染和邮件辅助方法
- `infrastructure.smtp.*`
  SMTP 发送实现与自动配置

## 配置示例

```yaml
getboot:
  mail:
    enabled: true
    type: smtp
    default-from: no-reply@example.com
    default-content-type: text/html;charset=UTF-8
    smtp:
      enabled: true
      host: smtp.example.com
      port: 465
      username: no-reply@example.com
      password: your-password
      protocol: smtp
      auth: true
      starttls-enabled: false
      ssl-enabled: true
      connection-timeout: 5000
      timeout: 5000
      write-timeout: 5000
      properties:
        mail.debug: false
```

- `type`
  当前实现类型，第一版固定为 `smtp`
- `default-from`
  默认发件人，单次请求未指定时使用
- `default-content-type`
  默认正文内容类型，单次请求未指定 `contentType` 与 `html` 标记时使用；当前首版主要用于决定按 plain text 还是 HTML 发送
- `smtp.properties`
  额外透传到 JavaMail Session 的属性

## 使用示例

```java
@Service
public class RegisterMailFacade {

    private final MailOperator mailOperator;

    public RegisterMailFacade(MailOperator mailOperator) {
        this.mailOperator = mailOperator;
    }

    public void sendWelcomeMail(String receiver, String userName) {
        MailSendRequest request = new MailSendRequest();
        request.setToAddresses(List.of(receiver));
        request.setSubjectTemplate("Welcome, {{userName}}");
        request.setContentTemplate("<h1>Hello {{userName}}</h1><p>Your account is ready.</p>");
        request.setTemplateVariables(Map.of("userName", userName));
        request.setHtml(true);
        mailOperator.send(request);
    }
}
```

## 默认 Bean

- `MailTemplateRenderer`
  默认实现为 `DefaultMailTemplateRenderer`
- `JavaMailSender`
  当 `getboot.mail.smtp.host` 存在时自动注册
- `MailOperator`
  当容器内存在 `JavaMailSender` 时，默认实现为 `SmtpMailOperator`

## 扩展点

- 可注册 `MailTemplateRenderer` Bean，自定义主题和正文模板渲染规则
- 可直接覆盖 `MailOperator` Bean，自定义供应商实现或发送编排逻辑

## 已实现技术栈

- `smtp`

## 最小示例

更多示例见：

- [docs/SMTP_MINIMAL_INTEGRATION.md](docs/SMTP_MINIMAL_INTEGRATION.md)

## 边界 / 补充文档

- 当前第一版只覆盖 SMTP 发送、模板变量替换和附件，不承接邮件投递回执、退信处理和营销编排
- 当前不内置阿里云邮件、腾讯云邮件等云厂商实现，后续如果复用价值足够高再补
- 配置示例可直接参考 [`getboot-mail.yml.example`](./src/main/resources/getboot-mail.yml.example)
- SMTP 最小接入示例见 [`docs/SMTP_MINIMAL_INTEGRATION.md`](./docs/SMTP_MINIMAL_INTEGRATION.md)
