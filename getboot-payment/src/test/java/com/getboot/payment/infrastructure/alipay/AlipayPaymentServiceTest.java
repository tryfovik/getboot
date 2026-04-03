/*
 * Copyright (c) 2026 qiheng. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.getboot.payment.infrastructure.alipay;

import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCancelResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import com.alipay.easysdk.util.generic.models.AlipayOpenApiGenericResponse;
import com.getboot.payment.api.model.PaymentChannel;
import com.getboot.payment.api.model.PaymentMode;
import com.getboot.payment.api.model.PaymentNotifyType;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.request.PaymentCreateRequest;
import com.getboot.payment.api.request.PaymentNotifyRequest;
import com.getboot.payment.api.request.PaymentOrderQueryRequest;
import com.getboot.payment.api.request.PaymentRefundQueryRequest;
import com.getboot.payment.api.request.PaymentRefundRequest;
import com.getboot.payment.spi.alipay.AlipayRequestCustomizer;
import com.getboot.payment.spi.alipay.AlipayRequestOptions;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayRequestContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 支付宝统一支付服务测试。
 *
 * @author qiheng
 */
class AlipayPaymentServiceTest {

    /**
     * 验证 APP 支付会生成签名订单串。
     */
    @Test
    void shouldCreateAppOrder() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.appPayResponse = new AlipayTradeAppPayResponse();
        gateway.appPayResponse.body = "signed-order-string";

        AlipayPaymentService service = new AlipayPaymentService(paymentProperties(), gateway);
        var response = service.create(PaymentCreateRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .mode(PaymentMode.APP)
                .merchantOrderNo("order-001")
                .subject("订单支付")
                .description("测试订单")
                .payerId("2088buyer")
                .amount(new BigDecimal("12.34"))
                .notifyUrl("https://demo.example.com/notify")
                .metadata(Map.of(
                        "sellerId", "2088seller",
                        "appAuthToken", "app-auth-token"
                ))
                .build());

