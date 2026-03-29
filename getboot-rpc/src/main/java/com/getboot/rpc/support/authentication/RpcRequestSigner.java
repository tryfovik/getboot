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
package com.getboot.rpc.support.authentication;

import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * RPC 请求签名工具类。
 *
 * <p>基于调用方凭证与调用元数据生成 HMAC-SHA256 签名。</p>
 *
 * @author qiheng
 */
public final class RpcRequestSigner {

    private static final String HMAC_SHA_256 = "HmacSHA256";

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private RpcRequestSigner() {
    }

    public static String sign(String appId, String appSecret, String serviceName, String methodName, long timestamp) {
        String canonicalRequest = canonicalRequest(appId, serviceName, methodName, timestamp);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            return toHex(mac.doFinal(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to sign the RPC request.", exception);
        }
    }

    public static boolean matches(String expectedSignature, String actualSignature) {
        if (!StringUtils.hasText(expectedSignature) || !StringUtils.hasText(actualSignature)) {
            return false;
        }
        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                actualSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String canonicalRequest(String appId, String serviceName, String methodName, long timestamp) {
        String resolvedServiceName = StringUtils.hasText(serviceName) ? serviceName : "";
        String resolvedMethodName = StringUtils.hasText(methodName) ? methodName : "";
        return appId + '\n' + resolvedServiceName + '\n' + resolvedMethodName + '\n' + timestamp;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(HEX_DIGITS[(value >> 4) & 0x0F]);
            builder.append(HEX_DIGITS[value & 0x0F]);
        }
        return builder.toString();
    }
}
