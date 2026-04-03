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
package com.getboot.payment.infrastructure.alipay.trade;

import com.alipay.easysdk.payment.common.models.AlipayDataDataserviceBillDownloadurlQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCancelResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeCloseResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeFastpayRefundQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.alipay.easysdk.payment.common.models.AlipayTradeRefundResponse;
import com.alipay.easysdk.payment.app.models.AlipayTradeAppPayResponse;
import com.alipay.easysdk.payment.facetoface.models.AlipayTradePrecreateResponse;
import com.alipay.easysdk.payment.huabei.models.HuabeiConfig;
import com.alipay.easysdk.payment.page.models.AlipayTradePagePayResponse;
import com.alipay.easysdk.payment.wap.models.AlipayTradeWapPayResponse;
import com.alipay.easysdk.util.generic.models.AlipayOpenApiGenericResponse;
import com.getboot.payment.api.alipay.trade.AlipayTradeBillRequest;
import com.getboot.payment.api.alipay.trade.AlipayTradeCancelRequest;
import com.getboot.payment.spi.alipay.AlipayRequestCustomizer;
import com.getboot.payment.spi.alipay.AlipayRequestOptions;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayRequestContext;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 支付宝交易增强服务测试。
 *
 * @author qiheng
 */
class AlipayTradeServiceImplTest {

    /**
     * 验证账单下载地址查询。
     */
    @Test
    void shouldDownloadBill() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.billResponse = new AlipayDataDataserviceBillDownloadurlQueryResponse();
        gateway.billResponse.code = "10000";
        gateway.billResponse.msg = "Success";
        gateway.billResponse.billDownloadUrl = "https://bill.example.com/trade.csv";

        AlipayTradeServiceImpl service = new AlipayTradeServiceImpl(gateway);
        var response = service.downloadBill(AlipayTradeBillRequest.builder()
                .billType("trade")
                .billDate("2026-03-29")
                .metadata(Map.of("appAuthToken", "token-001"))
                .build());

        assertEquals("https://bill.example.com/trade.csv", response.getBillDownloadUrl());
        assertEquals("token-001", gateway.lastRequestContext.appAuthToken());
    }

    /**
     * 验证撤销交易结果映射。
     */
    @Test
    void shouldCancelTrade() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.cancelResponse = new AlipayTradeCancelResponse();
        gateway.cancelResponse.code = "10000";
        gateway.cancelResponse.msg = "Success";
        gateway.cancelResponse.outTradeNo = "order-007";
        gateway.cancelResponse.tradeNo = "trade-007";
        gateway.cancelResponse.retryFlag = "N";
        gateway.cancelResponse.action = "close";

        AlipayTradeServiceImpl service = new AlipayTradeServiceImpl(gateway);
        var response = service.cancel(AlipayTradeCancelRequest.builder()
                .merchantOrderNo("order-007")
                .build());

        assertEquals("trade-007", response.getPlatformOrderNo());
        assertEquals("N", response.getRetryFlag());
        assertEquals("close", response.getAction());
    }

    /**
     * 验证交易请求会应用 SPI 扩展参数。
     */
    @Test
    void shouldApplySpiCustomizerToTradeRequest() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.billResponse = new AlipayDataDataserviceBillDownloadurlQueryResponse();
        gateway.billResponse.code = "10000";
        gateway.billResponse.msg = "Success";
        gateway.billResponse.billDownloadUrl = "https://bill.example.com/spi.csv";

        AlipayRequestCustomizer customizer = new AlipayRequestCustomizer() {
            /**
             * 为账单下载请求注入测试扩展参数。
             *
             * @param request 账单请求
             * @param options 请求选项
             */
            @Override
            public void customizeDownloadBill(AlipayTradeBillRequest request, AlipayRequestOptions options) {
                options.setRoute("https://route.example.com/gateway");
                options.putOptionalArg("bill_scene", "spi");
            }
        };

        AlipayTradeServiceImpl service = new AlipayTradeServiceImpl(gateway, List.of(customizer));
        service.downloadBill(AlipayTradeBillRequest.builder()
                .billType("trade")
                .billDate("2026-03-29")
                .build());

        assertEquals("https://route.example.com/gateway", gateway.lastRequestContext.route());
        assertEquals("spi", gateway.lastOptionalArgs.get("bill_scene"));
    }

    /**
     * 记录交易增强服务调用信息的测试网关。
     */
    private static final class RecordingGateway implements AlipayGateway {

        /**
         * 账单下载响应。
         */
        private AlipayDataDataserviceBillDownloadurlQueryResponse billResponse;

        /**
         * 撤销响应。
         */
        private AlipayTradeCancelResponse cancelResponse;

        /**
         * 最近一次请求上下文。
         */
        private AlipayRequestContext lastRequestContext;

        /**
         * 最近一次可选参数。
         */
        private Map<String, Object> lastOptionalArgs = new LinkedHashMap<>();

        /**
         * 模拟 APP 支付调用。
         *
         * @param subject 订单标题
         * @param outTradeNo 商户订单号
         * @param totalAmount 订单金额
         * @param notifyUrl 通知地址
         * @param optionalArgs 可选参数
         * @param requestContext 请求上下文
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
         * 模拟花呗分期建单调用。
         */
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
            return new com.alipay.easysdk.payment.huabei.models.AlipayTradeCreateResponse();
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
            this.lastOptionalArgs = new LinkedHashMap<>(optionalArgs);
            this.lastRequestContext = requestContext;
            return cancelResponse;
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
            this.lastRequestContext = requestContext;
            this.lastOptionalArgs = new LinkedHashMap<>(optionalArgs);
            return billResponse;
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