        assertEquals("signed-order-string", response.getPaymentData().get("orderString"));
        assertEquals("2088seller", gateway.lastOptionalArgs.get("seller_id"));
        assertEquals("2088buyer", gateway.lastOptionalArgs.get("buyer_id"));
        assertEquals("app-auth-token", gateway.lastRequestContext.appAuthToken());
        assertEquals("https://demo.example.com/notify", gateway.lastNotifyUrl);
    }

    /**
     * 验证扫码支付会返回二维码内容。
     */
    @Test
    void shouldCreateNativeOrder() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.precreateResponse = new AlipayTradePrecreateResponse();
        gateway.precreateResponse.code = "10000";
        gateway.precreateResponse.msg = "Success";
        gateway.precreateResponse.outTradeNo = "order-002";
        gateway.precreateResponse.qrCode = "https://qr.example.com/code";

        AlipayPaymentService service = new AlipayPaymentService(paymentProperties(), gateway);
        var response = service.create(PaymentCreateRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .mode(PaymentMode.NATIVE)
                .merchantOrderNo("order-002")
                .subject("扫码支付")
                .amount(new BigDecimal("10.00"))
                .build());

        assertEquals("https://qr.example.com/code", response.getQrCodeContent());
        assertEquals("order-002", response.getMerchantOrderNo());
    }

    /**
     * 验证订单查询结果映射。
     */
    @Test
    void shouldQueryOrder() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.queryResponse = new AlipayTradeQueryResponse();
        gateway.queryResponse.code = "10000";
        gateway.queryResponse.msg = "Success";
        gateway.queryResponse.outTradeNo = "order-003";
        gateway.queryResponse.tradeNo = "trade-003";
        gateway.queryResponse.tradeStatus = "TRADE_SUCCESS";
        gateway.queryResponse.buyerPayAmount = "88.88";
        gateway.queryResponse.payCurrency = "CNY";
        gateway.queryResponse.buyerUserId = "2088buyer";
        gateway.queryResponse.sendPayDate = "2026-03-29 10:00:00";

        AlipayPaymentService service = new AlipayPaymentService(paymentProperties(), gateway);
        var response = service.queryOrder(PaymentOrderQueryRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .mode(PaymentMode.PAGE)
                .merchantOrderNo("order-003")
                .platformOrderNo("trade-003")
                .build());

        assertEquals("TRADE_SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("88.88"), response.getPaidAmount());
        assertEquals("CNY", response.getCurrency());
        assertEquals("trade-003", response.getPlatformOrderNo());
        assertEquals("trade-003", gateway.lastOptionalArgs.get("trade_no"));
    }

    /**
     * 验证退款查询结果映射。
     */
    @Test
    void shouldQueryRefund() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.refundQueryResponse = new AlipayTradeFastpayRefundQueryResponse();
        gateway.refundQueryResponse.code = "10000";
        gateway.refundQueryResponse.msg = "Success";
        gateway.refundQueryResponse.outTradeNo = "order-004";
        gateway.refundQueryResponse.outRequestNo = "refund-004";
        gateway.refundQueryResponse.refundStatus = "REFUND_SUCCESS";
        gateway.refundQueryResponse.refundAmount = "8.00";
        gateway.refundQueryResponse.gmtRefundPay = "2026-03-29 11:00:00";

        AlipayPaymentService service = new AlipayPaymentService(paymentProperties(), gateway);
        var response = service.queryRefund(PaymentRefundQueryRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .merchantOrderNo("order-004")
                .refundRequestNo("refund-004")
                .build());

        assertEquals("REFUND_SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("8.00"), response.getRefundAmount());
    }

    /**
     * 验证支付通知解析与验签透传。
     */
    @Test
    void shouldParsePaymentNotify() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.verifyNotifyResult = true;
        AlipayPaymentService service = new AlipayPaymentService(paymentProperties(), gateway);

        var response = service.parseNotify(PaymentNotifyRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .notifyType(PaymentNotifyType.PAYMENT)
                .body("out_trade_no=order-005&trade_no=trade-005&trade_status=TRADE_SUCCESS&buyer_id=2088buyer&notify_time=2026-03-29+12%3A00%3A00&sign=mock")
                .build());

        assertTrue(response.isSuccess());
        assertEquals("order-005", response.getMerchantOrderNo());
        assertEquals("trade-005", response.getPlatformOrderNo());
        assertEquals("TRADE_SUCCESS", response.getStatus());
        assertEquals("2026-03-29 12:00:00", response.getEventTime());
        assertEquals("2088buyer", gateway.lastNotifyParameters.get("buyer_id"));
    }

    /**
     * 验证退款请求参数映射。
     */
    @Test
    void shouldRefundOrder() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.refundResponse = new AlipayTradeRefundResponse();
        gateway.refundResponse.code = "10000";
        gateway.refundResponse.msg = "Success";
        gateway.refundResponse.outTradeNo = "order-006";
        gateway.refundResponse.fundChange = "Y";
        gateway.refundResponse.refundFee = "5.00";

        AlipayPaymentService service = new AlipayPaymentService(paymentProperties(), gateway);
        var response = service.refund(PaymentRefundRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .merchantOrderNo("order-006")
                .platformOrderNo("trade-006")
                .refundRequestNo("refund-006")
                .refundAmount(new BigDecimal("5.00"))
                .reason("用户退款")
                .build());

        assertEquals("SUCCESS", response.getStatus());
        assertEquals("trade-006", gateway.lastOptionalArgs.get("trade_no"));
        assertEquals("refund-006", gateway.lastOptionalArgs.get("out_request_no"));
    }

    /**
     * 验证统一下单会应用 SPI 扩展参数。
     */
    @Test
    void shouldApplySpiCustomizerToUnifiedCreateRequest() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.appPayResponse = new AlipayTradeAppPayResponse();
        gateway.appPayResponse.body = "spi-order-string";

        AlipayRequestCustomizer customizer = new AlipayRequestCustomizer() {
            /**
             * 为统一下单请求注入测试扩展参数。
             *
             * @param request 创建请求
             * @param options 请求选项
             */
            @Override
            public void customizeCreate(PaymentCreateRequest request, AlipayRequestOptions options) {
                options.setNotifyUrl("https://spi.example.com/notify");
                options.setAppAuthToken("spi-token");
                options.putOptionalArg("scene_tag", "spi-create");
            }
        };

        AlipayPaymentService service = new AlipayPaymentService(paymentProperties(), gateway, List.of(customizer));
        var response = service.create(PaymentCreateRequest.builder()
                .channel(PaymentChannel.ALIPAY)
                .mode(PaymentMode.APP)
                .merchantOrderNo("order-spi")
                .subject("SPI测试订单")
                .amount(new BigDecimal("9.99"))
                .build());

        assertEquals("spi-order-string", response.getPaymentData().get("orderString"));
        assertEquals("https://spi.example.com/notify", gateway.lastNotifyUrl);
        assertEquals("spi-token", gateway.lastRequestContext.appAuthToken());
        assertEquals("spi-create", gateway.lastOptionalArgs.get("scene_tag"));
    }

    /**
     * 构造测试使用的支付宝配置。
     *
     * @return 支付宝配置
     */
    private PaymentProperties paymentProperties() {
        PaymentProperties properties = new PaymentProperties();
        PaymentProperties.Alipay alipay = properties.getAlipay();
        alipay.setAppId("2026000000000001");
        alipay.setMerchantPrivateKey("merchant-private-key");
        alipay.setAlipayPublicKey("alipay-public-key");
        alipay.setNotifyUrl("https://demo.example.com/default-notify");
        alipay.setReturnUrl("https://demo.example.com/return");
        return properties;
    }

    /**
     * 记录支付宝网关调用信息的测试桩。
     */
    private static final class RecordingGateway implements AlipayGateway {

        /**
         * APP 支付响应。
         */
        private AlipayTradeAppPayResponse appPayResponse;

        /**
         * 页面支付响应。
         */
        private AlipayTradePagePayResponse pagePayResponse;

        /**
         * WAP 支付响应。
         */
        private AlipayTradeWapPayResponse wapPayResponse;

        /**
         * 扫码预下单响应。
         */
        private AlipayTradePrecreateResponse precreateResponse;

        /**
         * 订单查询响应。
         */
        private AlipayTradeQueryResponse queryResponse;

        /**
         * 退款响应。
         */
        private AlipayTradeRefundResponse refundResponse;

        /**
         * 退款查询响应。
         */
        private AlipayTradeFastpayRefundQueryResponse refundQueryResponse;

        /**
         * 最近一次可选参数。
         */
        private Map<String, Object> lastOptionalArgs = new LinkedHashMap<>();

        /**
         * 最近一次调用上下文。
         */
        private AlipayRequestContext lastRequestContext;

        /**
         * 最近一次通知地址。
         */
        private String lastNotifyUrl;

        /**
         * 最近一次通知参数。
         */
        private Map<String, String> lastNotifyParameters = new LinkedHashMap<>();

        /**
         * 验签结果。
         */
        private boolean verifyNotifyResult;

        /**
         * 模拟 APP 支付调用。
         *
         * @param subject 订单标题
         * @param outTradeNo 商户订单号
         * @param totalAmount 订单金额
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return APP 支付响应
         */
        @Override
        public AlipayTradeAppPayResponse appPay(
                String subject,
                String outTradeNo,
                String totalAmount,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            this.lastNotifyUrl = notifyUrl;
            this.lastOptionalArgs = new LinkedHashMap<>(optionalArgs);
            this.lastRequestContext = requestContext;
            return appPayResponse;
        }

        /**
         * 模拟页面支付调用。
         *
         * @param subject 订单标题
         * @param outTradeNo 商户订单号
         * @param totalAmount 订单金额
         * @param returnUrl 返回地址
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 页面支付响应
         */
        @Override
        public AlipayTradePagePayResponse pagePay(
                String subject,
                String outTradeNo,
                String totalAmount,
                String returnUrl,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return pagePayResponse;
        }

        /**
         * 模拟 WAP 支付调用。
         *
         * @param subject 订单标题
         * @param outTradeNo 商户订单号
         * @param totalAmount 订单金额
         * @param quitUrl 退出地址
         * @param returnUrl 返回地址
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return WAP 支付响应
         */
        @Override
        public AlipayTradeWapPayResponse wapPay(
                String subject,
                String outTradeNo,
                String totalAmount,
                String quitUrl,
                String returnUrl,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return wapPayResponse;
        }

        /**
         * 模拟扫码预下单调用。
         *
         * @param subject 订单标题
         * @param outTradeNo 商户订单号
         * @param totalAmount 订单金额
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 扫码预下单响应
         */
        @Override
        public AlipayTradePrecreateResponse preCreate(
                String subject,
                String outTradeNo,
                String totalAmount,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return precreateResponse;
        }

        /**
         * 模拟订单查询调用。
         *
         * @param outTradeNo 商户订单号
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 订单查询响应
         */
        @Override
        public AlipayTradeQueryResponse query(
                String outTradeNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            this.lastOptionalArgs = new LinkedHashMap<>(optionalArgs);
            return queryResponse;
        }

        /**
         * 模拟退款调用。
         *
         * @param outTradeNo 商户订单号
         * @param refundAmount 退款金额
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 退款响应
         */
        @Override
        public AlipayTradeRefundResponse refund(
                String outTradeNo,
                String refundAmount,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            this.lastOptionalArgs = new LinkedHashMap<>(optionalArgs);
            return refundResponse;
        }

        /**
         * 模拟退款查询调用。
         *
         * @param outTradeNo 商户订单号
         * @param outRequestNo 退款请求号
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 退款查询响应
         */
        @Override
        public AlipayTradeFastpayRefundQueryResponse queryRefund(
                String outTradeNo,
                String outRequestNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return refundQueryResponse;
        }

        /**
         * 模拟关单调用。
         *
         * @param outTradeNo 商户订单号
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 关单响应
         */
        @Override
        public AlipayTradeCloseResponse close(
                String outTradeNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeCloseResponse();
        }

        /**
         * 模拟撤销调用。
         *
         * @param outTradeNo 商户订单号
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 撤销响应
         */
        @Override
        public AlipayTradeCancelResponse cancel(
                String outTradeNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeCancelResponse();
        }

        /**
         * 模拟账单下载地址查询。
         *
         * @param billType 账单类型
         * @param billDate 账单日期
         * @param optionalArgs 可选参数
         * @param requestContext 调用上下文
         * @return 账单响应
         */
        @Override
        public AlipayDataDataserviceBillDownloadurlQueryResponse downloadBill(
                String billType,
                String billDate,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayDataDataserviceBillDownloadurlQueryResponse();
        }

        /**
         * 模拟通知验签。
         *
         * @param parameters 通知参数
         * @return 验签结果
         */
        @Override
        public boolean verifyNotify(Map<String, String> parameters) {
            this.lastNotifyParameters = new LinkedHashMap<>(parameters);
            return verifyNotifyResult;
        }

        /**
         * 模拟泛化调用。
         *
         * @param method 方法名
         * @param textParams 文本参数
         * @param bizParams 业务参数
         * @return 泛化响应
         */
        @Override
        public AlipayOpenApiGenericResponse execute(
                String method,
                Map<String, String> textParams,
                Map<String, Object> bizParams) {
            return new AlipayOpenApiGenericResponse();
        }
    }
}
