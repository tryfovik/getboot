# 支付宝暂不纳入清单

这份清单记录当前不直接沉淀进 `getboot-payment` 稳定接口的支付宝能力。

## 暂不纳入

- 资金授权、代扣签约、周期扣款：更适合作为独立支付协议/签约子树，而不是继续膨胀统一交易主链路。
- 营销、会员、生活号、安全文本风控等非交易核心能力：后续如确有沉淀价值，再拆独立子树。
- 分账类支付宝能力：当前模块尚未提供稳定抽象。
- 复杂嵌套 `biz_content` 长尾参数：优先通过 `AlipayApiService` 兜底，不强行塞进统一请求模型。

## 当前建议

- 跨渠道单笔交易优先走 `PaymentService`
- 支付宝账单/撤销走 `AlipayTradeService`
- 支付宝条码支付走 `AlipayFaceToFaceService`
- 支付宝花呗分期走 `AlipayHuabeiService`
- 支付宝转账、转账通知、账户查询、电子回单走 `AlipaySettlementService`
- 复杂定制参数优先走 `AlipayRequestCustomizer`
- 其余长尾 OpenAPI 先走 `AlipayApiService`
