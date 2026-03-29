# 支付宝最小接入示例

这份示例只覆盖当前 `getboot-payment` 已稳定承载的支付宝能力：

- 单笔交易主链路：下单、退款、查单、查退款、关单、异步通知验签
- 交易增强：账单下载地址、交易撤销、条码支付、花呗分期
- 结算能力：单笔转账、转账查询、转账通知解析、账户余额查询、电子回单申请与查询
- OpenAPI 兜底：长尾接口直接调用
- 请求扩展 SPI：统一覆盖上下文与扩展参数

## Maven 依赖

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-payment</artifactId>
</dependency>
```

## 配置

### 公钥模式

```yaml
getboot:
  payment:
    enabled: true
    alipay:
      enabled: true
      app-id: 2026000000000001
      merchant-private-key: your-private-key
      alipay-public-key: your-alipay-public-key
      notify-url: https://demo.example.com/payment/alipay/notify
      return-url: https://demo.example.com/payment/alipay/return
```

### 证书模式

```yaml
getboot:
  payment:
    enabled: true
    alipay:
      enabled: true
      app-id: 2026000000000001
      merchant-private-key: your-private-key
      merchant-cert-path: classpath:payment/alipay/appCertPublicKey.crt
      alipay-cert-path: classpath:payment/alipay/alipayCertPublicKey_RSA2.crt
      alipay-root-cert-path: classpath:payment/alipay/alipayRootCert.crt
      notify-url: https://demo.example.com/payment/alipay/notify
      return-url: https://demo.example.com/payment/alipay/return
```

## 下单

```java
@Service
public class AlipayOrderFacade {

    private final PaymentServiceRegistry paymentServiceRegistry;

    public AlipayOrderFacade(PaymentServiceRegistry paymentServiceRegistry) {
        this.paymentServiceRegistry = paymentServiceRegistry;
    }

    public String createPageForm(String orderNo, BigDecimal amount) {
        PaymentService paymentService = paymentServiceRegistry.getRequired(PaymentChannel.ALIPAY);
        PaymentCreateResponse response = paymentService.create(PaymentCreateRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .mode(PaymentMode.PAGE)
                .merchantOrderNo(orderNo)
                .subject("订单支付")
                .description("演示订单")
                .amount(amount)
                .build());
        return response.getPaymentData().get("form");
    }

    public String createAppOrderString(String orderNo, BigDecimal amount) {
        PaymentService paymentService = paymentServiceRegistry.getRequired(PaymentChannel.ALIPAY);
        PaymentCreateResponse response = paymentService.create(PaymentCreateRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .mode(PaymentMode.APP)
                .merchantOrderNo(orderNo)
                .subject("订单支付")
                .amount(amount)
                .build());
        return response.getPaymentData().get("orderString");
    }

    public String createNativeQrCode(String orderNo, BigDecimal amount) {
        PaymentService paymentService = paymentServiceRegistry.getRequired(PaymentChannel.ALIPAY);
        PaymentCreateResponse response = paymentService.create(PaymentCreateRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .mode(PaymentMode.NATIVE)
                .merchantOrderNo(orderNo)
                .subject("订单支付")
                .amount(amount)
                .build());
        return response.getQrCodeContent();
    }
}
```

说明：

- `PAGE / WAP` 返回值放在 `paymentData.form`
- `APP` 返回值放在 `paymentData.orderString`
- `NATIVE` 返回值放在 `qrCodeContent`

## 退款与查询

```java
@Service
public class AlipayAfterSaleFacade {

    private final PaymentServiceRegistry paymentServiceRegistry;

    public AlipayAfterSaleFacade(PaymentServiceRegistry paymentServiceRegistry) {
        this.paymentServiceRegistry = paymentServiceRegistry;
    }

    public void refund(String orderNo, String refundNo, BigDecimal amount) {
        PaymentService paymentService = paymentServiceRegistry.getRequired(PaymentChannel.ALIPAY);
        paymentService.refund(PaymentRefundRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .merchantOrderNo(orderNo)
                .refundRequestNo(refundNo)
                .refundAmount(amount)
                .reason("用户退款")
                .build());
    }

