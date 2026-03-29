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

import com.alipay.easysdk.kernel.util.ResponseChecker;
import com.aliyun.tea.TeaModel;
import com.getboot.exception.api.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝响应辅助工具。
 *
 * @author qiheng
 */
public final class AlipayResponseSupport {

    private AlipayResponseSupport() {
    }

    /**
     * 校验官方响应是否成功。
     *
     * @param response 响应对象
     * @param message  失败消息
     */
    public static void ensureSuccess(TeaModel response, String message) {
        if (ResponseChecker.success(response)) {
            return;
        }
        Map<String, ?> metadata = TeaModel.buildMap(response);
        throw new BusinessException(message + ": "
                + firstNonBlank(stringValue(metadata.get("code")), "unknown")
                + " "
                + firstNonBlank(stringValue(metadata.get("msg")), "")
                + " "
                + firstNonBlank(stringValue(metadata.get("subCode")), "")
                + " "
                + firstNonBlank(stringValue(metadata.get("subMsg")), ""));
    }

    /**
     * 提取响应元数据。
     *
     * @param response 响应对象
     * @return 元数据
     */
    public static Map<String, String> extractMetadata(TeaModel response) {
        Map<String, String> metadata = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : TeaModel.buildMap(response).entrySet()) {
            if (entry.getValue() != null) {
                metadata.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        return metadata;
    }

    /**
     * 转换金额。
     *
     * @param amount 金额文本
     * @return 金额对象
     */
    public static BigDecimal toBigDecimal(String amount) {
        return StringUtils.hasText(amount) ? new BigDecimal(amount) : null;
    }

    /**
     * 返回第一个非空白文本。
     *
     * @param values 候选文本
     * @return 第一个非空白文本
     */
    public static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
