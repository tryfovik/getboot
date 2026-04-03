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
package com.getboot.payment.infrastructure.alipay.trade.facetoface;

import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCancelResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePayResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import com.alipay.easysdk.util.generic.models.AlipayOpenApiGenericResponse;
import com.getboot.payment.api.alipay.trade.facetoface.AlipayFaceToFacePayRequest;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayRequestContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 支付宝当面付服务测试。
 *
 * @author qiheng
 */
class AlipayFaceToFaceServiceImplTest {

    /**
     * 验证当面付条码支付结果映射。
     */
    @Test
    void shouldPayByBarcode() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.facePayResponse = new AlipayTradePayResponse();
        gateway.facePayResponse.code = "10000";
        gateway.facePayResponse.msg = "Success";
        gateway.facePayResponse.outTradeNo = "order-face-001";
        gateway.facePayResponse.tradeNo = "trade-face-001";
        gateway.facePayResponse.buyerPayAmount = "15.20";
        gateway.facePayResponse.payCurrency = "CNY";
        gateway.facePayResponse.buyerUserId = "2088buyer";
        gateway.facePayResponse.gmtPayment = "2026-03-29 13:00:00";

        AlipayFaceToFaceServiceImpl service = new AlipayFaceToFaceServiceImpl(paymentProperties(), gateway);
        var response = service.pay(AlipayFaceToFacePayRequest.builder()
                .merchantOrderNo("order-face-001")
                .subject("门店收款")
                .description("当面付测试")
                .authCode("289821051157962364")
                .amount(new BigDecimal("15.20"))
                .metadata(Map.of("storeId", "store-001"))
                .build());

        assertEquals("trade-face-001", response.getPlatformOrderNo());
        assertEquals("TRADE_SUCCESS", response.getStatus());
        assertEquals(new BigDecimal("15.20"), response.getPaidAmount());
        assertEquals("store-001", gateway.lastOptionalArgs.get("store_id"));
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
        return properties;
    }

    /**
     * 记录当面付调用信息的测试网关。
     */
    private static final class RecordingGateway implements AlipayGateway {

        /**
         * 条码支付响应。
         */
        private AlipayTradePayResponse facePayResponse;

        /**
         * 最近一次可选参数。
         */
        private Map<String, Object> lastOptionalArgs = new LinkedHashMap<>();

        /**
         * 模拟当面付调用。
         */
        @Override
        public AlipayTradePayResponse facePay(
                String subject,
                String outTradeNo,
                String totalAmount,
                String authCode,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            this.lastOptionalArgs = new LinkedHashMap<>(optionalArgs);
            return facePayResponse;
        }

        /**
         * 模拟 APP 支付调用。
         */
        @Override
        public AlipayTradeAppPayResponse appPay(
                String subject,
                String outTradeNo,
                String totalAmount,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeAppPayResponse();
        }

        /**
         * 模拟页面支付调用。
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
            return new AlipayTradePagePayResponse();
        }

        /**
         * 模拟 WAP 支付调用。
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
            return new AlipayTradeWapPayResponse();
        }

        /**
         * 模拟预下单调用。
         */
        @Override
        public AlipayTradePrecreateResponse preCreate(
                String subject,
                String outTradeNo,
                String totalAmount,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradePrecreateResponse();
        }

        /**
         * 模拟订单查询调用。
         */
        @Override
        public AlipayTradeQueryResponse query(
                String outTradeNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeQueryResponse();
        }

        /**
         * 模拟退款调用。
         */
        @Override
        public AlipayTradeRefundResponse refund(
                String outTradeNo,
                String refundAmount,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeRefundResponse();
        }

        /**
         * 模拟退款查询调用。
         */
        @Override
        public AlipayTradeFastpayRefundQueryResponse queryRefund(
                String outTradeNo,
                String outRequestNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeFastpayRefundQueryResponse();
        }

        /**
         * 模拟关单调用。
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
         */
        @Override
        public boolean verifyNotify(Map<String, String> parameters) {
            return false;
        }

        /**
         * 模拟泛化调用。
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
