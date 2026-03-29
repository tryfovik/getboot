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

    private static final class RecordingGateway implements AlipayGateway {

        private AlipayOpenApiGenericResponse genericResponse;
        private String lastMethod;
        private Map<String, String> lastTextParams;
        private Map<String, Object> lastBizParams;
        private AlipayRequestContext lastRequestContext;

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
