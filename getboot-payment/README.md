# getboot-payment

统一支付 starter。

## 作用

- 提供跨渠道统一支付主链路
- 提供支付宝和微信支付两套实现
- 提供渠道级交易增强、结算、安全和运营能力
- 提供请求定制 SPI，避免业务方直接散落 SDK 调用

## 接入方式

业务项目继承父 `pom` 后，按需引入：

```xml
<dependency>
    <groupId>com.dt</groupId>
    <artifactId>getboot-payment</artifactId>
</dependency>
```

推荐场景：

- 业务系统需要直接承接支付宝或微信支付下单、退款、查单、通知处理
- 你想保留统一支付主链路，但不想把多渠道 SDK 调用散落到业务代码里
- 你需要渠道增强能力，例如支付宝结算、微信合单、微信运营、安全、投诉能力

`getboot-payment` 独立承接支付能力，不要求同时引入 `getboot-wechat`。只有当你还需要小程序 / 服务号 SDK 时，才再额外引入 `getboot-wechat`。

## 前置条件

- 先准备支付渠道对应的商户配置、证书或密钥、回调地址
- 微信支付除 `api-v3-key` 外，部分运营场景还需要 `api-v2-key`
- 建议先看本 README，再按渠道进入最小接入文档

## 目录约定

- `api.model / request / response / service / registry`：跨渠道共性模型与主链路接口
- `api.alipay.*`：支付宝专属能力接口
- `api.wechatpay.*`：微信支付专属能力接口
- `spi.alipay`：支付宝请求定制扩展点
- `spi.wechatpay`：微信支付请求定制扩展点
- `support`：异常包装、注册中心和渠道共性辅助实现
- `infrastructure.alipay.*`：支付宝实现与自动装配
- `infrastructure.wechatpay.*`：微信支付实现与自动装配
- `infrastructure.autoconfigure`：支付模块自动配置入口

## 分层说明

- 通用支付主链路：通过 `PaymentService` 暴露单笔下单、退款、查单、查退款、关单、通知解析
- 支付宝专属能力：通过 `api.alipay.*` 承接交易增强、结算能力与 OpenAPI 兜底访问
- 微信专属能力：通过 `api.wechatpay.*` 承接合单、交易增强、运营、结算、安全、扩展能力

`PaymentService` 只承载跨渠道共性的单笔交易主链路，不继续膨胀渠道私有字段。

## 已提供能力

- 支付宝统一支付：`APP / PAGE / WAP / NATIVE`
- 支付宝交易增强：账单下载地址查询、交易撤销、当面付条码支付、花呗分期
- 支付宝结算能力：单笔转账、转账查询、转账通知解析、账户余额查询、电子回单申请与查询
- 支付宝开放接口兜底：`AlipayApiService`
- 支付宝请求扩展 SPI：`AlipayRequestCustomizer`
- 微信统一支付：`JSAPI / MINI_PROGRAM / APP / H5 / NATIVE`
- 微信交易增强：合单支付、异常退款、交易账单、资金账单
- 微信请求扩展 SPI：`WechatPayRequestCustomizer`
- 微信运营能力：发券插件、H5 发券、支付分、智慧商圈、支付有礼等
- 微信结算能力：商家转账、分账、电商分账
- 微信安全能力：平台证书下载、敏感字段加密
- 微信扩展能力：消费者投诉 2.0

## 配置示例

