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
package com.getboot.payment.support.alipay;

import org.springframework.util.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝异步通知辅助工具。
 *
 * <p>用于统一处理 form-urlencoded 报文解析与验签前后的参数清洗。</p>
 *
 * @author qiheng
 */
public final class AlipayNotifySupport {

    private AlipayNotifySupport() {
    }

    /**
     * 解析支付宝通知表单报文。
     *
     * @param body 原始报文
     * @return 参数表
     */
    public static Map<String, String> parseFormBody(String body) {
        Map<String, String> parameters = new LinkedHashMap<>();
        if (!StringUtils.hasText(body)) {
            return parameters;
        }
        for (String pair : body.split("&")) {
            if (!StringUtils.hasText(pair)) {
                continue;
            }
            int separatorIndex = pair.indexOf('=');
            if (separatorIndex < 0) {
                parameters.put(urlDecode(pair), "");
                continue;
            }
            String key = urlDecode(pair.substring(0, separatorIndex));
            String value = urlDecode(pair.substring(separatorIndex + 1));
            parameters.put(key, value);
        }
        return parameters;
    }

    /**
     * 构建通知元数据。
     *
     * <p>签名字段会被移除，便于直接透传业务字段。</p>
     *
     * @param parameters 通知参数
     * @return 元数据
     */
    public static Map<String, String> buildMetadata(Map<String, String> parameters) {
        Map<String, String> metadata = new LinkedHashMap<>();
        if (parameters == null || parameters.isEmpty()) {
            return metadata;
        }
        metadata.putAll(parameters);
        metadata.remove("sign");
        metadata.remove("sign_type");
        return metadata;
    }

    private static String urlDecode(String text) {
        return URLDecoder.decode(text, StandardCharsets.UTF_8);
    }
}
