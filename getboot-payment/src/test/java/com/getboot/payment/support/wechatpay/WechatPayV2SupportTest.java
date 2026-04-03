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
package com.getboot.payment.support.wechatpay;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信支付 V2 工具测试。
 *
 * @author qiheng
 */
class WechatPayV2SupportTest {

    /**
     * 验证签名原文与签名值稳定。
     */
    @Test
    void shouldBuildStableSignSourceAndSignature() {
        WechatPayV2Support support = new WechatPayV2Support("test-key");
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("nonce_str", "abc123");
        args.put("foo", "bar");

        assertEquals("foo=bar&nonce_str=abc123&key=test-key", support.buildSignSource(args));
        assertEquals(
                "6769804F1BC192767C9A08B0A0285926B881BA9EF17492D91F7BB5110436DCB4",
                support.sign(args)
        );
    }

    /**
     * 验证查询字符串会执行 URL 编码。
     */
    @Test
    void shouldBuildQueryStringWithUrlEncoding() {
        WechatPayV2Support support = new WechatPayV2Support("test-key");
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("name", "test user");
        args.put("city", "shanghai");

        assertEquals("name=test%20user&city=shanghai", support.buildQueryString(args));
    }

    /**
     * 验证随机串由 32 位字母数字组成。
     */
    @Test
    void shouldGenerateAlphaNumericNonce() {
        WechatPayV2Support support = new WechatPayV2Support("test-key");

        String nonce = support.generateNonceStr();

        assertEquals(32, nonce.length());
        assertTrue(nonce.chars().allMatch(ch -> Character.isLetterOrDigit(ch)));
    }
}