    public PaymentOrderQueryResponse queryOrder(String orderNo) {
        PaymentService paymentService = paymentServiceRegistry.getRequired(PaymentChannel.ALIPAY);
        return paymentService.queryOrder(PaymentOrderQueryRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .merchantOrderNo(orderNo)
                .build());
    }

    public PaymentRefundQueryResponse queryRefund(String orderNo, String refundNo) {
        PaymentService paymentService = paymentServiceRegistry.getRequired(PaymentChannel.ALIPAY);
        return paymentService.queryRefund(PaymentRefundQueryRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .merchantOrderNo(orderNo)
                .refundRequestNo(refundNo)
                .build());
    }
}
```

## 异步通知

```java
@RestController
@RequestMapping("/payment/alipay")
public class AlipayNotifyController {

    private final PaymentServiceRegistry paymentServiceRegistry;

    public AlipayNotifyController(PaymentServiceRegistry paymentServiceRegistry) {
        this.paymentServiceRegistry = paymentServiceRegistry;
    }

    @PostMapping(
            value = "/notify",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String notify(@RequestBody String body) {
        PaymentService paymentService = paymentServiceRegistry.getRequired(PaymentChannel.ALIPAY);
        PaymentNotifyResponse response = paymentService.parseNotify(PaymentNotifyRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .notifyType(PaymentNotifyType.PAYMENT)
                .body(body)
                .build());
        return response.isSuccess() ? "success" : "failure";
    }
}
```

## 交易增强

```java
@Service
public class AlipayTradeFacade {

    private final AlipayTradeService alipayTradeService;

    public AlipayTradeFacade(AlipayTradeService alipayTradeService) {
        this.alipayTradeService = alipayTradeService;
    }

    public String downloadBill(String billDate) {
        return alipayTradeService.downloadBill(AlipayTradeBillRequest.builder()
                        .billType("trade")
                        .billDate(billDate)
                        .build())
                .getBillDownloadUrl();
    }
}
```

## 条码支付

```java
@Service
public class AlipayFaceToFaceFacade {

    private final AlipayFaceToFaceService alipayFaceToFaceService;

    public AlipayFaceToFaceFacade(AlipayFaceToFaceService alipayFaceToFaceService) {
        this.alipayFaceToFaceService = alipayFaceToFaceService;
    }

    public String pay(String orderNo, String authCode, BigDecimal amount) {
        return alipayFaceToFaceService.pay(AlipayFaceToFacePayRequest.builder()
                        .merchantOrderNo(orderNo)
                        .subject("门店收款")
                        .authCode(authCode)
                        .amount(amount)
                        .build())
                .getPlatformOrderNo();
    }
}
```

## 花呗分期

```java
@Service
public class AlipayHuabeiFacade {

    private final AlipayHuabeiService alipayHuabeiService;

    public AlipayHuabeiFacade(AlipayHuabeiService alipayHuabeiService) {
        this.alipayHuabeiService = alipayHuabeiService;
    }

    public String create(String orderNo, String buyerId, BigDecimal amount) {
        return alipayHuabeiService.create(AlipayHuabeiCreateRequest.builder()
                        .merchantOrderNo(orderNo)
                        .subject("花呗分期订单")
                        .payerId(buyerId)
                        .amount(amount)
                        .hbFqNum("3")
                        .hbFqSellerPercent("0")
                        .build())
                .getPlatformOrderNo();
    }
}
```

## OpenAPI 兜底

```java
@Service
public class AlipayOpenApiFacade {

    private final AlipayApiService alipayApiService;

    public AlipayOpenApiFacade(AlipayApiService alipayApiService) {
        this.alipayApiService = alipayApiService;
    }

    public String queryByOpenApi(String orderNo) {
        return alipayApiService.execute(AlipayApiRequest.builder()
                        .method("alipay.trade.query")
                        .bizParams(Map.of("out_trade_no", orderNo))
                        .build())
                .getHttpBody();
    }
}
```

## 转账与账户查询

```java
@Service
public class AlipaySettlementFacade {

