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
package com.getboot.payment.api.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 支付配置绑定测试。
 *
 * @author qiheng
 */
class PaymentPropertiesBindingTest {

    @Test
    void shouldBindNestedPaymentPropertiesFromKebabCaseConfiguration() {
        Map<String, String> source = new LinkedHashMap<>();
        source.put("getboot.payment.enabled", "false");
        source.put("getboot.payment.alipay.enabled", "true");
        source.put("getboot.payment.alipay.app-id", "2026000000000001");
        source.put("getboot.payment.alipay.merchant-private-key", "merchant-private-key");
        source.put("getboot.payment.alipay.alipay-public-key", "alipay-public-key");
        source.put("getboot.payment.alipay.notify-url", "https://demo.example.com/alipay/notify");
        source.put("getboot.payment.alipay.return-url", "https://demo.example.com/alipay/return");
        source.put("getboot.payment.wechatpay.enabled", "true");
        source.put("getboot.payment.wechatpay.app-id", "wx1234567890");
        source.put("getboot.payment.wechatpay.merchant-id", "1900001234");
        source.put("getboot.payment.wechatpay.merchant-serial-number", "4A3B2C1D");
        source.put("getboot.payment.wechatpay.api-v3-key", "api-v3-key");
        source.put("getboot.payment.wechatpay.api-v2-key", "api-v2-key");
        source.put("getboot.payment.wechatpay.private-key-location", "classpath:payment/wechat/apiclient_key.pem");
        source.put("getboot.payment.wechatpay.notify-url", "https://demo.example.com/wechat/notify");

        MapConfigurationPropertySource propertySource = new MapConfigurationPropertySource(source);

        PaymentProperties properties = new Binder(propertySource)
                .bind("getboot.payment", Bindable.of(PaymentProperties.class))
                .orElseThrow(() -> new IllegalStateException("payment properties should bind"));

        assertFalse(properties.isEnabled());

        assertTrue(properties.getAlipay().isEnabled());
        assertEquals("2026000000000001", properties.getAlipay().getAppId());
        assertEquals("merchant-private-key", properties.getAlipay().getMerchantPrivateKey());
        assertEquals("alipay-public-key", properties.getAlipay().getAlipayPublicKey());
        assertEquals("https://demo.example.com/alipay/notify", properties.getAlipay().getNotifyUrl());
        assertEquals("https://demo.example.com/alipay/return", properties.getAlipay().getReturnUrl());
        assertEquals("https", properties.getAlipay().getProtocol());
        assertEquals("RSA2", properties.getAlipay().getSignType());

        assertTrue(properties.getWechatpay().isEnabled());
        assertEquals("wx1234567890", properties.getWechatpay().getAppId());
        assertEquals("1900001234", properties.getWechatpay().getMerchantId());
        assertEquals("4A3B2C1D", properties.getWechatpay().getMerchantSerialNumber());
        assertEquals("api-v3-key", properties.getWechatpay().getApiV3Key());
        assertEquals("api-v2-key", properties.getWechatpay().getApiV2Key());
        assertEquals("classpath:payment/wechat/apiclient_key.pem", properties.getWechatpay().getPrivateKeyLocation());
        assertEquals("https://demo.example.com/wechat/notify", properties.getWechatpay().getNotifyUrl());
    }
}
