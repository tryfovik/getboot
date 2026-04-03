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
package com.getboot.payment.infrastructure.wechatpay.operation.payscore;

import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.wechatpay.operation.payscore.WechatPayPayScoreDetailViewRequest;
import com.getboot.payment.api.wechatpay.operation.payscore.WechatPayPayScoreQueryRequest;
import com.getboot.payment.support.wechatpay.WechatPayHttpGateway;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 微信支付分能力测试。
 *
 * @author qiheng
 */
class WechatPayPayScoreServiceImplTest {

    /**
     * 验证未配置 API V2 密钥时仍可查询支付分订单。
     */
    @Test
    void shouldQueryPayScoreOrderWithoutApiV2Key() {
        RecordingGateway gateway = new RecordingGateway();
        WechatPayPayScoreServiceImpl service = new WechatPayPayScoreServiceImpl(paymentProperties(false), gateway);

        service.queryOrder(WechatPayPayScoreQueryRequest.builder()
                .serviceId("service-001")
                .appId("wx-app-001")
                .outOrderNo("order-001")
                .build());

        assertEquals(
                "/v3/payscore/serviceorder?service_id=service-001&appid=wx-app-001&out_order_no=order-001",
                gateway.lastGetPath
        );
    }

    /**
     * 验证未配置 API V2 密钥时仍可构建确认页参数。
     */
    @Test
    void shouldBuildConfirmViewWithoutApiV2Key() {
        WechatPayPayScoreServiceImpl service = new WechatPayPayScoreServiceImpl(
                paymentProperties(false),
                new RecordingGateway()
        );

        var response = service.buildJsapiConfirmOrderView("prepay_id=wx123");

        assertEquals("wxpayScoreUse", response.getBusinessType());
        assertEquals("package=prepay_id%3Dwx123", response.getQueryString());
    }

    /**
     * 验证配置 API V2 密钥后会生成签名详情页参数。
     */
    @Test
    void shouldBuildOrderDetailViewWithApiV2Signature() {
        WechatPayPayScoreServiceImpl service = new WechatPayPayScoreServiceImpl(
                paymentProperties(true),
                new RecordingGateway()
        );

        var response = service.buildJsapiOrderDetailView(WechatPayPayScoreDetailViewRequest.builder()
                .serviceId("service-001")
                .outOrderNo("order-001")
                .timestamp("1711711711")
                .nonceStr("abc123")
                .build());

        assertEquals("wxpayScoreDetail", response.getBusinessType());
        assertEquals(
                "mch_id=1900001234&service_id=service-001&out_order_no=order-001&timestamp=1711711711"
                        + "&nonce_str=abc123&sign_type=HMAC-SHA256"
                        + "&sign=CA58E86A43842D6D59DE58772B906726428538D7B3ECAE9F808851168583756B",
                response.getQueryString()
        );
    }

    /**
     * 构造测试使用的微信支付配置。
     *
     * @param withApiV2Key 是否注入 API V2 密钥
     * @return 支付配置
     */
    private PaymentProperties paymentProperties(boolean withApiV2Key) {
        PaymentProperties properties = new PaymentProperties();
        properties.getWechatpay().setMerchantId("1900001234");
        if (withApiV2Key) {
            properties.getWechatpay().setApiV2Key("test-key");
        }
        return properties;
    }

    /**
     * 记录支付分请求路径的测试网关。
     */
    private static final class RecordingGateway implements WechatPayHttpGateway {

        /**
         * 最近一次 GET 请求路径。
         */
        private String lastGetPath;

        /**
         * 模拟 GET 请求。
         *
         * @param path 请求路径
         * @param responseType 响应类型
         * @param <T> 响应泛型
         * @return 模拟响应
         */
        @Override
        public <T> T get(String path, Class<T> responseType) {
            this.lastGetPath = path;
            return responseType.cast(Map.of("ok", true));
        }

        /**
         * 模拟 POST 请求。
         *
         * @param path 请求路径
         * @param requestBody 请求体
         * @param responseType 响应类型
         * @param <T> 响应泛型
         * @return 模拟响应
         */
        @Override
        public <T> T post(String path, Object requestBody, Class<T> responseType) {
            return responseType.cast(Map.of("ok", true));
        }

        /**
         * 模拟无响应 POST 请求。
         *
         * @param path 请求路径
         * @param requestBody 请求体
         */
        @Override
        public void postWithoutResponse(String path, Object requestBody) {
        }
    }
}
