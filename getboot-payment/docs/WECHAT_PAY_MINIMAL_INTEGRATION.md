# 微信支付最小接入示例

这份文档只回答两个问题：

- 业务项目先引哪个模块
- 微信支付当前应该注入哪个 Bean

## Maven 依赖

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-payment</artifactId>
</dependency>
```

## 配置

```yaml
getboot:
  payment:
    enabled: true
    wechatpay:
      enabled: true
      app-id: wx1234567890
      merchant-id: 1900001234
      merchant-serial-number: 4A3B2C1D
      api-v3-key: your-api-v3-key
      api-v2-key: your-api-v2-key
      private-key-location: classpath:payment/wechat/apiclient_key.pem
      notify-url: https://demo.example.com/payment/wechat/notify
```

## 单笔支付

适用场景：公众号 / 小程序 / App / H5 / Native 单笔支付、退款、查单、关单、通知解析。

```java
@Service
public class DemoUnifiedPaymentFacade {

    private final PaymentServiceRegistry paymentServiceRegistry;

    public DemoUnifiedPaymentFacade(PaymentServiceRegistry paymentServiceRegistry) {
        this.paymentServiceRegistry = paymentServiceRegistry;
    }

    public PaymentCreateResponse createJsapi(String orderNo, String openId, BigDecimal amount) {
        PaymentService paymentService = paymentServiceRegistry.getRequired(PaymentChannel.WECHAT_PAY);
        return paymentService.create(PaymentCreateRequest.builder()
                .channel(PaymentChannel.WECHAT_PAY)
                .mode(PaymentMode.JSAPI)
                .merchantOrderNo(orderNo)
                .subject("订单支付")
                .payerId(openId)
                .amount(amount)
                .build());
    }
}
```

## 运营能力

适用场景：支付分、发券插件、H5 发券、智慧商圈，以及官方 SDK 已覆盖的运营 service 聚合入口。

```java
@Service
public class DemoWechatOperationFacade {

    private final WechatPayOperationService operationService;

    public DemoWechatOperationFacade(WechatPayOperationService operationService) {
        this.operationService = operationService;
    }

    public String buildCouponUrl() {
        return operationService.couponService()
                .buildH5Launch(WechatPayH5CouponLaunchRequest.builder()
                        .stockId("stock-001")
                        .outRequestNo("req-001")
                        .openId("openid-001")
                        .build())
                .getUrl();
    }

    public WechatPayJsapiBusinessViewRequest buildPayScoreDetailView() {
        return operationService.payScoreService()
                .buildJsapiOrderDetailView(WechatPayPayScoreDetailViewRequest.builder()
                        .serviceId("service-001")
                        .outOrderNo("order-001")
                        .build());
    }

    public Map<String, Object> queryBusinessCircleAuthorization() {
        return operationService.businessCircleService()
                .queryUserAuthorization(WechatPayBusinessCircleAuthorizationQueryRequest.builder()
                        .openId("openid-001")
                        .appId("wx1234567890")
                        .build());
    }
}
```

## 合单 / 交易增强 / 结算 / 安全 / 投诉

适用场景：微信专属能力，但不属于统一单笔支付主链路。

```java
@Service
public class DemoWechatSpecialFacade {

    private final WechatPayCombinePaymentService combinePaymentService;
    private final WechatPayTradeService tradeService;
    private final WechatPaySettlementService settlementService;
    private final WechatPaySecurityService securityService;
    private final WechatPayComplaintService complaintService;

    public DemoWechatSpecialFacade(
            WechatPayCombinePaymentService combinePaymentService,
            WechatPayTradeService tradeService,
            WechatPaySettlementService settlementService,
            WechatPaySecurityService securityService,
            WechatPayComplaintService complaintService) {
        this.combinePaymentService = combinePaymentService;
        this.tradeService = tradeService;
        this.settlementService = settlementService;
        this.securityService = securityService;
        this.complaintService = complaintService;
    }

    public void example() {
        settlementService.transferBatchService();
        securityService.downloadPlatformCertificates();
        complaintService.complete("2002018202211220000000000000", Map.of("completed", true));
    }
}
```

## 仍未做稳定抽象的能力

适用场景：官方 SDK 未覆盖，且模块当前还没有稳定子服务。

```java
@Service
public class DemoWechatOpenApiFacade {

    private final WechatPayApiService apiService;

    public DemoWechatOpenApiFacade(WechatPayApiService apiService) {
        this.apiService = apiService;
    }

    public Map<?, ?> getRaw(String path) {
        return apiService.get(path, Map.class);
    }
}
```

## 请求扩展 SPI

适用场景：统一覆盖通知地址、AppId、付款人标识，或给合单/异常退款这类 Map 请求补充长尾字段。

```java
@Component
public class DemoWechatPayCustomizer implements WechatPayRequestCustomizer {

    @Override
    public void customizeCreate(PaymentCreateRequest request, WechatPayRequestOptions options) {
        options.setNotifyUrl("https://demo.example.com/payment/wechat/notify");
    }

    @Override
    public void customizeCombineCreate(WechatPayCombineCreateRequest request, WechatPayRequestOptions options) {
        options.putExtraBody("combine_remark", "demo");
    }
}
```
