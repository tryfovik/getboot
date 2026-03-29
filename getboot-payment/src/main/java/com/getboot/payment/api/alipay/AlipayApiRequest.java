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
package com.getboot.payment.api.alipay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝开放接口通用调用请求。
 *
 * <p>用于承接官方 Easy SDK 尚未提供稳定强类型封装的长尾 OpenAPI。</p>
 *
 * @author qiheng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlipayApiRequest {

    /**
     * OpenAPI 方法名，例如 `alipay.trade.query`。
     */
    private String method;

    /**
     * 非 `biz_content` 参数。
     */
    @Builder.Default
    private Map<String, String> textParams = new LinkedHashMap<>();

    /**
     * `biz_content` 参数。
     */
    @Builder.Default
    private Map<String, Object> bizParams = new LinkedHashMap<>();

    /**
     * 调用上下文元数据。
     */
    @Builder.Default
    private Map<String, String> metadata = new LinkedHashMap<>();
}
