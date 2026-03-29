# 支付宝能力矩阵

## 已纳入

| 能力 | 入口 | 状态 | 说明 |
| --- | --- | --- | --- |
| App 支付 | `PaymentService#create` + `APP` | 已支持 | 返回 `orderString` |
| 电脑网站支付 | `PaymentService#create` + `PAGE` | 已支持 | 返回可直接渲染提交的 HTML 表单 |
| 手机网站支付 | `PaymentService#create` + `WAP` | 已支持 | 返回可直接渲染提交的 HTML 表单 |
| 扫码预下单 | `PaymentService#create` + `NATIVE` | 已支持 | 基于 `alipay.trade.precreate` 返回二维码内容 |
| 当面付条码支付 | `AlipayFaceToFaceService#pay` | 已支持 | 基于 `alipay.trade.pay`，适合线下收银台场景 |
| 花呗分期交易创建 | `AlipayHuabeiService#create` | 已支持 | 基于官方 `Payment.Huabei().create` |
| 单笔转账 | `AlipaySettlementService#transfer` | 已支持 | 基于 `alipay.fund.trans.uni.transfer` |
| 转账查询 | `AlipaySettlementService#queryTransfer` | 已支持 | 基于 `alipay.fund.trans.common.query` |
| 转账异步通知验签 | `AlipaySettlementService#parseTransferNotify` | 已支持 | 传入原始 `form-urlencoded` 报文 |
| 账户余额查询 | `AlipaySettlementService#queryAccount` | 已支持 | 基于 `alipay.fund.account.query` |
| 电子回单申请 | `AlipaySettlementService#applyElectronicReceipt` | 已支持 | 基于 `alipay.data.bill.ereceipt.apply` |
| 电子回单查询 | `AlipaySettlementService#queryElectronicReceipt` | 已支持 | 基于 `alipay.data.bill.ereceipt.query` |
| 退款 | `PaymentService#refund` | 已支持 | 支持 `refundRequestNo / reason / notifyUrl` |
| 查单 | `PaymentService#queryOrder` | 已支持 | 以商户订单号为主 |
| 查退款 | `PaymentService#queryRefund` | 已支持 | 需携带原商户订单号与退款请求号 |
| 关单 | `PaymentService#close` | 已支持 | 基于 `alipay.trade.close` |
| 支付结果通知验签 | `PaymentService#parseNotify` | 已支持 | 传入原始 `form-urlencoded` 报文 |
| 账单下载地址 | `AlipayTradeService#downloadBill` | 已支持 | 基于 `alipay.data.dataservice.bill.downloadurl.query` |
| 交易撤销 | `AlipayTradeService#cancel` | 已支持 | 基于 `alipay.trade.cancel` |
| OpenAPI 兜底调用 | `AlipayApiService#execute` | 已支持 | 用于尚未沉淀为强类型 API 的长尾接口 |
| 请求扩展 SPI | `spi.alipay.AlipayRequestCustomizer` | 已支持 | 用于覆盖上下文、通知地址与扩展业务参数 |

## 当前边界

| 范围 | 处理方式 |
| --- | --- |
| `PaymentService` | 只承载跨渠道单笔交易主链路 |
| `api.alipay.trade.*` | 承载撤销、账单、当面付、花呗分期等支付宝专属交易能力 |
| `api.alipay.settlement.*` | 承载支付宝转账、资金通知、账户查询与电子回单等资金能力 |
| `AlipayApiService` | 承载暂不适合立即稳定抽象的长尾 OpenAPI |
| `spi.alipay.*` | 承载外部业务的支付宝请求扩展点 |
