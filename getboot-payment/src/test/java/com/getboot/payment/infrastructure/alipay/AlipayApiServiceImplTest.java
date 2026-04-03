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
import com.getboot.payment.api.alipay.AlipayApiRequest;
import com.getboot.payment.support.alipay.AlipayGateway;
import com.getboot.payment.support.alipay.AlipayRequestContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 支付宝开放接口兜底服务测试。
 *
 * @author qiheng
 */
class AlipayApiServiceImplTest {

    /**
     * 验证泛化 OpenAPI 调用透传。
     */
    @Test
    void shouldExecuteGenericOpenApi() {
        RecordingGateway gateway = new RecordingGateway();
        gateway.genericResponse = new AlipayOpenApiGenericResponse();
        gateway.genericResponse.code = "10000";
        gateway.genericResponse.msg = "Success";
        gateway.genericResponse.httpBody = "{\"ok\":true}";

        AlipayApiServiceImpl service = new AlipayApiServiceImpl(gateway);
        var response = service.execute(AlipayApiRequest.builder()
                .method("alipay.trade.query")
                .textParams(Map.of("app_auth_token", "token-001"))
                .bizParams(Map.of("out_trade_no", "order-008"))
                .metadata(Map.of("appAuthToken", "token-meta-001"))
                .build());

        assertEquals("alipay.trade.query", gateway.lastMethod);
        assertEquals("token-001", gateway.lastTextParams.get("app_auth_token"));
        assertEquals("order-008", gateway.lastBizParams.get("out_trade_no"));
        assertEquals("token-meta-001", gateway.lastRequestContext.appAuthToken());
        assertEquals("{\"ok\":true}", response.getHttpBody());
    }

    /**
     * 记录开放接口调用信息的测试网关。
     */
    private static final class RecordingGateway implements AlipayGateway {

        /**
         * 泛化调用响应。
         */
        private AlipayOpenApiGenericResponse genericResponse;

        /**
         * 最近一次方法名。
         */
        private String lastMethod;

        /**
         * 最近一次文本参数。
         */
        private Map<String, String> lastTextParams;

        /**
         * 最近一次业务参数。
         */
        private Map<String, Object> lastBizParams;

        /**
         * 最近一次请求上下文。
         */
        private AlipayRequestContext lastRequestContext;

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
         * 模拟带上下文的泛化调用。
         */
        @Override
        public AlipayOpenApiGenericResponse execute(
                String method,
                Map<String, String> textParams,
                Map<String, Object> bizParams,
                AlipayRequestContext requestContext) {
            this.lastMethod = method;
            this.lastTextParams = textParams;
            this.lastBizParams = bizParams;
            this.lastRequestContext = requestContext;
            return genericResponse;
        }
    }
}