```yaml
getboot:
  payment:
    enabled: true
    alipay:
      enabled: true
      app-id: 2026000000000001
      protocol: https
      gateway-host: openapi.alipay.com
      sign-type: RSA2
      merchant-private-key: your-private-key
      alipay-public-key: your-alipay-public-key
      merchant-cert-path: classpath:payment/alipay/appCertPublicKey.crt
      alipay-cert-path: classpath:payment/alipay/alipayCertPublicKey_RSA2.crt
      alipay-root-cert-path: classpath:payment/alipay/alipayRootCert.crt
      notify-url: https://demo.example.com/payment/alipay/notify
      return-url: https://demo.example.com/payment/alipay/return
      sandbox: false
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

说明：

- 支付宝支持 `公钥模式` 与 `证书模式`，证书模式下 `merchant-cert-path / alipay-cert-path / alipay-root-cert-path` 需一起配置
- 支付宝 `gateway-host` 不填时按生产/沙箱自动推导
- 支付宝 `PAGE / WAP` 返回值是可直接渲染并提交的 HTML 表单，`APP` 返回值是订单串
- `api-v2-key` 只在发券插件、H5 发券、支付分 JSAPI 详情页等 V2 规则签名场景下需要
- `app-id` 为默认应用 ID，业务侧可按请求覆盖
- `private-key-location` 支持 `classpath:` 与 Spring 标准资源路径

## 默认 Bean

- `PaymentServiceRegistry`
- `PaymentService`
- `AlipayTradeService`
- `AlipayFaceToFaceService`
- `AlipayHuabeiService`
- `AlipaySettlementService`
- `AlipayApiService`
- `WechatPayCombinePaymentService`
- `WechatPayTradeService`
- `WechatPayOperationService`
- `WechatPaySettlementService`
- `WechatPaySecurityService`
- `WechatPayComplaintService`
- 微信官方 SDK service Bean：账单、商家转账、分账、代金券、商家券、支付有礼、委托营销、支付分停车、平台证书、文件上传

## 扩展点

- `AlipayRequestCustomizer`
- `WechatPayRequestCustomizer`
- `PaymentServiceRegistry` 允许业务按渠道获取稳定入口，不需要直接依赖底层实现类
- `PaymentService` 只覆盖跨渠道共性能力；渠道私有字段和能力继续放在各自 `api.alipay.*` / `api.wechatpay.*`
- 当前模块不会把所有 SDK 原生能力无差别平铺成统一接口，避免统一层语义失真

## 已实现技术栈

- Alipay SDK
- WeChat Pay Java SDK

## 最小示例

```java
@Service
public class DemoPaymentFacade {

    private final PaymentServiceRegistry paymentServiceRegistry;
    private final AlipayTradeService alipayTradeService;

    public DemoPaymentFacade(
            PaymentServiceRegistry paymentServiceRegistry,
            AlipayTradeService alipayTradeService) {
        this.paymentServiceRegistry = paymentServiceRegistry;
        this.alipayTradeService = alipayTradeService;
    }

    public PaymentCreateResponse createPagePay(String orderNo, BigDecimal amount) {
        PaymentService paymentService = paymentServiceRegistry.getRequired(PaymentChannel.ALIPAY);
        return paymentService.create(PaymentCreateRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .mode(PaymentMode.PAGE)
                .merchantOrderNo(orderNo)
                .subject("订单支付")
                .amount(amount)
                .build());
    }

    public String downloadTradeBillUrl(String billDate) {
        return alipayTradeService.downloadBill(AlipayTradeBillRequest.builder()
                        .billType("trade")
                        .billDate(billDate)
                        .build())
                .getBillDownloadUrl();
    }
}
```

更多示例见：

- [docs/ALIPAY_MINIMAL_INTEGRATION.md](docs/ALIPAY_MINIMAL_INTEGRATION.md)
- [docs/WECHAT_PAY_MINIMAL_INTEGRATION.md](docs/WECHAT_PAY_MINIMAL_INTEGRATION.md)

请求需要统一覆写时，可直接声明渠道级 `SPI` Bean：

```java
@Component
public class DemoWechatPayCustomizer implements WechatPayRequestCustomizer {

    @Override
    public void customizeCreate(PaymentCreateRequest request, WechatPayRequestOptions options) {
        options.setNotifyUrl("https://demo.example.com/payment/wechat/notify");
    }
}
```

## 补充文档

- 支付宝能力矩阵：[docs/ALIPAY_CAPABILITY_MATRIX.md](docs/ALIPAY_CAPABILITY_MATRIX.md)
- 支付宝暂不纳入清单：[docs/ALIPAY_OUT_OF_SCOPE.md](docs/ALIPAY_OUT_OF_SCOPE.md)
- 支付宝最小接入示例：[docs/ALIPAY_MINIMAL_INTEGRATION.md](docs/ALIPAY_MINIMAL_INTEGRATION.md)
- 微信支付能力矩阵：[docs/WECHAT_PAY_CAPABILITY_MATRIX.md](docs/WECHAT_PAY_CAPABILITY_MATRIX.md)
- 微信支付暂不纳入清单：[docs/WECHAT_PAY_OUT_OF_SCOPE.md](docs/WECHAT_PAY_OUT_OF_SCOPE.md)
- 微信支付最小接入示例：[docs/WECHAT_PAY_MINIMAL_INTEGRATION.md](docs/WECHAT_PAY_MINIMAL_INTEGRATION.md)
