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
import com.getboot.payment.support.alipay.AlipayRequestSupport;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * 微信支付请求读取辅助工具。
 *
 * @author qiheng
 */
public final class WechatPayRequestSupport {

    private WechatPayRequestSupport() {
    }

    public static String text(Map<String, String> metadata, String key) {
        return AlipayRequestSupport.text(metadata, key);
    }

    public static String requiredHeader(Map<String, String> headers, String name) {
        String value = header(headers, name);
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("Missing WeChat Pay notification header: " + name);
        }
        return value;
    }

    public static String header(Map<String, String> headers, String name) {
        if (headers == null || headers.isEmpty() || !StringUtils.hasText(name)) {
            return null;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (name.equalsIgnoreCase(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
