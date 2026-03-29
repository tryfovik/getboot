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

import com.getboot.exception.api.exception.BusinessException;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 微信支付 V2 规则内部工具。
 *
 * <p>用于发券插件、H5 发券、支付分详情页等仍沿用 V2 签名规则的场景。</p>
 *
 * @author qiheng
 */
public class WechatPayV2Support {

    /**
     * 微信支付 V2 签名类型。
     */
    public static final String SIGN_TYPE_HMAC_SHA256 = "HMAC-SHA256";

    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final char[] NONCE_CHARS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String apiV2Key;

    /**
     * 构造 V2 工具。
     *
     * @param apiV2Key API V2 密钥
     */
    public WechatPayV2Support(String apiV2Key) {
        if (!StringUtils.hasText(apiV2Key)) {
            throw new BusinessException("getboot.payment.wechatpay.api-v2-key must not be blank for V2 features");
        }
        this.apiV2Key = apiV2Key;
    }

    /**
     * 生成随机串。
     *
     * @return 随机串
     */
    public String generateNonceStr() {
        StringBuilder builder = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            builder.append(NONCE_CHARS[RANDOM.nextInt(NONCE_CHARS.length)]);
        }
        return builder.toString();
    }

    /**
     * 计算 HMAC-SHA256 签名。
     *
     * @param params 参与签名参数
     * @return 签名值
     */
    public String sign(Map<String, ?> params) {
        String signSource = buildSignSource(params);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(apiV2Key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] bytes = mac.doFinal(signSource.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                builder.append(String.format("%02X", value & 0xFF));
            }
            return builder.toString();
        } catch (Exception ex) {
            throw new BusinessException("Failed to sign WeChat Pay V2 request", ex);
        }
    }

    /**
     * 构造待签名字符串。
     *
     * @param params 参数
     * @return 待签名字符串
     */
    public String buildSignSource(Map<String, ?> params) {
        List<String> pairs = new ArrayList<>();
        params.entrySet().stream()
                .filter(entry -> shouldParticipate(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> pairs.add(entry.getKey() + "=" + entry.getValue()));
        pairs.add("key=" + apiV2Key);
        return String.join("&", pairs);
    }

    /**
     * 按当前迭代顺序构造 query string。
     *
     * @param params 参数
     * @return query string
     */
    public String buildQueryString(Map<String, ?> params) {
        List<String> pairs = new ArrayList<>();
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            pairs.add(entry.getKey() + "=" + urlEncode(String.valueOf(entry.getValue())));
        }
        return String.join("&", pairs);
    }

    /**
     * URL 编码。
     *
     * @param value 原值
     * @return 编码后值
     */
    public String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    private boolean shouldParticipate(String key, Object value) {
        if (value == null) {
            return false;
        }
        if ("sign".equalsIgnoreCase(key) || "key".equalsIgnoreCase(key)) {
            return false;
        }
        return !(value instanceof CharSequence) || StringUtils.hasText((CharSequence) value);
    }
}