    private final AlipaySettlementService alipaySettlementService;

    public AlipaySettlementFacade(AlipaySettlementService alipaySettlementService) {
        this.alipaySettlementService = alipaySettlementService;
    }

    public String transfer(String requestNo, BigDecimal amount, String alipayUserId) {
        return alipaySettlementService.transfer(AlipayTransferRequest.builder()
                        .transferRequestNo(requestNo)
                        .amount(amount)
                        .orderTitle("商户转账")
                        .payeeIdentity(alipayUserId)
                        .payeeIdentityType("ALIPAY_USER_ID")
                        .build())
                .getPlatformTransferOrderNo();
    }

    public BigDecimal queryAvailableAmount(String alipayUserId) {
        return alipaySettlementService.queryAccount(AlipayAccountQueryRequest.builder()
                        .alipayUserId(alipayUserId)
                        .build())
                .getAvailableAmount();
    }

    public String applyReceipt(String payFundOrderId, String alipayUserId) {
        return alipaySettlementService.applyElectronicReceipt(AlipayElectronicReceiptApplyRequest.builder()
                        .billUserId(alipayUserId)
                        .type("FUND_DETAIL")
                        .key(payFundOrderId)
                        .build())
                .getFileId();
    }

    public String queryReceiptDownloadUrl(String fileId) {
        return alipaySettlementService.queryElectronicReceipt(AlipayElectronicReceiptQueryRequest.builder()
                        .fileId(fileId)
                        .build())
                .getDownloadUrl();
    }
}
```

## 转账通知

```java
@RestController
@RequestMapping("/payment/alipay/transfer")
public class AlipayTransferNotifyController {

    private final AlipaySettlementService alipaySettlementService;

    public AlipayTransferNotifyController(AlipaySettlementService alipaySettlementService) {
        this.alipaySettlementService = alipaySettlementService;
    }

    @PostMapping(
            value = "/notify",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_PLAIN_VALUE
    )
    public String notify(@RequestBody String body) {
        var response = alipaySettlementService.parseTransferNotify(AlipayTransferNotifyRequest.builder()
                .body(body)
                .build());
        return response.isSuccess() ? "success" : "failure";
    }
}
```

## 元数据约定

统一主链路当前约定的支付宝扩展参数如下：

- 通用上下文：`appAuthToken`、`authToken`、`route`
- 下单扩展：`quitUrl`、`timeoutExpress`、`passbackParams`、`sellerId`、`storeId`、`operatorId`、`terminalId`、`disablePayChannels`、`enablePayChannels`、`serviceProviderId`
- 退款扩展：`storeId`、`operatorId`、`terminalId`

如果业务需要更复杂的嵌套 `biz_content` 参数，优先通过 `AlipayApiService` 兜底。

## SPI 扩展

如果业务需要统一注入服务商上下文、特殊通知地址、额外业务参数，可直接实现 `AlipayRequestCustomizer`：

```java
@Component
public class DemoAlipayRequestCustomizer implements AlipayRequestCustomizer {

    @Override
    public void customizeCreate(PaymentCreateRequest request, AlipayRequestOptions options) {
        options.setAppAuthToken("app-auth-token");
        options.putOptionalArg("extend_params", Map.of("sys_service_provider_id", "2088xxxx"));
    }

    @Override
    public void customizeDownloadBill(AlipayTradeBillRequest request, AlipayRequestOptions options) {
        options.setRoute("https://test-gateway.example.com");
    }

    @Override
    public void customizeApplyElectronicReceipt(
            AlipayElectronicReceiptApplyRequest request,
            AlipayRequestOptions options) {
        options.putOptionalArg("biz_scene", "DIRECT_TRANSFER");
    }
}
```

这层 SPI 适合做：

- ISV 代调用 token 注入
- 特殊联调路由
- 统一扩展业务字段
- 不想暴露给业务方的渠道细节收口
