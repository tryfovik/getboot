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
package com.getboot.payment.infrastructure.wechatpay.operation.coupon;

import com.getboot.payment.api.properties.PaymentProperties;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayH5CouponLaunchRequest;
import com.getboot.payment.api.wechatpay.operation.coupon.WechatPayMiniProgramCouponLaunchRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信支付发券能力测试。
 *
 * @author qiheng
 */
class WechatPayCouponServiceImplTest {

    @Test
    void shouldBuildMiniProgramCouponPluginPayload() {
        WechatPayCouponServiceImpl service = new WechatPayCouponServiceImpl(paymentProperties());

        var response = service.buildMiniProgramLaunch(WechatPayMiniProgramCouponLaunchRequest.builder()
                .coupons(List.of(
                        WechatPayMiniProgramCouponLaunchRequest.Coupon.builder()
                                .stockId("stock-001")
                                .outRequestNo("req-001")
                                .build(),
                        WechatPayMiniProgramCouponLaunchRequest.Coupon.builder()
                                .stockId("stock-002")
                                .outRequestNo("req-002")
                                .build()
                ))
                .build());

        assertEquals("1900001234", response.getSendCouponMerchant());
        assertEquals(
                "[{\"stock_id\":\"stock-001\",\"out_request_no\":\"req-001\"},"
                        + "{\"stock_id\":\"stock-002\",\"out_request_no\":\"req-002\"}]",
                response.getSendCouponParamsJson()
        );
        assertEquals(
                "DFA197C979160A4DD30ED76D62E83FB68F4E5B218FF16B8CADA3355D17492555",
                response.getSign()
        );
    }

    @Test
    void shouldBuildH5CouponLaunchUrl() {
        WechatPayCouponServiceImpl service = new WechatPayCouponServiceImpl(paymentProperties());

        var response = service.buildH5Launch(WechatPayH5CouponLaunchRequest.builder()
                .stockId("stock-001")
                .outRequestNo("req-001")
                .openId("openid-001")
                .couponCode("code-001")
                .build());

        assertEquals("2A927ADF4227EDF0FAF36AF8CF3F7AE0F1F00F7D08BB67D4CF6C9A815234930D", response.getSign());
        assertTrue(response.getUrl().startsWith("https://action.weixin.qq.com/busifavor/getcouponinfo?"));
        assertTrue(response.getUrl().contains("stock_id=stock-001"));
        assertTrue(response.getUrl().contains("open_id=openid-001"));
        assertTrue(response.getUrl().contains("coupon_code=code-001"));
        assertTrue(response.getUrl().endsWith("#wechat_pay&wechat_redirect"));
    }

    private PaymentProperties paymentProperties() {
        PaymentProperties properties = new PaymentProperties();
        properties.getWechatpay().setMerchantId("1900001234");
        properties.getWechatpay().setApiV2Key("test-key");
        return properties;
    }
}
