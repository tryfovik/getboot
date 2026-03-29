# 微信支付能力矩阵

这份文档的目标不是罗列 SDK 全部接口，而是回答两个问题：

- `getboot-payment` 当前已经把哪些微信支付能力沉淀成稳定入口
- 业务代码应该优先从哪个服务入口进入

## 1. 已纳入 `getboot-payment`

### 1.1 支付产品

- `JSAPI支付 / 小程序支付 / APP支付 / H5支付 / Native支付`
  - 稳定入口：`PaymentService`
- `订单退款 / 查询订单 / 查询退款 / 关单 / 回调验签`
  - 稳定入口：`PaymentService`
- `合单支付`
  - 稳定入口：`api.wechatpay.combine.*`
- `下载账单 / 异常退款`
  - 稳定入口：`api.wechatpay.trade.*`

### 1.2 运营工具

- `商家转账`
  - 稳定入口：`api.wechatpay.settlement.WechatPaySettlementService`
- `微信支付分`
  - 稳定入口：`api.wechatpay.operation.payscore.*`
- `微信支付分停车服务`
  - 统一入口：`WechatPayOperationService.payScoreParkingService()`
  - 官方 SDK：`service.wexinpayscoreparking.*`
- `代金券 / 商家券 / 支付有礼 / 委托营销`
  - 统一入口：`WechatPayOperationService`
  - 官方 SDK：`service.cashcoupons.*`、`service.merchantexclusivecoupon.*`、`service.giftactivity.*`、`service.marketingbankpackages.*`
- `小程序发券插件 / H5发券`
  - 稳定入口：`api.wechatpay.operation.coupon.*`
- `智慧商圈`
  - 稳定入口：`api.wechatpay.operation.businesscircle.*`

### 1.3 扩展工具

- `分账 / 电商分账`
  - 稳定入口：`api.wechatpay.settlement.WechatPaySettlementService`
- `消费者投诉2.0`
  - 稳定入口：`api.wechatpay.extension.complaint.*`

### 1.4 安全工具

- `平台证书`
  - 稳定入口：`api.wechatpay.security.WechatPaySecurityService`
- `平台证书公钥加密敏感字段`
  - 稳定入口：`api.wechatpay.security.WechatPaySecurityService`

### 1.5 请求扩展

- `统一下单 / 合单 / 交易增强 请求扩展 SPI`
  - 稳定入口：`spi.wechatpay.WechatPayRequestCustomizer`

## 2. 当前推荐调用方式

- 业务侧优先调用稳定子服务：
  - `operation.coupon.*`
  - `operation.payscore.*`
  - `operation.businesscircle.*`
  - `extension.complaint.*`
- 官方 SDK 已经高度匹配业务语义时，保留聚合入口：
  - `WechatPayOperationService`
  - `WechatPaySettlementService`
  - `WechatPaySecurityService`
- 需要统一覆盖通知地址、AppId、付款人标识或补充 Map 请求体字段时：
  - `spi.wechatpay.WechatPayRequestCustomizer`
- 只有官方 SDK 尚未覆盖、模块当前也还没做稳定抽象时，再使用 `WechatPayApiService`

## 3. 当前未纳入稳定子树

以下能力还没有进入稳定 API，详见 [WECHAT_PAY_OUT_OF_SCOPE.md](WECHAT_PAY_OUT_OF_SCOPE.md)：

- `付款码支付（V2）`
- `现金红包（V2）`
- `清关报关（V2）`
- `刷脸支付`
- `医保支付`
- `支付即服务`
- `微信支付公钥`

其中 `支付即服务 / 微信支付公钥` 当前结论仍然是继续留在 out-of-scope，不新增独立稳定子树。

## 4. 模块内分层结论

当前 `getboot-payment` 的分层已经固定为：

- 通用支付层：`api.model / api.request / api.response / api.service`
- 微信专属层：`api.wechatpay.combine / trade / operation / settlement / security / extension`
- 外部扩展层：`spi.wechatpay.*`
- 技术实现层：`infrastructure.wechatpay.*`
- 内部支撑层：`support.wechatpay.*`

这层拆分的目标是：统一主链路不膨胀，微信专属能力不散落，`support` 不再额外扩成业务静态门面。
