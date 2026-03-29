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
package com.getboot.payment.infrastructure.alipay.trade.huabei;

import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.common.models.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCancelResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.payment.huabei.models.HuabeiConfig;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import com.alipay.easysdk.util.generic.models.AlipayOpenApiGenericResponse;
import com.getboot.payment.api.alipay.trade.huabei.AlipayHuabeiCreateRequest;
import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayRequestContext;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 支付宝花呗分期服务测试。
 *
 * @author qiheng
 */
class AlipayHuabeiServiceImplTest {

    @Test
    void shouldCreateHuabeiTrade() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.huabeiCreateResponse = new com.alipay.easysdk.payment.huabei.models.AlipayTradeCreateResponse();
        gateway.huabeiCreateResponse.code = "10000";
        gateway.huabeiCreateResponse.msg = "Success";
        gateway.huabeiCreateResponse.outTradeNo = "order-hb-001";
        gateway.huabeiCreateResponse.tradeNo = "trade-hb-001";

        AlipayHuabeiServiceImpl service = new AlipayHuabeiServiceImpl(paymentProperties(), gateway);
        var response = service.create(AlipayHuabeiCreateRequest.builder()
                .merchantOrderNo("order-hb-001")
                .subject("花呗分期订单")
                .description("花呗测试")
                .payerId("2088buyer")
                .amount(new BigDecimal("88.00"))
                .hbFqNum("3")
                .hbFqSellerPercent("0")
                .metadata(Map.of("sellerId", "2088seller"))
                .build());

        assertEquals("trade-hb-001", response.getPlatformOrderNo());
        assertEquals("CREATED", response.getStatus());
        assertEquals("3", gateway.lastHuabeiConfig.getHbFqNum());
        assertEquals("0", gateway.lastHuabeiConfig.getHbFqSellerPercent());
        assertEquals("2088seller", gateway.lastOptionalArgs.get("seller_id"));
    }

    private PaymentProperties paymentProperties() {
        PaymentProperties properties = new PaymentProperties();
        PaymentProperties.Alipay alipay = properties.getAlipay();
        alipay.setAppId("2026000000000001");
        alipay.setMerchantPrivateKey("merchant-private-key");
        alipay.setAlipayPublicKey("alipay-public-key");
        alipay.setNotifyUrl("https://demo.example.com/default-notify");
        return properties;
    }

    private static final class RecordingGateway implements AlipayGateway {

        private com.alipay.easysdk.payment.huabei.models.AlipayTradeCreateResponse huabeiCreateResponse;
        private HuabeiConfig lastHuabeiConfig;
        private Map<String, Object> lastOptionalArgs = new LinkedHashMap<>();

        @Override
        public com.alipay.easysdk.payment.huabei.models.AlipayTradeCreateResponse huabeiCreate(
                String subject,
                String outTradeNo,
                String totalAmount,
                String buyerId,
                HuabeiConfig extendParams,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            this.lastHuabeiConfig = extendParams;
            this.lastOptionalArgs = new LinkedHashMap<>(optionalArgs);
            return huabeiCreateResponse;
        }

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

        @Override
        public AlipayTradeQueryResponse query(
                String outTradeNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeQueryResponse();
        }

        @Override
        public AlipayTradeRefundResponse refund(
                String outTradeNo,
                String refundAmount,
                String notifyUrl,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeRefundResponse();
        }

        @Override
        public AlipayTradeFastpayRefundQueryResponse queryRefund(
                String outTradeNo,
                String outRequestNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeFastpayRefundQueryResponse();
        }

        @Override
        public AlipayTradeCloseResponse close(
                String outTradeNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeCloseResponse();
        }

        @Override
        public AlipayTradeCancelResponse cancel(
                String outTradeNo,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayTradeCancelResponse();
        }

        @Override
        public AlipayDataDataserviceBillDownloadurlQueryResponse downloadBill(
                String billType,
                String billDate,
                Map<String, Object> optionalArgs,
                AlipayRequestContext requestContext) {
            return new AlipayDataDataserviceBillDownloadurlQueryResponse();
        }

        @Override
        public boolean verifyNotify(Map<String, String> parameters) {
            return false;
        }

        @Override
        public AlipayOpenApiGenericResponse execute(
                String method,
                Map<String, String> textParams,
                Map<String, Object> bizParams) {
            return new AlipayOpenApiGenericResponse();
        }
    }
}
