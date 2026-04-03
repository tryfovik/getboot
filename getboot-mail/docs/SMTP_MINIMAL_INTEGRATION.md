# SMTP 最小接入示例

这份示例只覆盖当前 `getboot-mail` 已稳定承载的邮件能力：

- 单次邮件发送
- 模板变量替换
- 附件发送

## Maven 依赖

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-mail</artifactId>
</dependency>
```

## 配置

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
      auth: true
      ssl-enabled: true
```

- `default-content-type`
  可统一默认正文类型；配置为 `text/html` 时，未显式指定 `html` 与 `contentType` 的请求会按 HTML 正文发送

## 发送模板邮件

```java
@Service
public class InvoiceMailFacade {

    private final MailOperator mailOperator;

    public InvoiceMailFacade(MailOperator mailOperator) {
        this.mailOperator = mailOperator;
    }

    public void sendInvoice(String receiver, String orderNo) {
        MailSendRequest request = new MailSendRequest();
        request.setToAddresses(List.of(receiver));
        request.setSubjectTemplate("Invoice for order {{orderNo}}");
        request.setContentTemplate("<p>Your invoice for order <b>{{orderNo}}</b> is ready.</p>");
        request.setTemplateVariables(Map.of("orderNo", orderNo));
        request.setHtml(true);
        mailOperator.send(request);
    }
}
```

## 发送附件邮件

```java
@Service
public class ReportMailFacade {

    private final MailOperator mailOperator;

    public ReportMailFacade(MailOperator mailOperator) {
        this.mailOperator = mailOperator;
    }

    public void sendReport(String receiver, byte[] reportBytes) {
        MailAttachment attachment = new MailAttachment();
        attachment.setFilename("report.xlsx");
        attachment.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        attachment.setContent(reportBytes);

        MailSendRequest request = new MailSendRequest();
        request.setToAddresses(List.of(receiver));
        request.setSubjectTemplate("Weekly report");
        request.setContentTemplate("Please check the attached report.");
        request.setAttachments(List.of(attachment));

        mailOperator.send(request);
    }
}
```
